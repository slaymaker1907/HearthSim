package com.hearthsim.gui.cardcomparator;

import java.util.ArrayList;

import com.hearthsim.card.ImplementedCardList.ImplementedCard;
import com.hearthsim.card.minion.Hero;

public class DeckAndHero
{
	public DeckAndHero(final Hero hero, final ArrayList<ImplementedCard> deck)
	{
		this.hero = hero;
		this.deck = deck;
	}

	public ArrayList<ImplementedCard> deck;
	public Hero hero;
}
