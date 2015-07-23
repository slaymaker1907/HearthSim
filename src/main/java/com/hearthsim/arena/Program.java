package com.hearthsim.arena;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.function.Function;

import com.hearthsim.arena.tiergui.ArenaDatabase;
import com.hearthsim.arena.tiergui.ExtractArenaWebData;
import com.hearthsim.arena.tiergui.ExtractArenaWebData.ArenaWebData;
import com.hearthsim.card.ImplementedCardList.ImplementedCard;
import com.hearthsim.util.DeckFactory;

public class Program {
    public static final String mainFile = "ArenaDatabaseCurve.ser";
    public static final String secondFile = "ArenaDatabaseCurve2.ser";
    public static final String manaCurveDataBase = Program.mainFile;
    
    public static void main(String[] args) throws Exception 
    {
//         ArenaGenerator.simulateArena(new ArenaAgent(), args[0], false);
//        printManaCurve(Program.mainFile);
        Program.gatherData(Program.secondFile);
    }
    
    public static void gatherData(String fileName)
    {
        Program.deleteFile(fileName);
        Program.launchStopWatcher();
        IndeterminateExecutor<Object> executor = new IndeterminateExecutor<>();
        ArenaDatabase data = new ArenaDatabase(fileName);
        executor.apply(data::gatherData);
    }
    
    public static boolean deleteFile(String fileName)
    {
        File toDelete = new File(fileName);
        return toDelete.delete();
    }
    
    public static void printManaCurve(String fileName)
    {
        Function<Integer, Double> manaCurve = new ArenaDatabase(fileName).getWinningGroupCurve();
        for(int i = 2; i <= 7; i++)
            System.out.println("Manacost: " + i + " Count: " + manaCurve.apply(i));
    }
    
    public static void stopWatcher()
    {
        Scanner sc = new Scanner(System.in);
        UnsafeSupplier<String> getLine = sc::nextLine;
        String line = "";
        while(line == null || !line.equals("stop"))
        {
            line = getLine.getSafely();
            Program.safeSleep(100);
        }
        
        sc.close();
        System.exit(0);
    }
    
    public static void launchStopWatcher()
    {
        Thread shutdownThread = new Thread(Program::stopWatcher);
        shutdownThread.setDaemon(true);
        shutdownThread.start();
    }
    
    public static void safeSleep(int milliseconds)
    {
        UnsafeSupplier<Object> sleep = () -> 
        {
            Thread.sleep(milliseconds);
            return null;
        };
        sleep.getSafely();
    }
    
    public static void writeStringToFile(String fileName, String data) throws IOException
    {
        FileWriter writer = new FileWriter(fileName);
        writer.write(data);
        writer.close();
    }
    
    public static void verifyWebData(String hero) throws Exception
    {
        ArenaWebData warlock = ExtractArenaWebData.parseHero(hero);
        for(ImplementedCard card : new DeckFactory.DeckFactoryBuilder().filterByHero(hero, "neutral").buildDeckFactory().getAllPossibleCards())
        {
            try
            {
                int val = warlock.getTier.apply(card);
                if (val < 1 || val > 8)
                    System.out.println(card.name_);
            }
            catch (Exception e)
            {
                System.out.println(card.name_ + ";" + card.rarity_);
            }
        }
    }
}
