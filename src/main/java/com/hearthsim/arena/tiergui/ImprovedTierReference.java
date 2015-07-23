package com.hearthsim.arena.tiergui;

import java.util.List;
import java.util.function.Function;
import java.io.Serializable;

import com.hearthsim.arena.ArenaGenerator;
import com.hearthsim.arena.Program;
import com.hearthsim.card.ImplementedCardList.ImplementedCard;
import com.hearthsim.util.DeckFactory;
import com.hearthsim.util.immutable.ImmutableMap;

public class ImprovedTierReference 
{
    private static final ImmutableMap<String, ImmutableMap<ImplementedCard, Integer>> getTierData = initializeFunction();
    private static final String fileName = "ImprovedArenaTierReference.ser";
    
    @SuppressWarnings("unchecked")
    private static final ImmutableMap<String, ImmutableMap<ImplementedCard, Integer>> initializeFunction()
    {
        try
        {
            return (ImmutableMap<String, ImmutableMap<ImplementedCard, Integer>>)ArenaTierReference.getObjectFromFile(ImprovedTierReference.fileName);
        }
        catch (Exception e)
        {
            System.out.println("Could not initialize tier data from file.");
        }
        try
        {
            ImmutableMap<String, ImmutableMap<ImplementedCard, Integer>> result = new ImmutableMap<>(ArenaGenerator.Heroes, ImprovedTierReference::getMapForHero);

            ImprovedTierReference.saveData(result);
            return result;
        }
        catch (Exception e)
        {
            System.out.println("Could not properly initialize tier data.");
            e.printStackTrace();
            return null;
        }
    }
    
    public static ImmutableMap<ImplementedCard, Integer> getMapForHero(String hero)
    {
        List<ImplementedCard> allCards = new DeckFactory.DeckFactoryBuilder().filterByHero("neutral", ArenaGenerator.unCapitalize(hero)).buildDeckFactory().getAllPossibleCards();
        Function<ImplementedCard, Integer> heroFunc;
        try {
            Thread.sleep(1000);
            heroFunc = ExtractArenaWebData.parseHero(hero).getTier;
            Program.verifyWebData(ArenaGenerator.unCapitalize(hero));
        } catch (Exception e) {
            System.out.println("Could not get data for: " + hero);
            e.printStackTrace();
            heroFunc = null;
        }
        assert heroFunc != null;
        return new ImmutableMap<>(allCards, heroFunc);
    }
    
    public static int getTierData(String hero, ImplementedCard card)
    {
        assert ImprovedTierReference.getTierData != null;
        return ImprovedTierReference.getTierData.get(ArenaGenerator.ensureCapitalization(hero)).get(card);
    }
    
    private static void saveData(ImmutableMap<String, ImmutableMap<ImplementedCard, Integer>> data) throws Exception
    {
        ArenaTierReference.saveObjectToFile((Serializable)data, ImprovedTierReference.fileName);
    }
}
