package com.hearthsim.arena;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.hearthsim.card.ImplementedCardList.ImplementedCard;
import com.hearthsim.util.immutable.ImmutableMap;

public class Manacurve 
{
    public static final Set<Integer> ManaGroups = 
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList(new Integer[]{2, 3, 4, 5, 6, 7})));
    public static final BiFunction<DraftData, Integer, Double> getExpectedGroupCount =
            Manacurve.getFastExpectedCountFunction();
    
    public static int idealGroupCurve(int manaCostGroup)
    {
        if (manaCostGroup < 0 || manaCostGroup > 7)
            throw new IllegalArgumentException(String.valueOf(manaCostGroup));
        
        switch(manaCostGroup)
        {
        case 2:
            return 10;
        case 3:
            return 5;
        case 4:
        case 5:
        case 6:
            return 4;
        default:
            return 3;
        }
    }
    
    public static int getManaCostGroup(int manaCost)
    {
        if (manaCost < 0)
            throw new IllegalArgumentException(String.valueOf(manaCost));
        
        switch(manaCost)
        {
        case 0:
        case 1:
        case 2:
            return 2;
        case 3:
            return 3;
        case 4:
            return 4;
        case 5:
            return 5;
        case 6:
            return 6;
        default:
            return 7;
        }
    }
    
    private static BiFunction<String, Integer, Double> getCountOfManaGroupRarity(String hero)
    {
        Function<String, ArrayList<ImplementedCard>> rarityFunction = ArenaGenerator.getRarityFunction(hero);
        Set<String> allRarities = ArenaGenerator.queryPossibleAttributes((card) -> card.rarity_);
        ImmutableMap<String, Function<Integer, Double>> mapFunction =
                new ImmutableMap<>(allRarities, (rarity) -> 
                Manacurve.accumlateCards(rarityFunction.apply(rarity)));
        return (rarity, manaCurveGroup) -> mapFunction.get(rarity).apply(manaCurveGroup);
    }
    
    private static Function<Integer, Double> accumlateCards(List<ImplementedCard> cards)
    {
        Function<Integer, Integer> cardCount = Manacurve.accumlateCardsCount(cards);
        
        ImmutableMap<Integer, Double> functionMap = new ImmutableMap<>(Manacurve.ManaGroups, (manaGroup) ->
                Double.valueOf(cardCount.apply(manaGroup) / Double.valueOf(cards.size())));
        return (manaGroup) -> functionMap.get(manaGroup);
    }
    
    private static Function<Integer, Integer> accumlateCardsCount(List<ImplementedCard> cards)
    {
        HashMap<Integer, Integer> manaGroupCount = new HashMap<>();
        for(ImplementedCard card : cards)
        {
            manaGroupCount.merge(Manacurve.getManaCostGroup(card.mana_), 1, (oldValue, newValue) -> oldValue + newValue);
        }
        
        return (manaGroup) -> manaGroupCount.getOrDefault(manaGroup, 0);
    }
    
    public static Function<Integer, Double> getExpectedCountFunction(Function<Integer, Integer> currentCount,
           int turnCount, String hero)
    {
        BiFunction<String, Integer, Double> manaGroupRarity = Manacurve.getCountOfManaGroupRarity(hero);
        Set<String> rarities = ArenaGenerator.queryPossibleAttributes((card) -> card.rarity_);
        return (manaGroup) ->
        {
            double expectedValue = currentCount.apply(manaGroup);
            for(int i = turnCount; i <= 30; i++)
            {
                for(String rarity : rarities)
                {
                    expectedValue += ArenaGenerator.getTurnProbability(rarity, i) * 
                            manaGroupRarity.apply(rarity, manaGroup);
                }
            }
            
            return expectedValue;
        };
    }
    
    // Lower is better.
    public static double getManaCurveValue(DraftData currentState, ImplementedCard cardToAdd)
    {
        ArrayList<ImplementedCard> currentCards = new ArrayList<>(currentState.cardsInDeck);
        currentCards.add(cardToAdd);
        
        Function<Integer, Integer> currentCount = Manacurve.accumlateCardsCount(currentCards);
        double distanceFromIdeal = 0;
        for(Integer manaGroup : Manacurve.ManaGroups)
        {
            double expectedValue =
                    Manacurve.getExpectedGroupCount.apply(currentState, manaGroup) + currentCount.apply(manaGroup);
            distanceFromIdeal += Math.abs(expectedValue - Manacurve.idealGroupCurve(manaGroup));
        }
        return distanceFromIdeal;
    }
    
    private static BiFunction<DraftData, Integer, Double> getFastExpectedCountFunction()
    {
        Set<String> rarities = ArenaGenerator.queryPossibleAttributes((card) -> card.rarity_);
        ArrayList<String> heroes = ArenaGenerator.convertCollection(ArenaGenerator.Heroes, ArenaGenerator::unCapitalize);
        ImmutableMap<String, BiFunction<String, Integer, Double>> heroRarityMap =
                new ImmutableMap<>(heroes, Manacurve::getCountOfManaGroupRarity);
        ImmutableMap<String, HashMap<Integer, ImmutableMap<Integer, Double>>> finalFunction = 
                new ImmutableMap<String, HashMap<Integer, ImmutableMap<Integer, Double>>>(heroes, (hero) ->
                {
                    HashMap<Integer, ImmutableMap<Integer, Double>> result = new HashMap<>();
                    result.put(0, new ImmutableMap<>(Manacurve.ManaGroups, (manaGroup) -> 0.0));
                    result.put(31, new ImmutableMap<>(Manacurve.ManaGroups, (manaGroup) -> 0.0));
                    for(int turnCount = 1; turnCount <= 30; turnCount++)
                    {
                        ImmutableMap<Integer, Double> lastMap = result.get(turnCount - 1);
                        Function<String, Double> getProb = Manacurve.getRarityProbability(turnCount);
                        Function<Integer, Double> mapFunction = (manaGroup) -> 
                        {
                            double expectedValue = lastMap.get(manaGroup);
                            for(String rarity : rarities)
                            {
                                expectedValue += getProb.apply(rarity) * 
                                        heroRarityMap.get(hero).apply(rarity, manaGroup);
                            }
                            return expectedValue;
                        };
                        ImmutableMap<Integer, Double> newMap  = new ImmutableMap<>(Manacurve.ManaGroups, mapFunction);
                        result.put(turnCount, newMap);
                    }
                    return result;
                });
        return (arenaState, manaGroup) ->
        {
            int turnCount = arenaState.getTurnCount() + 1;
            String hero = arenaState.hero;
            HashMap<Integer, ImmutableMap<Integer, Double>> bigMap = finalFunction.get(hero);
            ImmutableMap<Integer, Double> smallMap = bigMap.get(turnCount);
            return smallMap.get(manaGroup);
        };
    }
    
    private static Function<String, Double> getRarityProbability(int turn)
    {
        return (rarity) -> ArenaGenerator.getTurnProbability(rarity, turn);
    }
}