package com.hearthsim.arena;

import com.hearthsim.card.ImplementedCardList.ImplementedCard;

public class ManaCurveAgent 
{
    public static ImplementedCard takeTurn(ImplementedCard first, ImplementedCard second,
            ImplementedCard third, DraftData currentState)
    {
        double firstVal, secondVal, thirdVal;
        firstVal = Manacurve.getManaCurveValue(currentState, first);
        secondVal = Manacurve.getManaCurveValue(currentState, second);
        thirdVal = Manacurve.getManaCurveValue(currentState, third);
        
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
}
