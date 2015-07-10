package com.hearthsim.arena;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import com.hearthsim.card.ImplementedCardList.ImplementedCard;
import com.hearthsim.util.DeckFactory;
import com.hearthsim.util.immutable.ImmutableMap;

public class ArenaGenerator 
{
    public static final ImmutableMap<String, ArrayList<ImplementedCard>> CardsByRarity;
    private static final Random Gen = new Random();
    // TODO Change this to be an immutable list.
    private static final HashSet<String> Rarities;
    
    static
    {
        Rarities = ArenaGenerator.getAllRarities();
        CardsByRarity = new ImmutableMap<>(Rarities, ArenaGenerator::getCardsOfRarity);
    }
    
    private static ArrayList<ImplementedCard> getCardsOfRarity(String ... rarities)
    {
        return new DeckFactory.DeckFactoryBuilder().filterByRarity(rarities).buildDeckFactory().getAllPossibleCards();
    }
    
    private static Double getTurnProbability(String rarity, int turnCount)
    {
        if (turnCount < 1 || turnCount > 30)
            throw new IllegalArgumentException("Turn count must be beween 1 and 30 inclusive, input: " + turnCount);
        if (turnCount == 10 || turnCount == 20 || turnCount == 30)
        {
            switch(rarity)
            {
            case "free":
            case "common":
                return 427.0 / 468.0;
            case "rare":
                return 34.0 / 468.0;
            case "epic":
                return 3.0 / 468.0;
            case "legendary":
                return 4.0 / 468.0;
            default:
                throw new IllegalArgumentException(rarity + " is not a valid card rarity.");
            }
        }
        else
        {
            switch(rarity)
            {
            case "free":
            case "common:":
                return 0.0;
            case "rare":
                return 57.0 / 72.0;
            case "epic":
                return 9.0 / 72.0;
            case "legendary":
                return 6.0 / 72.0;
            default:
                throw new IllegalArgumentException(rarity + " is not a valid card rarity.");
            }
        }
    }
    
    private static HashSet<String> getAllRarities()
    {
        DeckFactory gen = new DeckFactory.DeckFactoryBuilder().buildDeckFactory();
        HashSet<String> rarities = new HashSet<>();
        ArrayList<ImplementedCard> cards = gen.getAllPossibleCards();
        for(ImplementedCard card : cards)
            rarities.add(card.rarity_);
        return rarities;
    }
    
    public static ArrayList<ImplementedCard> simulateArena(ArenaTurnFunction agent)
    {
        return ArenaGenerator.simulateArena(agent, new ArrayList<ImplementedCard>());
    }
    
    public static ArrayList<ImplementedCard> simulateArena(ArenaTurnFunction agent, ArrayList<ImplementedCard> cardsToInclude)
    {
        if (cardsToInclude.size() > 30)
            throw new IllegalArgumentException("Number of cards to include must be < 30. Actual: " + cardsToInclude.size());
            
        ArrayList<ImplementedCard> result = new ArrayList<>(cardsToInclude);
        for(int turnCount = cardsToInclude.size() + 1; turnCount <= 30; turnCount++)
        {
            String rarity = getTurnRarity(turnCount);
            ImplementedCard[] cardTriple = ArenaGenerator.getCardTriple(rarity);
            result.add(agent.takeTurn(cardTriple[0], cardTriple[1], cardTriple[2]));
        }
        
        return result;
    }
    
    public static ImplementedCard[] getCardTriple(String rarity)
    {
        HashSet<ImplementedCard> cardsInResult = new HashSet<ImplementedCard>();
        ArrayList<ImplementedCard> cardsToChooseFrom = ArenaGenerator.CardsByRarity.get(rarity);
        while(cardsInResult.size() < 3)
        {
            cardsInResult.add(cardsToChooseFrom.get(ArenaGenerator.Gen.nextInt(cardsToChooseFrom.size())));
        }
        
        if (cardsInResult.size() != 3)
            throw new RuntimeException("The size of an arena hand should always be three, actual: " + cardsInResult.size());
        
        return (ImplementedCard[])cardsInResult.toArray();
    }
    
    public static String getTurnRarity(int turnCount)
    {
        double roll = ArenaGenerator.Gen.nextDouble();
        double runningTotal = 0.0;
        
        for(String rarity : ArenaGenerator.Rarities)
        {
            runningTotal += ArenaGenerator.getTurnProbability(rarity, turnCount);
            if (runningTotal >= roll)
                return rarity;
        }
        
        throw new RuntimeException("Could not correctly roll for Arena turn rarity.");
    }
    
    protected ArenaGenerator()
    {
        // This is a static class.
    }
}
