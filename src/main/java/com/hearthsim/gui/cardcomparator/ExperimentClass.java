package com.hearthsim.gui.cardcomparator;

import java.io.IOException;
import java.util.function.Predicate;

import com.hearthsim.card.ImplementedCardList;
import com.hearthsim.card.ImplementedCardList.ImplementedCard;
import com.hearthsim.exception.HSException;

public class ExperimentClass
{

	public static void main(final String[] args) throws IOException,
	InterruptedException, HSException
	{
		// // Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		// ArrayList<ImplementedCardList.ImplementedCard> allCards =
		// ImplementedCardList.getInstance().getCardList();
		// allCards.removeIf(new FilterCards());
		// ImplementedCardList.ImplementedCard[] cards = new
		// ImplementedCardList.ImplementedCard[allCards.size()];
		// allCards.toArray(cards);
		// FastCardComparator comp = new FastCardComparator();
		// Arrays.parallelSort(cards, comp);
		//
		// // Output the results.
		// for(ImplementedCardList.ImplementedCard card : cards)
		// FastCardComparator.original.println(card.name_);
		// PrintWriter writer = new PrintWriter("sortedByGoodness.txt");
		// for(ImplementedCardList.ImplementedCard card : cards)
		// writer.println(card.name_);
		// writer.close();
	}

	public static class FilterCards implements
	Predicate<ImplementedCardList.ImplementedCard>
	{
		@Override
		public boolean test(final ImplementedCard t)
		{
			return !t.collectible || t.isHero
					|| !t.charClass_.equals("neutral");
		}
	}

}
