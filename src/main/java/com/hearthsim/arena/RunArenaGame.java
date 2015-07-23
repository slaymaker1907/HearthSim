package com.hearthsim.arena;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.math3.stat.inference.AlternativeHypothesis;
import org.apache.commons.math3.stat.inference.BinomialTest;

import com.hearthsim.model.PlayerModel;

public class RunArenaGame implements Supplier<Integer>
{
    private final Object addingResultLock = new Object();
    private Generator record1, record2;
    
    private static class Generator
    {
        public final int wins, totalGames;
        public final Supplier<BiFunction<Byte, String, PlayerModel>> generatorFunction;
        
        public Generator(int wins, int totalGames, Supplier<BiFunction<Byte, String, PlayerModel>> generatorFunction)
        {
            this.wins = wins;
            this.totalGames = totalGames;
            this.generatorFunction = generatorFunction;
        }
        
        public double getWinRatio()
        {
            double doubleWins, doubleGames;
            doubleWins = (double)wins;
            doubleGames = (double)totalGames;
            return doubleWins / doubleGames;
        }
        
        public Generator addResult(boolean wonGame)
        {
            int newWins;
            if (wonGame)
                newWins = this.wins + 1;
            else
                newWins = this.wins;
            return new Generator(newWins, this.totalGames + 1, this.generatorFunction);
        }
    }
    
    public RunArenaGame(Supplier<BiFunction<Byte, String, PlayerModel>> gameGenerator1, Supplier<BiFunction<Byte, String, PlayerModel>> gameGenerator2)
    {
        this.record1 = new Generator(0, 0, gameGenerator1);
        this.record2 = new Generator(0, 0, gameGenerator2);
    }

    @Override
    public Integer get() 
    {
        if (RunArenaGame.isReady(record1, record2))
        {
            if (record1.getWinRatio() >= record2.getWinRatio())
                return 1;
            else
                return -1;
        }
        else
        {
            this.runGame(1);
            this.runGame(2);
            return null;
        }
    }
    
    private Generator getGenerator(int generatorNum)
    {
        Generator oldGenerator;
        if (generatorNum == 1)
            oldGenerator = this.record1;
        else
            oldGenerator = this.record2;
        return oldGenerator;
    }
        
    // This method is thread safe.
    private void incrementGenerator(int generatorNum, int result)
    {
        if (generatorNum != 1 && generatorNum != 2)
            throw new IllegalArgumentException("Generator number must be 1 or 2: " + generatorNum);
        
        Function<Generator, Generator> generatorChange = (oldGenerator) ->
        {
            if (result == 1)
                return oldGenerator.addResult(true);
            else if (result == 2)
                return oldGenerator.addResult(false);
            else
                return oldGenerator;
        };
        
        synchronized(this.addingResultLock)
        {
            if (generatorNum == 1)
                this.record1 = generatorChange.apply(this.record1);
            else
                this.record2 = generatorChange.apply(this.record2);
        }
    }
    
    private void runGame(int generatorNum)
    {
        if (generatorNum != 1 && generatorNum != 2)
            throw new IllegalArgumentException("Generator number must be 1 or 2: " + generatorNum);
        int result = -1;
        Generator oldGenerator = this.getGenerator(generatorNum);
        try
        {
            result = ArenaCardComparator.runGameVersusRandom(oldGenerator.generatorFunction.get());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        this.incrementGenerator(generatorNum, result);
    }
        
    private static boolean isReady(Generator record1, Generator record2)
    {
        final double alpha = 0.1;
        final int maxGames = Integer.MAX_VALUE;
        
        if (record1.totalGames < 2 || record2.totalGames < 2)
            return false;
        else if (record1.totalGames > maxGames && record2.totalGames > maxGames)
            return true;
        
        // Do a test to make sure that there is alpha chance that the two
        // probabilities are equal.
        final BinomialTest test = new BinomialTest();
        return test.binomialTest(record1.totalGames, record1.wins, record2.getWinRatio(), AlternativeHypothesis.TWO_SIDED, alpha) &&
                test.binomialTest(record2.totalGames, record2.wins, record1.getWinRatio(), AlternativeHypothesis.TWO_SIDED, alpha);
    }
}
