package com.hearthsim.arena;

public class Program {
    public static void main(String[] args) throws Exception 
    {
        ArenaGenerator.simulateArena(new ArenaAgent(), args[0], false);
    }
}
