package com.hearthsim.gui.cardcomparator;

import java.io.Serializable;

public class CardGameResult implements Serializable
{
	private static final long serialVersionUID = 1L;
	private int firstPlayerWins;
	private int games;

	public CardGameResult(final int firstPlayerWins, final int games)
	{
		this.firstPlayerWins = firstPlayerWins;
		this.games = games;
	}

	public int getFirstPlayerWins()
	{
		return this.firstPlayerWins;
	}

	public int getTotalGames()
	{
		return this.games;
	}

	public void add(final CardGameResult toAdd)
	{
		this.firstPlayerWins = this.firstPlayerWins + toAdd.firstPlayerWins;
		this.games = this.games + toAdd.games;
	}

	public CardGameResult deepCopy()
	{
		return new CardGameResult(this.firstPlayerWins, this.games);
	}

	public double getWinRatio()
	{
		return (double) this.firstPlayerWins / this.games;
	}
}
