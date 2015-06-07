package com.hearthsim.gui.cardcomparator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

import com.hearthsim.card.ImplementedCardList;
import com.hearthsim.card.ImplementedCardList.ImplementedCard;

public class SortByRarityGUI
{

	public static void main(final String[] args)
	{
		try
		{
			Integer numOfProcesses = null;
			while (numOfProcesses == null)
			{
				try
				{
					numOfProcesses = Integer
							.parseInt(JOptionPane
									.showInputDialog("Input number of processes to launch."));
				} catch (final NumberFormatException e)
				{

				}
			}
			final ExecutorService processes = Executors
					.newFixedThreadPool(numOfProcesses);

			Runtime.getRuntime().addShutdownHook(
					new Thread(() -> JavaProcess.shutDownNow()));

			final HashSet<String> possibleHeroes = SortByRarityGUI
					.getAllPossibleHeroes();
			for (int i = 0; i < numOfProcesses; i++)
			{
				final String arg = SortByRarityGUI
						.getProcessArg(possibleHeroes);
				processes.execute(new JavaProcess.ExecuteProcess(
						SortByClass.class, arg));
				possibleHeroes.remove(arg);
			}

			processes.shutdown();
			try
			{
				processes.awaitTermination(7, TimeUnit.DAYS);
			} catch (final InterruptedException e)
			{
				processes.shutdownNow();
				e.printStackTrace();
			}
		} catch (final Exception e)
		{
			e.printStackTrace();
			JavaProcess.shutDownNow();
		}
	}

	public static String getProcessArg(final HashSet<String> possibleHeroes)
	{
		String hero = "";
		while (!possibleHeroes.contains(hero))
			hero = JOptionPane.showInputDialog("Input hero to analyze.");
		return hero;
	}

	public static HashSet<String> getAllPossibleHeroes()
	{
		final HashSet<String> rarities = new HashSet<String>();
		final ArrayList<ImplementedCard> allCards = ImplementedCardList
				.getInstance().getCardList();
		for (final ImplementedCard card : allCards)
			rarities.add(card.charClass_);
		rarities.remove(null);
		final HashSet<String> result = new HashSet<String>();
		for (final String hero : rarities)
			result.add(hero.substring(0, 1).toUpperCase()
					+ hero.substring(1, hero.length()));
		return result;
	}

}