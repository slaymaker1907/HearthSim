package com.hearthsim.card.minion.concrete;

import com.hearthsim.card.minion.Minion;
import com.hearthsim.card.minion.MinionUntargetableBattlecry;
import com.hearthsim.exception.HSException;
import com.hearthsim.model.PlayerModel;
import com.hearthsim.model.PlayerSide;
import com.hearthsim.util.tree.HearthTreeNode;

public class DefenderOfArgus extends Minion implements MinionUntargetableBattlecry {

    public DefenderOfArgus() {
        super();
    }

    /**
     * Battlecry: Give adjacent minions +1/+1 and Taunt
     */
    @Override
    public HearthTreeNode useUntargetableBattlecry_core(
            Minion minionPlacementTarget,
            HearthTreeNode boardState,
            boolean singleRealizationOnly
        ) throws HSException {
        HearthTreeNode toRet = boardState;
        PlayerModel currentPlayer = toRet.data_.modelForSide(PlayerSide.CURRENT_PLAYER);

        int thisMinionIndex = currentPlayer.getMinions().indexOf(this);
        for (Minion minion : currentPlayer.getMinionsAdjacentToCharacter(thisMinionIndex + 1)) {
            minion.setAttack((byte)(minion.getAttack() + 1));
            minion.setHealth((byte)(minion.getHealth() + 1));
            minion.setTaunt(true);
        }
        return toRet;
    }

}
