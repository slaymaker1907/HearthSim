package com.hearthsim.arena;

import java.util.Random;
import java.util.function.BiFunction;

import com.hearthsim.card.ImplementedCardList.ImplementedCard;
import com.hearthsim.model.PlayerModel;

public class ArenaAgent implements ArenaTurnFunction 
{
    private static final Random Gen = new Random();
    
    public static ImplementedCard randomSelector(ImplementedCard first, ImplementedCard second,
            ImplementedCard third, BiFunction<ArenaTurnFunction, ImplementedCard, BiFunction<Byte, String, PlayerModel>> lookaheadFunction)
    {
        // TODO Improve this in the future by giving it a cheap stateless function but one that doesn't make horrible choices.
        int selection = ArenaAgent.Gen.nextInt(3);
        switch(selection)
        {
        case 0:
            return first;
        case 1:
            return second;
        case 2:
            return third;
        default:
            throw new RuntimeException("Random number generator generated a number outside of 0 to 2 inclusive, the number was: " + selection);
        }
    }

    @Override
    public ImplementedCard takeTurn(
            ImplementedCard first,
            ImplementedCard second,
            ImplementedCard third,
            BiFunction<ArenaTurnFunction, ImplementedCard, BiFunction<Byte, String, PlayerModel>> lookaheadFunction)
            throws Exception 
    {
        ArenaCardComparator turn = new ArenaCardComparator(lookaheadFunction);
        if (turn.compare(first, second) > 0)
        {
            if (turn.compare(first, third) > 0)
                return first;
            else
                return third;
        }
        else if (turn.compare(second, third) > 0)
            return second;
        else
            return third;
    }
}
