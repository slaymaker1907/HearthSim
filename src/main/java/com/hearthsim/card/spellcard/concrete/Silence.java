package com.hearthsim.card.spellcard.concrete;

import com.hearthsim.card.spellcard.SpellCard;
import com.hearthsim.event.CharacterFilter;
import com.hearthsim.event.effect.CardEffectCharacter;
import com.hearthsim.event.CharacterFilterTargetedSpell;

public class Silence extends SpellCard {


    public Silence() {
        super();
    }

    @Override
    protected CharacterFilter getTargetFilter() {
        return CharacterFilterTargetedSpell.ALL_MINIONS;
    }

    @Deprecated
    public Silence(boolean hasBeenUsed) {
        this();
        this.hasBeenUsed = hasBeenUsed;
    }

    /**
     *
     * Use the card on the given target
     *
     * Deals 1 damage and draws a card
     *
     *
     *
     * @param side
     * @param boardState The BoardState before this card has performed its action.  It will be manipulated and returned.
     *
     * @return The boardState is manipulated and returned
     */
    @Override
    protected CardEffectCharacter getEffect() {
        return CardEffectCharacter.SILENCE;
    }
}
