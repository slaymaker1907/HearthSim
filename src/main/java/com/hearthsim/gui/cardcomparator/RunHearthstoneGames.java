package com.hearthsim.gui.cardcomparator;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import com.hearthsim.Game;
import com.hearthsim.card.Card;
import com.hearthsim.card.Deck;
import com.hearthsim.card.ImplementedCardList;
import com.hearthsim.model.PlayerModel;
import com.hearthsim.player.playercontroller.ArtificialPlayer;

public class RunHearthstoneGames
{
	public static class RunGame implements Callable<SingleGameResult>
	{
		private final DeckAndHero playerOne, playerTwo;
		private final ArtificialPlayer aiOne, aiTwo;
		private final PlayerModel playerOneModel, playerTwoModel;
		private final Object objectToSync;

		public RunGame(final DeckAndHero playerOne,
				final ArtificialPlayer artificialPlayer,
				final DeckAndHero playerTwo,
				final ArtificialPlayer artificialPlayer2,
				final Object objectToSync)
		{
			this.playerOne = playerOne;
			this.playerTwo = playerTwo;
			this.aiOne = artificialPlayer;
			this.aiTwo = artificialPlayer2;
			this.playerOneModel = new PlayerModel((byte) 0, "playerOne",
					playerOne.hero, RunGame.getDeck(playerOne.deck));
			this.playerTwoModel = new PlayerModel((byte) 1, "playerTwo",
					playerTwo.hero, RunGame.getDeck(playerTwo.deck));
			this.objectToSync = objectToSync;
		}

		@Override
		public SingleGameResult call() throws Exception
		{
			SingleGameResult result;
			try
			{
				result = new SingleGameResult(RunHearthstoneGames.runGame(
						this.playerOneModel, this.aiOne, this.playerTwoModel,
						this.aiTwo), this.playerOne.deck, this.playerTwo.deck);
			} catch (final Exception e)
			{
				// Return an empty result if anything goes wrong.
				result = new SingleGameResult(new CardGameResult(0, 0),
						new ArrayList<ImplementedCardList.ImplementedCard>(),
						new ArrayList<ImplementedCardList.ImplementedCard>());
			}

			synchronized (this.objectToSync)
			{
				this.objectToSync.notify();
			}
			return result;
		}

		public static Deck getDeck(
				final ArrayList<ImplementedCardList.ImplementedCard> cards)
		{
			final ArrayList<Card> partialResult = new ArrayList<Card>();
			for (final ImplementedCardList.ImplementedCard card : cards)
			{
				partialResult.add(card.createCardInstance());
			}
			return new Deck(partialResult);
		}
	}

	public static CardGameResult runGame(final PlayerModel playerOne,
			final ArtificialPlayer aiOne, final PlayerModel playerTwo,
			final ArtificialPlayer aiTwo)
	{
		final Game theGame = new Game(playerOne, playerTwo, aiOne, aiTwo);
		try
		{
			final boolean result = theGame.runGame().winnerPlayerIndex_ == playerOne
					.getPlayerId();
			if (result)
				return new CardGameResult(1, 1);
			else
				return new CardGameResult(0, 1);
		} catch (final Exception e)
		{
			return new CardGameResult(0, 0);
		}
	}
}
