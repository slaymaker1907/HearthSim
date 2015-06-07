package com.hearthsim.gui.cardcomparator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.function.Predicate;

import com.hearthsim.card.ImplementedCardList;
import com.hearthsim.card.ImplementedCardList.ImplementedCard;

public class SortByClass
{

	public static void main(String[] args)
	{
		if (args.length == 0)
			args = new String[] { "Warlock" };
		final Thread listenForDestruction = new Thread(() ->
		{
			while (true)
			{
				try
				{
					System.in.read();
					System.exit(0);
				} catch (final IOException e)
				{
				}
			}
		});
		listenForDestruction.start();
		// listenForDestruction.setPriority(Thread.MIN_PRIORITY);

		final String hero = args[0];
		for (final String rarity : SortByClass.printAllPossibleRarities())
		{
			SortByClass.runSort(rarity, hero);
		}
	}

	private static void runSort(final String rarity, final String hero)
	{
		final ArrayList<ImplementedCardList.ImplementedCard> allCards = new GenerateDecks()
		.getCardsByCharClass(hero);
		allCards.removeIf(new FilterByRarity(rarity));
		System.out.println("Sorting " + allCards.size() + " cards. " + hero
				+ " " + rarity);
		final ImplementedCardList.ImplementedCard[] cards = new ImplementedCardList.ImplementedCard[allCards
		                                                                                            .size()];
		allCards.toArray(cards);
		final CardComparator comp = new CardComparator(hero, hero
				+ rarity + ".ser");
		Arrays.parallelSort(cards, comp);

		// Output the results.
		for (final ImplementedCardList.ImplementedCard card : cards)
			CardComparator.original.println(card.name_);
		PrintWriter writer;
		try
		{
			writer = new PrintWriter(hero + rarity + ".txt");
			for (final ImplementedCardList.ImplementedCard card : cards)
				writer.println(card.name_);
			writer.close();
		} catch (final FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	public static HashSet<String> printAllPossibleRarities()
	{
		final HashSet<String> rarities = new HashSet<String>();
		final ArrayList<ImplementedCard> allCards = ImplementedCardList
				.getInstance().getCardList();
		for (final ImplementedCard card : allCards)
			rarities.add(card.rarity_);
		rarities.remove(null);
		return rarities;
	}

	public static class FilterByRarity implements
	Predicate<ImplementedCardList.ImplementedCard>
	{
		private final String rarity;

		public FilterByRarity(final String rarity)
		{
			this.rarity = rarity;
		}

		@Override
		public boolean test(final ImplementedCard arg0)
		{
			return !arg0.rarity_.equals(this.rarity);
		}
	}
}
