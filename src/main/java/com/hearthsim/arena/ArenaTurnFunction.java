package com.hearthsim.arena;

import java.util.function.BiFunction;

import com.hearthsim.card.ImplementedCardList.ImplementedCard;
import com.hearthsim.model.PlayerModel;

@FunctionalInterface
public interface ArenaTurnFunction
{
    // TODO Probably make an immutable arraylist.
    public ImplementedCard takeTurn(ImplementedCard first, ImplementedCard second, ImplementedCard third,
            BiFunction<ArenaTurnFunction, ImplementedCard, BiFunction<Byte, String, PlayerModel>> lookaheadFunction) throws Exception;
}
