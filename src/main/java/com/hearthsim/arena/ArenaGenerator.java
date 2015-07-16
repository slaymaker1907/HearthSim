package com.hearthsim.arena;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.hearthsim.card.Card;
import com.hearthsim.card.Deck;
import com.hearthsim.card.ImplementedCardList.ImplementedCard;
import com.hearthsim.card.minion.Hero;
import com.hearthsim.model.PlayerModel;
import com.hearthsim.util.DeckFactory;
import com.hearthsim.util.HeroFactory;
import com.hearthsim.util.immutable.ImmutableMap;

public class ArenaGenerator 
{
    private static final Random Gen = new Random();
    // TODO Change this to be an immutable list.
    private static final Set<String> Rarities = ArenaGenerator.queryPossibleAttributes((card) -> card.rarity_);
    private static final List<String> Heroes;
    static
    {
        Function<ImplementedCard, String> filterForHeroes = (card) -> ArenaGenerator.ensureCapitalization(card.charClass_);
        ArrayList<String> allHeroes = new ArrayList<String>(ArenaGenerator.queryPossibleAttributes(filterForHeroes));
        allHeroes.removeIf((hero) -> hero.equals("Neutral"));
        Heroes = Collections.unmodifiableList(allHeroes);
    }
    
    public static String ensureCapitalization(String toCapitalize)
    {
        return toCapitalize.substring(0, 1).toUpperCase() + toCapitalize.substring(1, toCapitalize.length());
    }
    
    public static String unCapitalize(String toUnCapitalize)
    {
        return toUnCapitalize.substring(0, 1).toLowerCase() + toUnCapitalize.substring(1, toUnCapitalize.length());
    }
    
    private static ArrayList<ImplementedCard> getCardsOfRarity(String hero, String ... rarities)
    {
        String lowerCaseHero = ArenaGenerator.unCapitalize(hero);
        return new DeckFactory.DeckFactoryBuilder().filterByRarity(rarities).filterByHero(lowerCaseHero, "neutral").buildDeckFactory().getAllPossibleCards();
    }
    
    private static Set<String> queryPossibleAttributes(Function<ImplementedCard, String> query)
    {
        DeckFactory gen = new DeckFactory.DeckFactoryBuilder().buildDeckFactory();
        HashSet<String> attributeState = new HashSet<>();
        ArrayList<ImplementedCard> cards = gen.getAllPossibleCards();
        for(ImplementedCard card : cards)
            attributeState.add(query.apply(card));
        return Collections.unmodifiableSet(attributeState);
    }
    
