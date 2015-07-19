package com.hearthsim.arena;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.hearthsim.arena.tiergui.ArenaTierReference;
import com.hearthsim.card.ImplementedCardList.ImplementedCard;
import com.hearthsim.model.PlayerModel;

public class DraftData 
{
    public final List<ImplementedCard> cardsInDeck;
    public final BiFunction<ArenaTurnFunction, ImplementedCard, BiFunction<Byte, String, PlayerModel>> lookaheadFunction;
    public final Function<ImplementedCard, Integer> arenaTierFunction;
    public final String hero;
    
    public DraftData(List<ImplementedCard> cardsInDeck, String hero,
            BiFunction<ArenaTurnFunction, ImplementedCard, BiFunction<Byte, String, PlayerModel>> lookaheadFunction)
    {
        this.cardsInDeck = Collections.unmodifiableList(cardsInDeck);
        this.hero = ArenaGenerator.unCapitalize(hero);
        this.lookaheadFunction = lookaheadFunction;
        this.arenaTierFunction = ArenaTierReference.getHeroFunction(this.hero);
    }
    
    public int getTurnCount()
    {
        return cardsInDeck.size() + 1;
    }
}
