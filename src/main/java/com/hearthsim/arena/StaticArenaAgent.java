package com.hearthsim.arena;

import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.hearthsim.card.ImplementedCardList.ImplementedCard;
import com.hearthsim.model.PlayerModel;

public class StaticArenaAgent implements ArenaTurnFunction
{
    private static final Random Gen = new Random();
    
    private final Function<ImplementedCard, Integer> tierFunction;
    
    public StaticArenaAgent(Function<ImplementedCard, Integer> tierFunction)
    {
        this.tierFunction = tierFunction;
    }

    @Override
    public ImplementedCard takeTurn(
            ImplementedCard first,
            ImplementedCard second,
            ImplementedCard third,
            BiFunction<ArenaTurnFunction, ImplementedCard, BiFunction<Byte, String, PlayerModel>> lookaheadFunction)
            throws Exception 
    {
        int firstVal, secondVal, thirdVal;
        firstVal = this.tierFunction.apply(first);
        secondVal = this.tierFunction.apply(second);
        thirdVal = this.tierFunction.apply(third);
        
        if (firstVal < secondVal)
        {
            if (firstVal < thirdVal)
                return first;
            else if (firstVal > thirdVal)
                return third;
            else
                return StaticArenaAgent.selectRandomCard(first, third);
        }
        else if (firstVal > secondVal)
        {
            if (secondVal < thirdVal)
                return second;
            else if (secondVal > thirdVal)
                return third;
            else
                return StaticArenaAgent.selectRandomCard(second, third);
        }
        else
        {
            assert firstVal == secondVal;
            if (firstVal < thirdVal)
                return StaticArenaAgent.selectRandomCard(first, second);
            else if (firstVal > thirdVal)
                return third;
            else
                return StaticArenaAgent.selectRandomCard(first, second, third);
        }
    }
    
    private static ImplementedCard selectRandomCard(ImplementedCard ... cards)
    {
        assert cards != null;
        assert cards.length != 0;
        
        return cards[StaticArenaAgent.Gen.nextInt(cards.length)];
    }
}
