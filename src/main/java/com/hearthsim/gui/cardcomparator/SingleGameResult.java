package com.hearthsim.gui.cardcomparator;

import java.util.ArrayList;

import com.hearthsim.card.ImplementedCardList;

public class SingleGameResult
{
	public CardGameResult result;
	public ArrayList<ImplementedCardList.ImplementedCard> deck0, deck1;

	public SingleGameResult(final CardGameResult result,
			final ArrayList<ImplementedCardList.ImplementedCard> deck0,
			final ArrayList<ImplementedCardList.ImplementedCard> deck1)
	{
		this.result = result;
		this.deck0 = deck0;
		this.deck1 = deck1;
	}
}
