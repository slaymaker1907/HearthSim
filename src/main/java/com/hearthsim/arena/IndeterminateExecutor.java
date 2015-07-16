package com.hearthsim.arena;

import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;

public class IndeterminateExecutor<OutputT>
    implements Function<Supplier<OutputT>, OutputT>
{
    public static class ThreadCommunicator
    {
        public boolean isComplete;
        public ThreadCommunicator()
        {
            this.isComplete = false;
        }
    }
    
    @Override
    public OutputT apply(Supplier<OutputT> supplier)
    {
        int processors = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(processors);
        ExecutorCompletionService<OutputT> executorService = new ExecutorCompletionService<OutputT>(executor);
        ThreadCommunicator comm = new ThreadCommunicator();
        for(int i = 0; i < processors; i++)
            executorService.submit(() -> executeTillComplete(supplier, comm));
        
        OutputT result;
        try
        {
            result = executorService.take().get();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        executor.shutdownNow();
        return result;
    }
    
    private static <OutputT> OutputT executeTillComplete(Supplier<OutputT> function, ThreadCommunicator comm)
    {
        OutputT result = null;
        while(result == null && !comm.isComplete)
        {
            result = function.get();
        }
        
        comm.isComplete = true;
        return result;
    }
}
