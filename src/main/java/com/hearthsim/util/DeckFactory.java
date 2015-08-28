package com.hearthsim.util;

import com.hearthsim.card.Card;
import com.hearthsim.card.Deck;
import com.hearthsim.card.ImplementedCard;
import com.hearthsim.card.ImplementedCardList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Predicate;

/**
 * This class provides a mechanism for generating random decks.
 *
 * @author dyllonmgagnier
 *
 */
public class DeckFactory {
    private ArrayList<ImplementedCard> cards;
    private boolean limitCopies;
    private Random gen;
    private ImplementedCard[] cardsToInclude;

    /**
     * This method initializes a new DeckFactory.
     *
     * @param filter
     *            Any card for which this returns true will be removed from the
     *            potential card pool.
     * @param limitCopies
     *            If true, then any deck will contain no more than two copies of
     *            any card no more than one copy of any legendary.
     */
    protected DeckFactory(Predicate<ImplementedCard> filter,
            boolean limitCopies, ImplementedCard[] cardsToInclude) {
        cards = new ArrayList<>(ImplementedCardList.getInstance().getCardList());
        cards.removeIf(filter);
        gen = new Random();
        this.limitCopies = limitCopies;
        this.cardsToInclude = cardsToInclude;
    }

    /**
     *
     * @return All possible cards that could be in a deck generated by this
     *         class as specified by the DeckFactoryBuilder.
     */
    public ArrayList<ImplementedCard> getAllPossibleCards() {
        return new ArrayList<ImplementedCard>(cards);
    }

    /**
     * This method generates a new random deck as specified by the builder. The
     * decks are completely random so shuffling is unnecessary.
     *
     * @return
     */
    public Deck generateRandomDeck() {
        Card[] result = new Card[30];
        int resultPos = 0;

        // Add in cardsToInclude
        for (ImplementedCard cardToInclude : cardsToInclude)
            result[resultPos++] = cardToInclude.createCardInstance();

        if (limitCopies) {
            HashMap<ImplementedCard, Integer> cardsInDeck = new HashMap<ImplementedCard, Integer>();

            // Insert included cards into HashMap.
            for (ImplementedCard cardToInclude : cardsToInclude) {
                if (cardsInDeck.containsKey(cardToInclude))
                    cardsInDeck.put(cardToInclude,
                            cardsInDeck.get(cardToInclude) + 1);
                else
                    cardsInDeck.put(cardToInclude, 1);
            }

            while (resultPos < 30) {
                ImplementedCard toAdd;
                // Keep going until a card is found that can be added to the
                // deck.
                while (true) {
                    toAdd = cards.get(gen.nextInt(cards.size()));
                    if (!cardsInDeck.containsKey(toAdd)) {
                        cardsInDeck.put(toAdd, 1);
                        break;
                    } else if (cardsInDeck.get(toAdd).equals(1)
                            && !toAdd.rarity_.equals("legendary")) {
                        cardsInDeck.put(toAdd, 2);
                        break;
                    }
                }
                result[resultPos++] = toAdd.createCardInstance();
            }
        } else {
            while (resultPos < 30) {
                result[resultPos++] = cards.get(gen.nextInt(cards.size()))
                        .createCardInstance();
            }
        }

        Deck deckResult = new Deck(result);
        deckResult.shuffle();
        return deckResult;
    }

    /**
     * This class builds a DeckFactory and allows for various options to be
     * selected for the factory.
     *
     * @author dyllonmgagnier
     *
     */
    public static class DeckFactoryBuilder {
        private Predicate<ImplementedCard> filter;
        private boolean limitCopies;
        private boolean allowUncollectible;
        private ImplementedCard[] cardsToInclude;

        /**
         * Constructs the default builder which does not allow for uncollectible
         * cards and will limit the number of copies of any card to no more than
         * two and limits the number of copies any particular legendary to no
         * more than one. Each method of the builder should only be called once
         * and successive calls will produce unspecified behavior.
         *
         * All of the methods return this DeckFactoryBuilder so that they can be
         * chained together if so desired or called one at a time.
         */
        public DeckFactoryBuilder() {
            filter = (card) -> false;
            limitCopies = true;
            allowUncollectible = false;
            cardsToInclude = new ImplementedCard[0];
        }

        /**
         * Limits the the card pool to only those specified by the given
         * rarities. This method will throw a NullPointerException if any of the
         * input rarities is null.
         *
         * @param rarities
         */
        public DeckFactoryBuilder filterByRarity(String... rarities) {
            // Validate input.
            for (String rarity : rarities)
                if (rarity == null)
                    throw new NullPointerException(
                            "One of the input rarities was null.");

            filter = filter.or((card) -> {
                boolean result = true;
                if (card.rarity_ == null)
                    return true;
                for (String rarity : rarities)
                    result = result && !card.rarity_.equals(rarity);
                return result;
            });
            return this;
        }

        /**
         * Only select cards usable by the input character class (i.e. warlock,
         * priest, mage, rogue, etc.). As a note, if neutral cards are also
         * desired, "neutral" must be included as an argument.
         *
         * @param characterClass
         *            The classes to filter by.
         */
        public DeckFactoryBuilder filterByHero(String... characterClasses) {
            filter = filter.or((card) -> {
                boolean result = true;
                for (String characterClass : characterClasses)
                    result = result && !card.charClass_.equals(characterClass);
                return result;
            });

            return this;
        }

        /**
         * This method allows for uncollectible cards to be in the card pool.
         */
        public DeckFactoryBuilder allowUncollectible() {
            allowUncollectible = true;
            return this;
        }

        /**
         * This method generates a DeckFactory based on the previously selected
         * options.
         *
         * @return A DeckFactory limited by the various options.
         */
        public DeckFactory buildDeckFactory() {
            if (!allowUncollectible)
                filter = filter.or((card) -> !card.collectible).or((card) -> card.isHero);
            return new DeckFactory(filter, limitCopies, cardsToInclude);
        }

        /**
         * This method only allows for cards between the minimum and maximum
         * mana cost.
         *
         * @param minimumCost
         *            The minimum mana cost allowed.
         * @param maximumCost
         *            The maximum mana cost allowed.
         */
        public DeckFactoryBuilder filterByManaCost(int minimumCost,
                int maximumCost) {
            filter = filter.or((card) -> card.mana_ < minimumCost
                    || card.mana_ > maximumCost);
            return this;
        }

        /**
         * This method allows for unlimited copies of cards to be used (i.e.
         * like in Arena).
         */
        public DeckFactoryBuilder allowUnlimitedCopiesOfCards() {
            limitCopies = false;

            return this;
        }

        /**
         * This method guarantees that the input cards will be included in any
         * generated deck. However, the number of cards that are to be included
         * should be less than 30.
         *
         * @param cardsToInclude
         */
        public DeckFactoryBuilder includeSpecificCards(
                ImplementedCard... cardsToInclude) {
            this.cardsToInclude = cardsToInclude;
            return this;
        }
    }
}
