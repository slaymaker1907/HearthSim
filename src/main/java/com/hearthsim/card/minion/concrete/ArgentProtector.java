package com.hearthsim.card.minion.concrete;

import com.hearthsim.card.Card;
import com.hearthsim.card.minion.Minion;
import com.hearthsim.card.minion.MinionBattlecryInterface;
import com.hearthsim.event.CharacterFilter;
import com.hearthsim.event.CharacterFilterTargetedBattlecry;
import com.hearthsim.event.effect.CardEffectCharacter;
import com.hearthsim.model.PlayerSide;
import com.hearthsim.util.tree.HearthTreeNode;

public class ArgentProtector extends Minion implements MinionBattlecryInterface {

    /**
     * Battlecry: Give a friendly minion Divine Shield
     */
    private final static CharacterFilterTargetedBattlecry filter = new CharacterFilterTargetedBattlecry() {
        protected boolean includeOwnMinions() { return true; }
    };

    private final static CardEffectCharacter battlecryAction = new CardEffectCharacter() {
        @Override
        public HearthTreeNode applyEffect(PlayerSide originSide, Card origin, PlayerSide targetSide, int targetCharacterIndex, HearthTreeNode boardState) {
            Minion targetMinion = boardState.data_.modelForSide(targetSide).getCharacter(targetCharacterIndex);
            targetMinion.setDivineShield(true);
            return boardState;
        }
    };

    public ArgentProtector() {
        super();
    }

    @Override
    public CharacterFilter getBattlecryFilter() {
        return ArgentProtector.filter;
    }

    @Override
    public CardEffectCharacter getBattlecryEffect() {
        return ArgentProtector.battlecryAction;
    }
}
