package com.hearthsim.gui.cardcomparator;

import java.io.Serializable;

import com.hearthsim.card.ImplementedCardList;

    public class CardVersusCard implements Serializable
    {
        private static final long serialVersionUID = 2L;
        private final CardGameResult record;
        private final ImplementedCardList.ImplementedCard thisCard;

        public CardVersusCard(final ImplementedCardList.ImplementedCard thisCard)
        {
            this.thisCard = thisCard;
            this.record = new CardGameResult(0, 0);
        }

        public void addGameResult(final CardGameResult toAdd)
        {
            this.record.add(toAdd);
        }
        
        public double winRatio()
        {
            return this.record.getWinRatio();
        }

        public int totalGames()
        {
            return this.record.getTotalGames();
        }

        public int totalWins()
        {
            return this.record.getFirstPlayerWins();
        }

        @Override
        public String toString()
        {
            final StringBuilder result = new StringBuilder();

            result.append(this.thisCard.name_);
            result.append(" ");
            result.append(this.record.getFirstPlayerWins());
            result.append(" ");
            result.append(this.record.getTotalGames());
            result.append("\n");

            return result.toString();
        }
    }