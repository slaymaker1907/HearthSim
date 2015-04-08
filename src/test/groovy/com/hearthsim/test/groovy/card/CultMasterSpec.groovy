package com.hearthsim.test.groovy.card

import com.hearthsim.card.minion.concrete.*
import com.hearthsim.card.spellcard.concrete.Fireball
import com.hearthsim.card.spellcard.concrete.TwistingNether
import com.hearthsim.card.spellcard.concrete.Whirlwind
import com.hearthsim.model.BoardModel
import com.hearthsim.test.helpers.BoardModelBuilder
import com.hearthsim.util.tree.CardDrawNode
import com.hearthsim.util.tree.HearthTreeNode

import static com.hearthsim.model.PlayerSide.CURRENT_PLAYER
import static com.hearthsim.model.PlayerSide.WAITING_PLAYER

class CultMasterSpec extends CardSpec {

    HearthTreeNode root
    BoardModel startingBoard

    def setup() {
        startingBoard = new BoardModelBuilder().make {
            currentPlayer {
                hand([Fireball, Whirlwind, TwistingNether])
                field([[minion: CultMaster], [minion: MurlocRaider], [minion: PatientAssassin]])
                mana(10)
            }
            waitingPlayer {
                field([[minion: BloodfenRaptor],[minion: StranglethornTiger]])
            }
        }

        root = new HearthTreeNode(startingBoard)
    }

    def "draws card on friendly minion death"() {
        def copiedBoard = startingBoard.deepCopy()
        def theCard = root.data_.getCurrentPlayer().getHand().get(0)
        def ret = theCard.useOn(CURRENT_PLAYER, 2, root)

        expect:
        ret != null
        ret instanceof CardDrawNode

        ((CardDrawNode)ret).numCardsToDraw == 1

        assertBoardDelta(copiedBoard, ret.data_) {
            currentPlayer {
                removeCardFromHand(Fireball)
                mana(6)
                numCardsUsed(1)
                removeMinion(1)
            }
        }
    }

    def "does not trigger on enemy minion death"() {
        def copiedBoard = startingBoard.deepCopy()
        def theCard = root.data_.getCurrentPlayer().getHand().get(0)
        def ret = theCard.useOn(WAITING_PLAYER, 1, root)

        expect:
        ret != null
        ret instanceof HearthTreeNode
        !(ret instanceof CardDrawNode)

        assertBoardDelta(copiedBoard, ret.data_) {
            currentPlayer {
                removeCardFromHand(Fireball)
                mana(6)
                numCardsUsed(1)
            }
            waitingPlayer {
                removeMinion(0)
            }
        }
    }

    def "draws one card for each death"() {
        def copiedBoard = startingBoard.deepCopy()
        def theCard = root.data_.getCurrentPlayer().getHand().get(1)
        def ret = theCard.useOn(CURRENT_PLAYER, 0, root)

        expect:
        ret != null
        ret instanceof CardDrawNode

        ((CardDrawNode)ret).numCardsToDraw == 2

        assertBoardDelta(copiedBoard, ret.data_) {
            currentPlayer {
                removeCardFromHand(Whirlwind)
                mana(9)
                numCardsUsed(1)
                removeMinion(2)
                removeMinion(1)
                updateMinion(0, [deltaHealth: -1])
            }
            waitingPlayer {
                updateMinion(1, [deltaHealth: -1])
                updateMinion(0, [deltaHealth: -1])
            }
        }
    }

    def "does not draw if also died"() {
        def copiedBoard = startingBoard.deepCopy()
        def theCard = root.data_.getCurrentPlayer().getHand().get(2)
        def ret = theCard.useOn(CURRENT_PLAYER, 0, root)

        expect:
        ret != null
        !(ret instanceof CardDrawNode)

        assertBoardDelta(copiedBoard, ret.data_) {
            currentPlayer {
                removeCardFromHand(TwistingNether)
                mana(2)
                numCardsUsed(1)
                removeMinion(2)
                removeMinion(1)
                removeMinion(0)
            }
            waitingPlayer {
                removeMinion(1)
                removeMinion(0)
            }
        }
    }

    def "does not trigger while in hand"() {
        startingBoard.modelForSide(CURRENT_PLAYER).placeCardHand(new CultMaster())
        startingBoard.removeMinion(CURRENT_PLAYER, 0)

        def copiedBoard = startingBoard.deepCopy()
        def theCard = root.data_.getCurrentPlayer().getHand().get(0)
        def ret = theCard.useOn(CURRENT_PLAYER, 1, root)

        expect:
        ret != null
        ret instanceof HearthTreeNode
        !(ret instanceof CardDrawNode)

        assertBoardDelta(copiedBoard, ret.data_) {
            currentPlayer {
                removeCardFromHand(Fireball)
                mana(6)
                numCardsUsed(1)
                removeMinion(0)
            }
        }
    }
}