package com.xlab.devsandbox.durak.game.ai;

import com.xlab.devsandbox.durak.game.entities.Card;
import com.xlab.devsandbox.durak.game.entities.Table;
import com.xlab.devsandbox.durak.game.utils.Helper;

import java.util.List;

public class BotAi implements Ai {

    @Override
    public Card onAttack(Table table) {
        Card card = null;
        Card trump = table.getTrump();

        if (table.getOpenCards().size() == 0) {
            card = Helper.sortCards(table.getPlayerCards(), trump).get(0);
        }
        else {
            List<Card> cards = Helper.sortCards(Helper.getCardsIntersection(table), trump);
            if (cards.size() > 0) {
                card = cards.get(0).getSuit().equals(trump.getSuit()) ? null : cards.get(0);
            }
        }
        return card;
    }

    @Override
    public Card onDefence(Table table) {
        return Helper.getSmallestDefenceCard(table);
    }
}
