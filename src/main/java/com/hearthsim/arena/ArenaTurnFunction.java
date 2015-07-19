package com.hearthsim.arena;

import com.hearthsim.card.ImplementedCardList.ImplementedCard;

@FunctionalInterface
public interface ArenaTurnFunction
{
    public ImplementedCard takeTurn(ImplementedCard first, ImplementedCard second, ImplementedCard third,
            DraftData arenaState) throws Exception;
}
