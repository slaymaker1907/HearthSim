package com.hearthsim.gui.cardcomparator;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;

import org.apache.commons.math3.stat.inference.AlternativeHypothesis;
import org.apache.commons.math3.stat.inference.BinomialTest;

import com.hearthsim.card.Deck;
import com.hearthsim.card.ImplementedCardList.ImplementedCard;
import com.hearthsim.util.DeckFactory;

public class BetterCardComparator implements Comparator<ImplementedCard>
{
    private int count;
    private HashMap<String, CardVersusCard> cardData;
    private String serialFileName;
    
    public BetterCardComparator(String serialFileName)
    {
        this.serialFileName = serialFileName;
        initializeComparisonDataFromFile();
        Runtime.getRuntime().addShutdownHook(new Thread(() ->dumpData()));
        DeckFactory.DeckFactoryBuilder builder = new DeckFactory.DeckFactoryBuilder();
        ArrayList<ImplementedCard> allCards = builder.buildDeckFactory().getAllPossibleCards();
        for(ImplementedCard card : allCards)
        {
            cardData.put(card.name_, new CardVersusCard(card));
        }
    }
    
    @Override
    public int compare(ImplementedCard o1, ImplementedCard o2) 
    {
        CardComparator.original.println("Now comparing " + o1.name_ + " to " + o2.name_);
        count = 0;
        if (!cardData.containsKey(o1.name_))
            cardData.put(o1.name_, new CardVersusCard(o1));
        if (!cardData.containsKey(o2.name_))
            cardData.put(o2.name_, new CardVersusCard(o2));
        
        CardVersusCard c1 = cardData.get(o1.name_);
        CardVersusCard c2 = cardData.get(o2.name_);
        PlayerModelFactory.Builder factory1, factory2;
        factory1 = getBuilder(o1);
        factory2 = getBuilder(o2);
        
        ObserverFunctor analysis = new ObserverFunctor(factory1, factory2, this, c1, c2);
        analysis.run();
        
        CardComparator.original.println();
        return Double.compare(c1.winRatio(), c2.winRatio());
    }
    
    public PlayerModelFactory.Builder getBuilder(ImplementedCard card)
    {
        String hero = card.charClass_;
        PlayerModelFactory.Builder result = new PlayerModelFactory.Builder().includeCards(card).specifyPlayerInfo("", (byte)0);
        if (!hero.equals("neutral"))
            result.specifyHero(hero);
        return result;
    }

//    @Override
//    public void update(Observable o, Object arg) 
//    {
//        try
//        {
//            DetailedGameResult result = (DetailedGameResult)arg;
//            Deck mainDeck = result.cards;
//            synchronized(cardData)
//            {
//                count++;
//                for(int i = 0; i < 30; i++)
//                {
//                    int wins;
//                    if (result.isWin)
//                        wins = 1;
//                    else
//                        wins = 0;
//                    CardGameResult gameResult = new CardGameResult(wins, 1);
//                    cardData.get(mainDeck.drawCard(i).getName()).addGameResult(gameResult);
//                }
//            }
//        } catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//    }
    
    public void addResult(DetailedGameResult result)
    {
        Deck mainDeck = result.cards;
        for(int i = 0; i < 30; i++)
        {
            int wins;
            if (result.isWin)
                wins = 1;
            else
                wins = 0;
            CardGameResult gameResult = new CardGameResult(wins, 1);
            cardData.get(mainDeck.drawCard(i).getName()).addGameResult(gameResult);
        }
    }
    
    public void processResults(Collection<DetailedGameResult> results, int count)
    {
        this.count += count;
        for(DetailedGameResult result : results)
            addResult(result);
    }
    
    private class ObserverFunctor implements Runnable
    {
        private long start;
        private HearthstoneExecutor toControl0, toControl1;
        private CardVersusCard first, second;
        private BetterCardComparator compRef;
        
        public ObserverFunctor(PlayerModelFactory.Builder player0, PlayerModelFactory.Builder player1, BetterCardComparator compRef, CardVersusCard first, CardVersusCard second)
        {
            PlayerModelFactory.Builder randomFactory = new PlayerModelFactory.Builder().specifyPlayerInfo("", (byte) 1);
            toControl0 = new HearthstoneExecutor(player0, randomFactory);
            toControl1 = new HearthstoneExecutor(player1, randomFactory);
            this.first = first;
            this.second = second;
            this.compRef = compRef;
            start = System.currentTimeMillis();
        }
        
        @Override
        public void run()
        {
            while(true)
            {
                try {
                    Thread.sleep(1000 * 60);
                } catch (InterruptedException e) { }
                
                ArrayList<DetailedGameResult> results = new ArrayList<DetailedGameResult>();
                int count = 0;
                count += toControl0.accumulateResults(results);
                count += toControl1.accumulateResults(results);
                compRef.processResults(results, count);
                
                dumpData();

                if (isReady(first, second))
                {
                    toControl0.stopComputation();
                    toControl1.stopComputation();
                    return;
                }
                
                long currentTime = System.currentTimeMillis() - start;
                CardComparator.original.println("Processing " + 1000.0 * compRef.count / currentTime + " games per second.");
            }
        }
    }
    
    private boolean isReady(final CardVersusCard first,
            final CardVersusCard second)
    {
        final double alpha = 0.1;
        final int maxGames = 2000;

        if (first.totalGames() < 2 || second.totalGames() < 2)
            return false;
        else if (first.totalGames() > maxGames
                && second.totalGames() > maxGames)
            return true;

        // Do a test to make sure that there is alpha chance that the two
        // probabilities are equal.
        final BinomialTest test = new BinomialTest();
        return test.binomialTest(first.totalGames(), first.totalWins(),
                second.winRatio(), AlternativeHypothesis.TWO_SIDED, alpha)
                && test.binomialTest(second.totalGames(), second.totalWins(),
                        first.winRatio(), AlternativeHypothesis.TWO_SIDED,
                        alpha);
    }
    
    @SuppressWarnings("unchecked")
    private void initializeComparisonDataFromFile()
    {
        try
        {
            final FileInputStream fileIn = new FileInputStream(
                    this.serialFileName);
            final ObjectInputStream objectIn = new ObjectInputStream(fileIn);
            this.cardData = (HashMap<String, CardVersusCard>) objectIn
                    .readObject();
            objectIn.close();
            fileIn.close();
        } catch (final Exception e)
        {
            // e.printStackTrace();
            CardComparator.original
                    .println("Could not read in comparison data.  Starting from scratch.");
            this.cardData = new HashMap<String, CardVersusCard>();
        }
    }
    
    // Make sure that this call is synchronzied on cardData.
    private void dumpData()
    {
        try
        {
            final PrintWriter writer = new PrintWriter(
                    this.serialFileName.substring(0,
                            this.serialFileName.length() - 4)
                            + ".txt");
            for (final String card : this.cardData
                    .keySet())
            {
                writer.print(this.cardData.get(card).toString());
            }
            writer.close();
        } catch (final Exception e)
        {
            e.printStackTrace();
        }
        try
        {
            final FileOutputStream fileOut = new FileOutputStream(
                    this.serialFileName);
            final ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(this.cardData);
            objectOut.close();
            fileOut.close();
        } catch (final Exception e)
        {
            e.printStackTrace();
        }
    }
}