    private static Double getTurnProbability(String rarity, int turnCount)
    {
        if (turnCount < 1 || turnCount > 30)
            throw new IllegalArgumentException("Turn count must be beween 1 and 30 inclusive, input: " + turnCount);
        if (turnCount != 10 && turnCount != 20 && turnCount != 30)
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
            case "common":
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
    
    public static BiFunction<Byte, String, PlayerModel> simulateArena(ArenaTurnFunction agent) throws Exception
    {
        String hero = ArenaGenerator.Heroes.get(ArenaGenerator.Gen.nextInt(ArenaGenerator.Heroes.size()));
        return ArenaGenerator.simulateArena(agent, new ArrayList<ImplementedCard>(), hero, true);
    }
    
    public static BiFunction<Byte, String, PlayerModel> simulateArena(ArenaTurnFunction agent, String hero, boolean runQuietly) throws Exception
    {
        return ArenaGenerator.simulateArena(agent, new ArrayList<ImplementedCard>(), hero, runQuietly);
    }
    
    public static BiFunction<Byte, String, PlayerModel> simulateArena(ArenaTurnFunction agent, ArrayList<ImplementedCard> cardsToInclude, String heroName, boolean runQuietly) throws Exception
    {
        String upperCaseHero = ArenaGenerator.ensureCapitalization(heroName);
        if (!ArenaGenerator.Heroes.contains(upperCaseHero))
            throw new IllegalArgumentException(heroName + " is not a valid hero.");
        
        ArrayList<ImplementedCard> cardsToUse = ArenaGenerator.simulateArenaList(agent, new ArrayList<ImplementedCard>(), upperCaseHero, runQuietly);
        Function<ImplementedCard, Card> cardToCard = (impCard) -> impCard.createCardInstance();
        Deck playerDeck = new Deck(convertCollection(cardsToUse, cardToCard));
        Hero hero = HeroFactory.getHero(upperCaseHero);
        
        return (playerId, playerName) -> new PlayerModel(playerId, playerName, hero.deepCopy(), playerDeck.deepCopy());
    }
    
    private static <InputT, OutputT> ArrayList<OutputT> convertCollection(ArrayList<InputT> inputList, Function<InputT, OutputT> mappingFunction)
    {
        ArrayList<OutputT> result = new ArrayList<>();
        for(InputT input : inputList)
            result.add(mappingFunction.apply(input));
        return result;
    }
    
    private static ArrayList<ImplementedCard> simulateArenaList(ArenaTurnFunction agent, ArrayList<ImplementedCard> cardsToInclude, String hero, boolean runQuietly) throws Exception
    {
        if (cardsToInclude.size() > 30)
            throw new IllegalArgumentException("Number of cards to include must be < 30. Actual: " + cardsToInclude.size());
        Function<String, ArrayList<ImplementedCard>> rarityFunction = ArenaGenerator.getRarityFunction(hero);
            
        ArrayList<ImplementedCard> result = new ArrayList<>(cardsToInclude);
        for(int turnCount = cardsToInclude.size() + 1; turnCount <= 30; turnCount++)
        {
            String rarity = getTurnRarity(turnCount);
            ImplementedCard[] cardTriple = ArenaGenerator.getCardTriple(rarityFunction.apply(rarity));
            BiFunction<ArenaTurnFunction, ImplementedCard, BiFunction<Byte, String, PlayerModel>> lookaheadFunction = (newAgent, cardSelected) ->
            {
                ArrayList<ImplementedCard> resultCopy = new ArrayList<>(result);
                resultCopy.add(cardSelected);
                try {
                    return ArenaGenerator.simulateArena(newAgent, resultCopy, hero, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                return null;
            };
            
            if (!runQuietly)
                System.out.println("Selecting from: " + cardTriple[0].name_ + ", " + cardTriple[1].name_ + ", and " + cardTriple[2].name_);
            ImplementedCard selectedCard = agent.takeTurn(cardTriple[0], cardTriple[1], cardTriple[2], lookaheadFunction);
            if (!runQuietly)
                System.out.println("\tSelected : " + selectedCard.name_);
            result.add(selectedCard);
        }
        
        return result;
    }
        
    private static Function<String, ArrayList<ImplementedCard>> getRarityFunction(String hero)
    {
        String lowerCaseHero = ArenaGenerator.unCapitalize(hero);
        Function<String, ArrayList<ImplementedCard>> expensiveMapFunction = (rarity) ->
        {
            switch(rarity)
            {
            case "free":
            case "common":
                return ArenaGenerator.getCardsOfRarity(lowerCaseHero, "free", "common");
            default:
                return ArenaGenerator.getCardsOfRarity(lowerCaseHero, rarity);
            }
        };
        
        ImmutableMap<String, ArrayList<ImplementedCard>> functionMap = new ImmutableMap<>(ArenaGenerator.Rarities, expensiveMapFunction);
        
        return (rarity) -> functionMap.get(rarity);
    }
    
    private static ImplementedCard[] getCardTriple(ArrayList<ImplementedCard> cardsToChooseFrom)
    {
        HashSet<ImplementedCard> cardsInResult = new HashSet<ImplementedCard>();
        
        while(cardsInResult.size() < 3)
        {
            cardsInResult.add(cardsToChooseFrom.get(ArenaGenerator.Gen.nextInt(cardsToChooseFrom.size())));
        }
        
        if (cardsInResult.size() != 3)
            throw new RuntimeException("The size of an arena hand should always be three, actual: " + cardsInResult.size());
        
        return cardsInResult.toArray(new ImplementedCard[0]);
    }
    
    private static String getTurnRarity(int turnCount)
    {
        double roll = ArenaGenerator.Gen.nextDouble();
        double runningTotal = 0.0;
        
        for(String rarity : ArenaGenerator.Rarities)
        {
            if (rarity.equals("free"))
                continue;
            
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
