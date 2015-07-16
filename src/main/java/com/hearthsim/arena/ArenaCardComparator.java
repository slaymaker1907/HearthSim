package com.hearthsim.arena;

import java.util.Comparator;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.hearthsim.Game;
import com.hearthsim.card.ImplementedCardList.ImplementedCard;
import com.hearthsim.model.PlayerModel;
import com.hearthsim.player.playercontroller.ArtificialPlayer;
import com.hearthsim.player.playercontroller.BruteForceSearchAI;

public class ArenaCardComparator implements Comparator<ImplementedCard>
{
    private static final ArtificialPlayer DefaultAI = BruteForceSearchAI.buildStandardAI1();
    private static final Random Gen = new Random();
    
    private final BiFunction<ArenaTurnFunction, ImplementedCard, BiFunction<Byte, String, PlayerModel>> lookaheadFunction;
    
    public ArenaCardComparator(BiFunction<ArenaTurnFunction, ImplementedCard, BiFunction<Byte, String, PlayerModel>> lookaheadFunction)
    {
        this.lookaheadFunction = lookaheadFunction;
    }
    
    @Override
    public int compare(ImplementedCard o1, ImplementedCard o2) 
    {
        Supplier<BiFunction<Byte, String, PlayerModel>> supp1, supp2;
        supp1 = () -> lookaheadFunction.apply(ArenaAgent::randomSelector, o1);
        supp2 = () -> lookaheadFunction.apply(ArenaAgent::randomSelector, o2);
        IndeterminateExecutor<Integer> executor = new IndeterminateExecutor<>();
        try {
            return executor.apply(new RunArenaGame(supp1, supp2));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    // Returns 1 if the first player won or 2 if the second player won.
    public static Integer runGame(BiFunction<Byte, String, PlayerModel> playerOne, BiFunction<Byte, String, PlayerModel> playerTwo)
    {
        Game cardGame = new Game(playerOne.apply((byte) 0, "player1"), playerTwo.apply((byte) 1, "player2"),
                ArenaCardComparator.DefaultAI.deepCopy(), ArenaCardComparator.DefaultAI.deepCopy(), ArenaCardComparator.Gen.nextInt(2));
        int winnerIndex = -2;
        try {
            winnerIndex = cardGame.runGame().winnerPlayerIndex_;
        } catch (Exception e) {
        }
        
        if (winnerIndex == 0 || winnerIndex == 1 || winnerIndex == -2)
            return winnerIndex + 1;
        else
            throw new RuntimeException("Winner index was not 1 or 2 or -1, it was: " + (winnerIndex + 1));
    }
    
    public static Integer runGameVersusRandom(BiFunction<Byte, String, PlayerModel> player) throws Exception
    {
        try
        {
            return runGame(player, ArenaGenerator.simulateArena(ArenaAgent::randomSelector));
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
