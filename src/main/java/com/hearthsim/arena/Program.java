package com.hearthsim.arena;

import java.util.function.Function;

import com.hearthsim.arena.tiergui.ArenaDatabase;

public class Program {
    public static void main(String[] args) throws Exception 
    {
        // ArenaGenerator.simulateArena(new ArenaAgent(), args[0], false);
//        IndeterminateExecutor<Object> executor = new IndeterminateExecutor<>();
//        executor.apply(ArenaDatabase::gatherData);
        Function<Integer, Double> manaCurve = ArenaDatabase.getWinningCurve();
        for(int i = 0; i <= 10; i++)
            System.out.println("Manacost: " + i + " Count: " + (manaCurve.apply(i) * 30));
        double upperCount = 0.0;
        for(int i = 7; i <= 50; i++)
        {
            upperCount += manaCurve.apply(i) * 30;
        }
        System.out.println("7+ = " + upperCount);
        double lowerCount = 0.0;
        for(int i = 0; i <= 2; i++)
        {
            lowerCount += manaCurve.apply(i) * 30;
        }
        System.out.println("2's = " + lowerCount);
    }
}
