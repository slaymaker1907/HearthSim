package com.hearthsim.arena.tiergui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import com.hearthsim.arena.ArenaAgent;
import com.hearthsim.arena.ArenaCardComparator;
import com.hearthsim.arena.ArenaGenerator;
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
    
    private static final String fileName = "ArenaDatabase.ser";
    private static final ArrayList<ArenaResult> results = initializeResults();
    private static final long start = System.currentTimeMillis();
    
    @SuppressWarnings("unchecked")
    private static ArrayList<ArenaResult> initializeResults()
    {
        try
        {
            return (ArrayList<ArenaResult>)ArenaTierReference.getObjectFromFile(ArenaDatabase.fileName);
        }
        catch (Exception e)
        {
            System.out.println("Could not initialize ArenaDatabase from file.");
            return new ArrayList<ArenaResult>();
        }
    }
    
    private static void saveData()
    {
        try 
        {
            ArenaTierReference.saveObjectToFile(results, ArenaDatabase.fileName);
        } catch (Exception e) 
        {
            System.out.println("Could not save ArenaDatabase to file.");
            e.printStackTrace();
        }
    }
    
    private static void addResult(Tuple<List<ImplementedCard>, String> winner,
            Tuple<List<ImplementedCard>, String> loser)
    {
        synchronized(ArenaDatabase.results)
        {
            ArenaDatabase.results.add(new ArenaResult(winner, loser));
            if (results.size() % 50 == 0)
            {
                ArenaDatabase.saveData();
                System.out.println("Processed " + results.size() + " results.");
                double resultsPerSecond = results.size() * 1000.0 / (System.currentTimeMillis() - ArenaDatabase.start);
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
        return ArenaGenerator.simulateArena(ArenaAgent::randomSelector);
    }
    
    public static Object gatherData()
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
            ArenaDatabase.addResult(ArenaDatabase.getTuple(winner), ArenaDatabase.getTuple(loser));
            return null;
        }
    }
    
    public static void forEach(Consumer<ArenaResult> procedure)
    {
        ArenaDatabase.results.forEach(procedure);
    }
    
    public static <Output> ArrayList<Output> mapResults(Function<ArenaResult, Output> mapFunction)
    {
        return ArenaGenerator.convertCollection(ArenaDatabase.results, mapFunction);
    }
    
    public static Function<Integer, Double> getWinningCurve()
    {
        Function<ImplementedCard, Integer> getManaGroup = (card) -> card.mana_;
        ArrayList<ArrayList<Integer>> manaCosts = ArenaDatabase.mapResults((arenaResult) ->
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
}
