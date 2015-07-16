package com.hearthsim.gui.cardcomparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import com.hearthsim.Game;
import com.hearthsim.card.Deck;
import com.hearthsim.model.PlayerModel;
import com.hearthsim.player.playercontroller.ArtificialPlayer;
import com.hearthsim.player.playercontroller.BruteForceSearchAI;
import com.hearthsim.results.GameResult;

public class ComputeGames implements Runnable
{
    private PlayerModelFactory player0, player1;
    private ArtificialPlayer ai;
    private Random gen = new Random();
    private Boolean runComputation = true;
    private AtomicInteger count;
    private ArrayList<DetailedGameResult> results;
    
    public ComputeGames(PlayerModelFactory player0, PlayerModelFactory player1)
    {
        this.player0 = player0;
        this.player1 = player1;
        ai = BruteForceSearchAI.buildStandardAI2();
        count = new AtomicInteger(0);
        this.results = new ArrayList<DetailedGameResult>();
    }
    
    @Override
    public void run() 
    {
        while(runComputation)
        {
            try
            {
                PlayerModel mainPlayer = player0.createPlayer();
                Deck mainDeck = mainPlayer.getDeck().deepCopy();
                GameResult result = new Game(mainPlayer, player1.createPlayer(), ai.deepCopy(), ai.deepCopy(), gen.nextInt(2)).runGame();
                boolean isWin = result.winnerPlayerIndex_ == 0;
                
                DetailedGameResult toAdd = new DetailedGameResult(mainDeck, isWin);
                synchronized(result)
                {
                    results.add(toAdd);
                }
                count.incrementAndGet();
            }
            catch (Exception e)
            {
            }
        }
    }
    
    public void stopComputation()
    {
            runComputation = false;
    }
    
    @Override
    protected void finalize()
    {
        stopComputation();
    }
    
    public int addResultsToCollection(Collection<DetailedGameResult> results)
    {
        synchronized(this.results)
        {
            results.addAll(this.results);
            results.clear();
        }
        
        return count.getAndSet(0);
    }
}
