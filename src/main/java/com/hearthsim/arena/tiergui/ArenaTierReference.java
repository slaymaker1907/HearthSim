package com.hearthsim.arena.tiergui;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

import javax.swing.JOptionPane;

import com.hearthsim.arena.ArenaGenerator;
import com.hearthsim.arena.tiergui.MainGUI.HeroAndCard;
import com.hearthsim.card.ImplementedCardList.ImplementedCard;

@SuppressWarnings("unchecked")
public class ArenaTierReference 
{
    private static final HashMap<HeroAndCard, Integer> data = ArenaTierReference.getData();
    private static final String nameOfFile = "ArenaTierReference.ser";
    
    private static HashMap<HeroAndCard, Integer> getData()
    {
        HashMap<HeroAndCard, Integer> tempData = null;
        try
        {
            tempData = (HashMap<HeroAndCard, Integer>)ArenaTierReference.getObjectFromFile(ArenaTierReference.nameOfFile);
        }
        catch (Exception e)
        {
            System.out.println("Could not initialize ArenaTierReference with existing data.");
            tempData = new HashMap<>();
        }
        
        assert tempData != null;
        return tempData;
    }
    
    public static Boolean hasData(HeroAndCard card)
    {
        return ArenaTierReference.data.containsKey(card);
    }
    
    public static void addLocalData(HeroAndCard card, int arenaTier)
    {
        ArenaTierReference.data.put(card, arenaTier);
        ArenaTierReference.saveData();
    }
    
    public static void saveData()
    {
        try
        {
            ArenaTierReference.saveObjectToFile(ArenaTierReference.data, ArenaTierReference.nameOfFile);
        }
        catch (Exception e)
        {
            System.out.println("Could not save ArenaTierReference");
            e.printStackTrace();
        }
    }
    
    public static void saveObjectToFile(Serializable object, String fileName) throws Exception
    {
        final FileOutputStream fileOut = new FileOutputStream(fileName);
        final ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
        objectOut.writeObject(object);
        objectOut.close();
        fileOut.close();
    }
    
    public static Object getObjectFromFile(String fileName) throws Exception
    {
        final FileInputStream fileIn = new FileInputStream(fileName);
        final ObjectInputStream objectIn = new ObjectInputStream(fileIn);
        Object result = objectIn.readObject();
        objectIn.close();
        fileIn.close();
        return result;
    }
    
    public static Boolean dataComplete()
    {
        ArrayList<HeroAndCard> missingCards = new ArrayList<HeroAndCard>();
        for(String hero : MainGUI.getAllHeroes())
        {
            ArrayList<HeroAndCard> allCardsOfHero = ArenaGenerator
                    .convertCollection(MainGUI.getHeroMap().apply(hero), HeroAndCard.getMap(hero));
            allCardsOfHero.removeIf((card) -> ArenaTierReference.hasData(card));
            missingCards.addAll(allCardsOfHero);
        }
        
        return missingCards.isEmpty();
    }
    
    public static Function<HeroAndCard, Integer> getValueFunction()
    {
        if (!ArenaTierReference.dataComplete())
            throw new IllegalArgumentException("Arena tier information is incomplete.");
        return ArenaTierReference.data::get;
    }
    
    public static Function<ImplementedCard, Integer> getHeroFunction(String hero)
    {
        Function<ImplementedCard, HeroAndCard> cardToHeroAndCard = HeroAndCard.getMap(hero);
        return (card) -> ArenaTierReference.getSafeValueFunction().apply(cardToHeroAndCard.apply(card));
    }
    
    public static Integer getTierRank(HeroAndCard card)
    {
        return ArenaTierReference.data.get(card);
    }
    
    private static Integer queryUserForTier(HeroAndCard card)
    {
        String message = "Hero: " + card.hero + " Card: " + card.cardName + ":" +
                MainGUI.getRarity(card);
        String input = "";
        Integer result;
        while(true)
        {
            input = JOptionPane.showInputDialog(message);
            try
            {
                result = Integer.parseInt(input);
                // If it gets to here, result is initialized properly.
                break;
            }
            catch (Exception e)
            {
            }
        }
        
        return result;
    }
    
    public static Function<HeroAndCard, Integer> getSafeValueFunction()
    {
        return (card) ->
        {
            if (!ArenaTierReference.data.containsKey(card))
            {
                ArenaTierReference.data.put(card, ArenaTierReference.queryUserForTier(card));
                ArenaTierReference.saveData();
            }
            
            return ArenaTierReference.data.get(card);
        };
    }
}
