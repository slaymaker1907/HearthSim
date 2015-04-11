package com.hearthsim.card.minion.concrete;

import com.hearthsim.card.minion.Minion;
import com.hearthsim.card.minion.MinionBattlecryInterface;
import com.hearthsim.event.effect.CardEffectCharacter;
import com.hearthsim.event.effect.CardEffectCharacterHeal;

public class PriestessOfElune extends Minion implements MinionBattlecryInterface {

    private static final CardEffectCharacter effect = new CardEffectCharacterHeal(4);

    public PriestessOfElune() {
        super();
    }

    /**
     * Battlecry: Restore 4 Health to your Hero
     */
    @Override
    public CardEffectCharacter getBattlecryEffect() {
        return PriestessOfElune.effect;
    }
}
