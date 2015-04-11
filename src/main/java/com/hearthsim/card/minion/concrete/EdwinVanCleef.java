package com.hearthsim.card.minion.concrete;

import com.hearthsim.card.minion.Minion;
import com.hearthsim.card.minion.MinionBattlecryInterface;
import com.hearthsim.event.effect.CardEffectCharacter;
import com.hearthsim.model.PlayerSide;
import com.hearthsim.util.tree.HearthTreeNode;

/**
 * Created by oyachai on 3/21/15.
 */
public class EdwinVanCleef extends Minion implements MinionBattlecryInterface {

    public EdwinVanCleef() {
        super();
    }

    @Override
    public CardEffectCharacter getBattlecryEffect() {
        return new CardEffectCharacter<Minion>() {

            @Override
            public HearthTreeNode applyEffect(PlayerSide originSide, Minion origin, PlayerSide targetSide, int targetCharacterIndex, HearthTreeNode boardState) {
                byte buff = (byte) ((boardState.data_.getCurrentPlayer().getNumCardsUsed() - 1) * 2);
                origin.addAttack(buff);
                origin.addHealth(buff);
                origin.addMaxHealth(buff);
                return boardState;
            }
        };
    }
}
