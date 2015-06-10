package com.hearthsim.gui.cardcomparator;

import java.util.HashMap;
import java.util.Random;

import com.hearthsim.card.Deck;
import com.hearthsim.card.ImplementedCardList.ImplementedCard;
import com.hearthsim.card.minion.Hero;
import com.hearthsim.exception.HSInvalidHeroException;
import com.hearthsim.model.PlayerModel;
import com.hearthsim.util.DeckFactory;
import com.hearthsim.util.HeroFactory;

public class PlayerModelFactory 
{
    private DeckFactory deckFactory;
    private Hero heroToUse;
    private byte playerId;
    private String playerName;
    private HashMap<String, DeckFactory> deckFactoriesOfAll;
    public static final String[] possibleHeroes = {"warlock", "priest", "mage", "rogue", "shaman", "paladin", "warrior", "hunter", "druid"};
    private Random gen = new Random();
    
    protected PlayerModelFactory(DeckFactory deckFactory, Hero heroToUse, byte playerId, String playerName)
    {
        // Hero may be null.
        this.heroToUse = heroToUse;
        this.deckFactory = deckFactory;
        this.playerId = playerId;
        this.playerName = playerName;
        
        if(heroToUse == null)
            deckFactoriesOfAll = generateAllFactories();
    }
    
    public PlayerModel createPlayer()
    {
        if (heroToUse != null)
            return new PlayerModel(playerId, playerName, heroToUse.deepCopy(), deckFactory.generateRandomDeck());
        else
        {
            String hero = possibleHeroes[gen.nextInt(possibleHeroes.length)];
            Deck deck = deckFactoriesOfAll.get(hero).generateRandomDeck();
            hero = hero.substring(0, 1).toUpperCase() + hero.substring(1, hero.length());
            try {
                return new PlayerModel(playerId, playerName, HeroFactory.getHero(hero), deck);
            } catch (HSInvalidHeroException e) {
                e.printStackTrace();
                System.err.println("PlayerModelFactory generated a random invalid hero '" + hero + "'.");
            }
            
            return null;
        }
    }
    
    private static HashMap<String, DeckFactory> generateAllFactories()
    {
        HashMap<String, DeckFactory> result = new HashMap<>();
        for(String hero : possibleHeroes)
        {
            DeckFactory.DeckFactoryBuilder builder = new DeckFactory.DeckFactoryBuilder();
            builder.filterByHero("neutral", hero);
            result.put(hero, builder.buildDeckFactory());
        }
        
        return result;
    }
    
    public static class Builder
    {
        private DeckFactory.DeckFactoryBuilder builder;
        private Hero heroToUse = null;
        private byte playerId = 0;
        private String playerName = "";
        
        public Builder()
        {
            builder = new DeckFactory.DeckFactoryBuilder();
        }
        
        public Builder includeCards(ImplementedCard ... cardsToInclude)
        {
            builder.includeSpecificCards(cardsToInclude);
            return this;
        }
        
        public Builder specifyHero(String hero)
        {
            builder.filterByHero("neutral", hero);
            hero = hero.substring(0, 1).toUpperCase() + hero.substring(1, hero.length());
            try {
                heroToUse = HeroFactory.getHero(hero);
            } catch (HSInvalidHeroException e) {
                throw new IllegalArgumentException(e);
            }
            return this;
        }
        
        public Builder specifyPlayerInfo(String name, byte Id)
        {
            this.playerId = Id;
            this.playerName = name;
            return this;
        }
        
        // This method is safe to call multiple times.
        public PlayerModelFactory createPlayerModelFactory()
        {
            DeckFactory factory = builder.buildDeckFactory();
            return new PlayerModelFactory(factory, heroToUse, playerId, playerName);
        }
    }
}
