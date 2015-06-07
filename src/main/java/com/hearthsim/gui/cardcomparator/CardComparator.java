package com.hearthsim.gui.cardcomparator;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.inference.AlternativeHypothesis;
import org.apache.commons.math3.stat.inference.BinomialTest;

import com.hearthsim.card.ImplementedCardList;
import com.hearthsim.card.ImplementedCardList.ImplementedCard;
import com.hearthsim.gui.cardcomparator.RunHearthstoneGames.RunGame;
import com.hearthsim.player.playercontroller.BruteForceSearchAI;

public class CardComparator implements
Comparator<ImplementedCardList.ImplementedCard>
{
	private HashMap<ImplementedCardList.ImplementedCard, CardVersusCard> comparisonData;
	private ThreadPoolExecutor executor;
	protected GenerateDecks deckGen0;
	protected GenerateDecks deckGen1;
	private Object lock;
	private final String serialFileName;

	public static final PrintStream original = System.out;
	private ExecutorCompletionService<SingleGameResult> taskQueue;

	public CardComparator()
	{
		this.serialFileName = "comparisonData.ser";
		this.initializeComparisonDataFromFile();
		this.deckGen0 = this.deckGen1 = new GenerateDecks();
		System.setOut(new NullPrintStream());
		Runtime.getRuntime().addShutdownHook(new DumpDataThread());
	}

	public CardComparator(final String hero, final String serialFileName)
	{
		this.serialFileName = serialFileName;
		this.initializeComparisonDataFromFile();
		this.deckGen0 = new GenerateDecks(hero);
		this.deckGen1 = new GenerateDecks();
		System.setOut(new NullPrintStream());

		// Make sure that expensive computations are saved.
		Runtime.getRuntime().addShutdownHook(new DumpDataThread());
	}

	private void initializeThreadPool()
	{
		this.executor = (ThreadPoolExecutor) Executors
				.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		this.taskQueue = new ExecutorCompletionService<SingleGameResult>(
				this.executor);
		this.lock = new Object();
	}

	private class DumpDataThread extends Thread
	{
		@Override
		public void run()
		{
			CardComparator.this.dumpData();
		}
	}

	@SuppressWarnings("unchecked")
	private void initializeComparisonDataFromFile()
	{
		try
		{
			final FileInputStream fileIn = new FileInputStream(
					this.serialFileName);
			final ObjectInputStream objectIn = new ObjectInputStream(fileIn);
			this.comparisonData = (HashMap<ImplementedCard, CardVersusCard>) objectIn
					.readObject();
			objectIn.close();
			fileIn.close();
		} catch (final Exception e)
		{
			CardComparator.original
					.println("Could not read in comparison data.  Starting from scratch.");
			this.comparisonData = new HashMap<ImplementedCard, CardVersusCard>();
		}
	}

	@Override
	public int compare(final ImplementedCard arg0, final ImplementedCard arg1)
	{
		CardComparator.original.println("Now comparing " + arg0.name_
				+ " to " + arg1.name_);
		long totalStart, start;
		this.initializeThreadPool();

		if (!this.comparisonData.containsKey(arg0))
			this.comparisonData.put(arg0, new CardVersusCard(arg0));
		if (!this.comparisonData.containsKey(arg1))
			this.comparisonData.put(arg1, new CardVersusCard(arg1));
		final CardVersusCard arg0Data = this.comparisonData.get(arg0);
		final CardVersusCard arg1Data = this.comparisonData.get(arg1);

		int count = 0;
		start = totalStart = System.currentTimeMillis();
		while (!this.isReady(arg0Data, arg1Data))
		{
			this.getNextResult(arg0, arg1);
			count++;
			final long currentTime = System.currentTimeMillis();
			if ((currentTime - start) / 1000.0 > 60.0)
			{
				this.dumpData();
				start = currentTime;
				CardComparator.original.println((currentTime - totalStart)
						/ 1000.0 / count + " seconds per comparison.");
			}
		}

		// Get everything out of the queue.
		this.executor.shutdown();
		try
		{
			this.executor.awaitTermination(5, TimeUnit.MINUTES);
		} catch (final InterruptedException e)
		{
			e.printStackTrace();
		}

		while (true)
		{
			final Future<SingleGameResult> toProcess = this.taskQueue.poll();
			if (toProcess == null)
				break;
			this.processFuture(toProcess);
		}

		CardComparator.original.println("Comparison complete in "
				+ (System.currentTimeMillis() - totalStart) / 1000.0 / 60.0
				+ " minutes.\n");
		System.gc();
		return Double.compare(arg0Data.winRatio(), arg1Data.winRatio());
	}

	private void dumpData()
	{
		try
		{
			final PrintWriter writer = new PrintWriter(
					this.serialFileName.substring(0,
							this.serialFileName.length() - 4)
							+ ".txt");
			for (final ImplementedCardList.ImplementedCard card : this.comparisonData
					.keySet())
			{
				writer.print(this.comparisonData.get(card).toString());
			}
			writer.close();
		} catch (final Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			final FileOutputStream fileOut = new FileOutputStream(
					this.serialFileName);
			final ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
			objectOut.writeObject(this.comparisonData);
			objectOut.close();
			fileOut.close();
		} catch (final Exception e)
		{
			e.printStackTrace();
		}
	}

	private void getNextResult(final ImplementedCard arg0,
			final ImplementedCard arg1)
	{
		while (this.executor.getActiveCount() < this.executor
				.getMaximumPoolSize())
		{
			this.taskQueue.submit(this.getNewGame(arg0));
			this.taskQueue.submit(this.getNewGame(arg1));
			final Future<SingleGameResult> result = this.taskQueue.poll();
			if (result != null)
			{
				this.processFuture(result);
				return;
			}
		}

		synchronized (this.lock)
		{
			try
			{
				this.lock.wait();
			} catch (final InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	private boolean processFuture(final Future<SingleGameResult> toProcess)
	{
		if (toProcess.isDone())
		{
			if (!toProcess.isCancelled())
			{
				try
				{
					final SingleGameResult result = toProcess.get();
					this.processSingleGameResult(result);
				} catch (InterruptedException | ExecutionException e)
				{
				}
			}
			return true;
		}
		return false;
	}

	private void processSingleGameResult(final SingleGameResult result)
	{
		for (final ImplementedCardList.ImplementedCard card : result.deck0)
		{
			if (!this.comparisonData.containsKey(card))
				this.comparisonData.put(card, new CardVersusCard(card));
			this.comparisonData.get(card).addGameResult(result.result);
		}
	}

	protected RunHearthstoneGames.RunGame getNewGame(final ImplementedCard arg0)
	{
		DeckAndHero playerOne, playerTwo;
		playerOne = playerTwo = null;
		try
		{
			playerOne = this.deckGen0.mostlyRandomDeck(arg0);
			playerTwo = this.deckGen1.randomDeck();
		} catch (final Exception e)
		{
		}

		BruteForceSearchAI ai1, ai2;
		ai1 = BruteForceSearchAI.buildStandardAI1();
		ai2 = BruteForceSearchAI.buildStandardAI1();
		ai1.setUseDuplicateNodePruning(true);
		ai2.setUseDuplicateNodePruning(true);
		ai1.setUseSparseBoardStateFactory(true);
		ai2.setUseSparseBoardStateFactory(true);

		return new RunGame(playerOne, BruteForceSearchAI.buildStandardAI1(),
				playerTwo, BruteForceSearchAI.buildStandardAI1(), this.lock);
	}

	private static class CardVersusCard implements Serializable
	{
		private static final long serialVersionUID = 2L;
		private final CardGameResult record;
		private final ImplementedCardList.ImplementedCard thisCard;

		public CardVersusCard(final ImplementedCardList.ImplementedCard thisCard)
		{
			this.thisCard = thisCard;
			this.record = new CardGameResult(0, 0);
		}

		public void addGameResult(final CardGameResult toAdd)
		{
			this.record.add(toAdd);
		}

		public double winRatio()
		{
			return this.record.getWinRatio();
		}

		public int totalGames()
		{
			return this.record.getTotalGames();
		}

		public int totalWins()
		{
			return this.record.getFirstPlayerWins();
		}

		@Override
		public String toString()
		{
			final StringBuilder result = new StringBuilder();

			result.append(this.thisCard.name_);
			result.append(" ");
			result.append(this.record.getFirstPlayerWins());
			result.append(" ");
			result.append(this.record.getTotalGames());
			result.append("\n");

			return result.toString();
		}
	}

	private boolean isReady(final CardVersusCard first,
			final CardVersusCard second)
	{
		final double alpha = 0.1;
		final int maxGames = 2000;

		if (first.totalGames() < 2 || second.totalGames() < 2)
			return false;
		else if (first.totalGames() > maxGames
				&& second.totalGames() > maxGames)
			return true;

		// Do a test to make sure that there is alpha chance that the two
		// probabilities are equal.
		final BinomialTest test = new BinomialTest();
		return test.binomialTest(first.totalGames(), first.totalWins(),
				second.winRatio(), AlternativeHypothesis.TWO_SIDED, alpha)
				&& test.binomialTest(second.totalGames(), second.totalWins(),
						first.winRatio(), AlternativeHypothesis.TWO_SIDED,
						alpha);
	}

}
