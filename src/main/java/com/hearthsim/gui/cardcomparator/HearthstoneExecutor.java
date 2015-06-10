package com.hearthsim.gui.cardcomparator;

import java.util.Collection;

public class HearthstoneExecutor
{
    private ComputeGames[] tasks;
    private PlayerModelFactory.Builder player0, player1;
    private Thread[] threads;
    
    public HearthstoneExecutor(PlayerModelFactory.Builder player0, PlayerModelFactory.Builder player1)
    {
        this.player0 = player0;
        this.player1 = player1;
        startComputation();
    }
    
    private void startComputation()
    {
        tasks = new ComputeGames[Runtime.getRuntime().availableProcessors() / 2];
        threads = new Thread[tasks.length];
        for(int i = 0; i < tasks.length; i++)
        {
            ComputeGames computation = new ComputeGames(player0.createPlayerModelFactory(), player1.createPlayerModelFactory());
            tasks[i] = computation;
            threads[i] = new Thread(computation);
            threads[i].start();
        }
    }
    
    public void stopComputation()
    {
        for(ComputeGames comp : tasks)
            comp.stopComputation();
    }
    
    @Override
    protected void finalize()
    {
        stopComputation();
    }
    
    public int accumulateResults(Collection<DetailedGameResult> results)
    {
        int count = 0;
        for (ComputeGames task : tasks)
            count += task.addResultsToCollection(results);
        return count;
    }
}