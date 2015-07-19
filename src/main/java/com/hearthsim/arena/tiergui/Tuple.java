package com.hearthsim.arena.tiergui;

import java.io.Serializable;

public class Tuple<T1, T2> implements Serializable
{
    private static final long serialVersionUID = 1L;
    private final T1 first;
    private final T2 second;
    
    public Tuple(T1 first, T2 second)
    {
        this.first = first;
        this.second = second;
    }
    public T1 getFirst()
    {
        return first;
    }
    
    public T2 getSecond()
    {
        return second;
    }
}
