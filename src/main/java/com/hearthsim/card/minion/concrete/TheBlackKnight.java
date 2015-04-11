package com.hearthsim.card.minion.concrete;

import com.hearthsim.card.Card;
import com.hearthsim.card.minion.Minion;
import com.hearthsim.card.minion.MinionBattlecryInterface;
import com.hearthsim.event.CharacterFilter;
import com.hearthsim.event.CharacterFilterTargetedBattlecry;
import com.hearthsim.event.effect.CardEffectCharacter;
import com.hearthsim.model.BoardModel;
import com.hearthsim.model.PlayerSide;

public class TheBlackKnight extends Minion implements MinionBattlecryInterface {

    private final static CharacterFilterTargetedBattlecry filter = new CharacterFilterTargetedBattlecry() {
        protected boolean includeEnemyMinions() { return true; }

        @Override
        public boolean targetMatches(PlayerSide originSide, Card origin, PlayerSide targetSide, Minion targetCharacter, BoardModel board) {
            if (!super.targetMatches(originSide, origin, targetSide, targetCharacter, board)) {
                return false;
            }

            return targetCharacter.getTaunt();
        }
    };

    private final static CardEffectCharacter battlecryAction = CardEffectCharacter.DESTROY;

    public TheBlackKnight() {
        super();
    }

    @Override
    public CharacterFilter getBattlecryFilter() {
        return TheBlackKnight.filter;
    }

    @Override
    public CardEffectCharacter getBattlecryEffect() {
        return TheBlackKnight.battlecryAction;
    }
}
