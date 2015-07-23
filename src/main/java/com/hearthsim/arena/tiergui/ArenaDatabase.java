package com.hearthsim.arena.tiergui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import com.hearthsim.arena.ArenaCardComparator;
import com.hearthsim.arena.ArenaGenerator;
import com.hearthsim.arena.StaticArenaAgent;
import com.hearthsim.card.Card;
import com.hearthsim.card.ImplementedCardList.ImplementedCard;
import com.hearthsim.model.PlayerModel;
import com.hearthsim.util.immutable.ImmutableMap;

public class ArenaDatabase 
{
    public static class ArenaResult implements Serializable
    {
        private static final long serialVersionUID = 1L;
        public final Tuple<List<ImplementedCard>, String> winner, loser;
        public ArenaResult(Tuple<List<ImplementedCard>, String> winner,
                Tuple<List<ImplementedCard>, String> loser)
        {
            this.winner = winner;
            this.loser = loser;
        }
    }
    
    private final String fileName;
    private final ArrayList<ArenaResult> results;
    private final long start;
    private final int initialCount;
    
    public ArenaDatabase(String fileName)
    {
        this.fileName = fileName;
        this.results = ArenaDatabase.initializeResults(this.fileName);
        this.initialCount = results.size();
        this.start = System.currentTimeMillis();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> this.saveData()));
    }
    
    @SuppressWarnings("unchecked")
    private static ArrayList<ArenaResult> initializeResults(String fileName)
    {
        try
        {
            return (ArrayList<ArenaResult>)ArenaTierReference.getObjectFromFile(fileName);
        }
        catch (Exception e)
        {
            System.out.println("Could not initialize ArenaDatabase from " + fileName);
            return new ArrayList<ArenaResult>();
        }
    }
    
    private synchronized void saveData()
    {
        synchronized(results)
        {
            try 
            {
                ArenaTierReference.saveObjectToFile(results, this.fileName);
            } catch (Exception e) 
            {
                System.out.println("Could not save ArenaDatabase to file.");
                e.printStackTrace();
            }
        }
    }
    
    private void addResult(Tuple<List<ImplementedCard>, String> winner,
            Tuple<List<ImplementedCard>, String> loser)
    {
        synchronized(this.results)
        {
            this.results.add(new ArenaResult(winner, loser));
            if (results.size() % 50 == 0)
            {
                this.saveData();
                System.out.println("Processed " + results.size() + " results.");
                final int resultsProcessed = results.size() - this.initialCount;
                double currentTime = (System.currentTimeMillis() - this.start) / 1000.0;
                double resultsPerSecond = resultsProcessed / currentTime;
                System.out.println("\tProcessing " + resultsPerSecond + " results per second.");
            }
        }
    }
    
    private static Tuple<List<ImplementedCard>, String> getTuple(BiFunction<Byte, String, PlayerModel> generator)
    {
        PlayerModel sampleModel = generator.apply((byte)0, "");
        String hero = sampleModel.getHero().getName();
        List<ImplementedCard> cards = ArenaGenerator.convertCollection(sampleModel.getDeck().getAllCards(),
                Card::getImplementedCard);
        cards = Collections.unmodifiableList(cards);
        return new Tuple<List<ImplementedCard>, String>(cards, hero);
    }
    
    private static boolean isComplete()
    {
        return false;
    }
        
    private static BiFunction<Byte, String, PlayerModel> getPlayer() throws Exception
    {
        return ArenaGenerator.simulateArena(StaticArenaAgent::takeTurn);
    }
    
    public Object gatherData()
    {
        if (ArenaDatabase.isComplete())
            return new Object();
        else
        {
            BiFunction<Byte, String, PlayerModel> firstPlayer, secondPlayer, winner, loser;
            try
            {
                firstPlayer = ArenaDatabase.getPlayer();
                secondPlayer = ArenaDatabase.getPlayer();
            }
            catch (Exception e)
            {
                return null;
            }
            int result = ArenaCardComparator.runGame(firstPlayer, secondPlayer);
            switch(result)
            {
            case 1:
                winner = firstPlayer;
                loser = secondPlayer;
                break;
            case 2:
                winner = secondPlayer;
                loser = firstPlayer;
                break;
            default:
                return null;
            }
            this.addResult(ArenaDatabase.getTuple(winner), ArenaDatabase.getTuple(loser));
            return null;
        }
    }
    
    public void forEach(Consumer<ArenaResult> procedure)
    {
        this.results.forEach(procedure);
    }
    
    public <Output> ArrayList<Output> mapResults(Function<ArenaResult, Output> mapFunction)
    {
        return ArenaGenerator.convertCollection(this.results, mapFunction);
    }
    
    public Function<Integer, Double> getWinningCurve()
    {
        Function<ImplementedCard, Integer> getManaGroup = (card) -> card.mana_;
        ArrayList<ArrayList<Integer>> manaCosts = this.mapResults((arenaResult) ->
        {
                return ArenaGenerator.convertCollection(arenaResult.winner.getFirst(), getManaGroup);
        });
        final double totalCards = manaCosts.size() * 30;
        HashMap<Integer, Integer> manaGroupCollection = new HashMap<>();
        for(ArrayList<Integer> deckManaCosts : manaCosts)
        {
            for(Integer manaCost : deckManaCosts)
            {
                manaGroupCollection.merge(manaCost, 1, (oldNum, newNum) -> oldNum + newNum);
            }
        }
        
        ImmutableMap<Integer, Double> probabilityMap = new ImmutableMap<>(manaGroupCollection.keySet(),
                (manaCost) -> ((double) manaGroupCollection.get(manaCost)) / totalCards);
        return (manaCost) -> probabilityMap.getOrDefault(manaCost, 0.0);
    }
    
    public Function<Integer, Double> getWinningGroupCurve()
    {
        Function<Integer, Double> originalFunction = this.getWinningCurve();
        final double twos = (originalFunction.apply(0) + originalFunction.apply(1) + originalFunction.apply(2)) * 30;
        double sevens = 0.0;
        for(int turn = 7; turn <= 20; turn++)
            sevens += originalFunction.apply(turn);
        sevens *= 30;
        // Hack to make the lambda compiler not complain.
        final double sevenPlus = sevens;
        return (manaGroup) ->
        {
            if (manaGroup > 2 && manaGroup < 7)
                return originalFunction.apply(manaGroup) * 30;
            else if (manaGroup <= 2)
                return twos;
            else
                return sevenPlus;
        };
    }
}
