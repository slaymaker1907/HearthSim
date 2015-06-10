package com.hearthsim.gui.cardcomparator;

import java.util.Observable;
import java.util.Random;

import com.hearthsim.Game;
import com.hearthsim.card.Deck;
import com.hearthsim.model.PlayerModel;
import com.hearthsim.player.playercontroller.ArtificialPlayer;
import com.hearthsim.player.playercontroller.BruteForceSearchAI;
import com.hearthsim.results.GameResult;

public class ComputeGames extends Observable implements Runnable
{
    private PlayerModelFactory player0, player1;
    private ArtificialPlayer ai;
    private Random gen = new Random();
    private Boolean runComputation = true;
    
    public ComputeGames(PlayerModelFactory player0, PlayerModelFactory player1)
    {
        this.player0 = player0;
        this.player1 = player1;
        ai = BruteForceSearchAI.buildStandardAI2();
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
                this.setChanged();
                this.notifyObservers(new DetailedGameResult(mainDeck, isWin));
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
}
