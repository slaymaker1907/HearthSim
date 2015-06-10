package com.hearthsim.gui.cardcomparator;

import com.hearthsim.card.Deck;

public class DetailedGameResult 
{
    public Deck cards;
    public boolean isWin;
    
    public DetailedGameResult(Deck cards, boolean isWin)
    {
        this.cards = cards;
        this.isWin = isWin;
    }
}
