package com.hearthsim.arena;

import java.util.Random;

import com.hearthsim.card.ImplementedCardList.ImplementedCard;

public class StaticArenaAgent
{
    private static final Random Gen = new Random();
        
    protected StaticArenaAgent()
    {
    }

    public static ImplementedCard takeTurn(
            ImplementedCard first,
            ImplementedCard second,
            ImplementedCard third,
            DraftData arenaState)
            throws Exception 
    {
        int firstVal, secondVal, thirdVal;
        double curve1, curve2, curve3;
        firstVal = arenaState.arenaTierFunction.apply(first);
        secondVal = arenaState.arenaTierFunction.apply(second);
        thirdVal = arenaState.arenaTierFunction.apply(third);
        curve1 = Manacurve.getManaCurveValue(arenaState, first);
        curve2 = Manacurve.getManaCurveValue(arenaState, second);
        curve3 = Manacurve.getManaCurveValue(arenaState, third);
        
        if (firstVal < secondVal)
        {
            if (firstVal < thirdVal)
                return first;
            else if (firstVal > thirdVal)
                return third;
            else
            {
                if (curve1 < curve3)
                    return first;
                else if (curve1 > curve3)
                    return third;
                else
                    return StaticArenaAgent.selectRandomCard(first, third);
            }
        }
        else if (firstVal > secondVal)
        {
            if (secondVal < thirdVal)
                return second;
            else if (secondVal > thirdVal)
                return third;
            else
            {
                if (curve2 < curve3)
                    return second;
                else if (curve2 > curve3)
                    return third;
                else
                    return StaticArenaAgent.selectRandomCard(second, third);
            }
        }
        else
        {
            assert firstVal == secondVal;
            if (firstVal < thirdVal)
            {
                if (curve1 < curve2)
                    return first;
                else if (curve1 > curve2)
                    return second;
                else
                    return StaticArenaAgent.selectRandomCard(first, second);
            }
            else if (firstVal > thirdVal)
                return third;
            else
            {
                if (curve1 < curve2)
                {
                    if (curve1 < curve3)
                        return first;
                    else if (curve1 > curve3)
                        return third;
                    else
                        return StaticArenaAgent.selectRandomCard(first, third);
                }
                else if (curve1 > curve2)
                {
                    if (curve2 < curve3)
                        return second;
                    else if (curve2 > curve3)
                        return third;
                    else
                        return StaticArenaAgent.selectRandomCard(second, third);
                }
                else
                {
                    assert curve1 == curve2;
                    if (curve1 < curve3)
                        return StaticArenaAgent.selectRandomCard(first, second);
                    else if (curve1 > curve3)
                        return third;
                    else
                        return StaticArenaAgent.selectRandomCard(first, second, third);
                }
            }
        }
    }
    
    public static ImplementedCard selectRandomCard(ImplementedCard ... cards)
    {
        assert cards != null;
        assert cards.length != 0;
        
        return cards[StaticArenaAgent.Gen.nextInt(cards.length)];
    }
}
