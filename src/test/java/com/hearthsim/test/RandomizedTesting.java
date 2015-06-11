package com.hearthsim.test;

import com.hearthsim.Game;
import com.hearthsim.card.minion.Hero;
import com.hearthsim.card.minion.heroes.TestHero;
import com.hearthsim.exception.HSException;
import com.hearthsim.model.PlayerModel;
import com.hearthsim.player.playercontroller.BruteForceSearchAI;
import com.hearthsim.util.DeckFactory;

public class RandomizedTesting {

    public static void main(String[] args) throws HSException 
    {
        DeckFactory factory = new DeckFactory.DeckFactoryBuilder().buildDeckFactory();
        Hero hero = new TestHero();
        runTest(factory, hero, 5.0);
    }
    
    public static int runTest(DeckFactory factory, Hero hero, double minutesToRun) throws HSException
    {
        int result = 0;
        long start = System.currentTimeMillis();
        BruteForceSearchAI ai = BruteForceSearchAI.buildStandardAI2();
        while((System.currentTimeMillis() - start) / 60_000.0 < minutesToRun)
        {
            PlayerModel model0 = new PlayerModel((byte) 0, "", hero.deepCopy(), factory.generateRandomDeck());
            PlayerModel model1 = new PlayerModel((byte) 1, "", hero.deepCopy(), factory.generateRandomDeck());
            Game testGame = new Game(model0, model1, ai.deepCopy(), ai.deepCopy());
            testGame.runGame();
            result++;
        }
        
        return result;
    }

}
