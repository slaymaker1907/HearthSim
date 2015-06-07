package com.hearthsim.gui.cardcomparator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

import com.hearthsim.card.ImplementedCardList;
import com.hearthsim.card.ImplementedCardList.ImplementedCard;
import com.hearthsim.card.minion.Hero;
import com.hearthsim.exception.HSInvalidHeroException;
import com.hearthsim.util.HeroFactory;

public class GenerateDecks
{
	private final Random gen;
	private final HashMap<String, ArrayList<ImplementedCardList.ImplementedCard>> cardsByCharClass;
	private final String[] heroList = { "Druid", "Hunter", "Mage", "Paladin",
			"Priest", "Rogue", "Shaman", "Warlock", "Warrior" };
	private boolean hasHero;
	private String hero;

	public GenerateDecks()
	{
		this.gen = new Random();
		final ArrayList<ImplementedCardList.ImplementedCard> cards = ImplementedCardList
				.getInstance().getCardList();
		cards.removeIf(new IsNotPlayable());
		this.cardsByCharClass = new HashMap<String, ArrayList<ImplementedCardList.ImplementedCard>>();

		for (final ImplementedCardList.ImplementedCard card : cards)
		{
			if (!this.cardsByCharClass.containsKey(card.charClass_))
				this.cardsByCharClass.put(card.charClass_,
						new ArrayList<ImplementedCardList.ImplementedCard>());
			this.cardsByCharClass.get(card.charClass_).add(card);
		}

		final Set<String> charClasses = this.cardsByCharClass.keySet();
		final ArrayList<ImplementedCardList.ImplementedCard> neutralCards = this.cardsByCharClass
				.get("neutral");
		for (final String charClass : charClasses)
		{
			this.cardsByCharClass.get(charClass).addAll(neutralCards);
		}

		this.hero = "";
		this.hasHero = false;
	}

	public GenerateDecks(final String hero)
	{
		this();
		this.hasHero = true;
		this.hero = hero;
	}

	public ArrayList<ImplementedCardList.ImplementedCard> getCardsByCharClass(
			String charClass)
			{
		charClass = charClass.toLowerCase();
		return new ArrayList<ImplementedCardList.ImplementedCard>(
				this.cardsByCharClass.get(charClass));
			}

	public DeckAndHero randomDeck() throws HSInvalidHeroException
	{
		final ArrayList<ImplementedCard> resultD = new ArrayList<ImplementedCard>();

		String hero;
		if (this.hasHero)
			hero = this.hero;
		else
			hero = this.heroList[this.gen.nextInt(this.heroList.length)];

		final Hero heroToUse = HeroFactory.getHero(hero);
		hero = hero.toLowerCase();

		final ArrayList<ImplementedCardList.ImplementedCard> possibleCards = this.cardsByCharClass
				.get(hero);

		final HashMap<ImplementedCardList.ImplementedCard, Integer> cardsInDeck = new HashMap<ImplementedCardList.ImplementedCard, Integer>();
		for (int i = 0; i < 30; i++)
		{
			ImplementedCardList.ImplementedCard toAdd;

			while (true)
			{
				toAdd = possibleCards
						.get(this.gen.nextInt(possibleCards.size()));
				if (!cardsInDeck.containsKey(toAdd))
					break;
				if (!toAdd.rarity_.equals("legendary")
						&& cardsInDeck.get(toAdd) < 2)
					break;
			}

			resultD.add(toAdd);
		}

		final DeckAndHero result = new DeckAndHero(heroToUse, resultD);

		return result;
	}

	public DeckAndHero mostlyRandomDeck(
			final ImplementedCardList.ImplementedCard cardToInclude)
					throws HSInvalidHeroException
	{
		final DeckAndHero result = this.randomDeck();
		result.deck.set(this.gen.nextInt(30), cardToInclude);
		return result;
	}

	public static class IsNotPlayable implements
	Predicate<ImplementedCardList.ImplementedCard>
	{
		@Override
		public boolean test(final ImplementedCard t)
		{
			return !t.collectible || t.isHero;
		}
	}

	// public getArenaTuple(int turn)
	// {
	// double rarityRoll = Math.random();
	// }
}
