package com.xlab.devsandbox.durak.game.utils;

import com.xlab.devsandbox.durak.game.entities.Card;
import com.xlab.devsandbox.durak.game.entities.CardsPair;
import com.xlab.devsandbox.durak.game.entities.Table;
import com.xlab.devsandbox.durak.game.types.Rank;
import com.xlab.devsandbox.durak.game.types.Suit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Helper {

    public static CardsPair getIncompletePair(Table table) {
        for (CardsPair pair : table.getOpenCards()) {
            if (!pair.isComplete()) {
                return pair;
            }
        }
        return null;
    }

    public static Card getAttackingCard(Table table) {
        CardsPair pair = getIncompletePair(table);
        return pair == null ? null : pair.getAttackingCard();
    }

    public static Card getSmallestTrump(List<Card> cards, Card trump) {
        Card smallestTrump = null;
        for (Card card : cards) {
            if (card.getSuit().equals(trump.getSuit())) {
                if (smallestTrump == null) {
                    smallestTrump = card;
                }
                else if (smallestTrump.getRank().getOrder() > card.getRank().getOrder()) {
                    smallestTrump = card;
                }
            }
        }
        return smallestTrump;
    }

    public static List<Card> sortCards(List<Card> cards, Card trump) {
        List<Card> cardsToSort = new ArrayList<>(cards);

        Collections.sort(cardsToSort);

        List<Card> sortedRegularCards = new ArrayList<>();
        List<Card> sortedTrumpCards = new ArrayList<>();
        for (Card card : cardsToSort) {
            if (!card.getSuit().equals(trump.getSuit())) {
                sortedRegularCards.add(card);
            }
            else {
                sortedTrumpCards.add(card);
            }
        }

        List<Card> result = new ArrayList<>();
        result.addAll(sortedRegularCards);
        result.addAll(sortedTrumpCards);

        return result;
    }

    public static List<Card> getCardsIntersection(Table table) {
        List<Card> playerCards = table.getPlayerCards();
        List<Card> openCards = new ArrayList<>();

        for (CardsPair pair : table.getOpenCards()) {
            openCards.add(pair.getAttackingCard());
            Card defence = pair.getDefendingCard();
            if (defence != null) {
                openCards.add(defence);
            }
        }

        Set<Rank> ranks = new HashSet<>();
        for (Card openCard : openCards) {
            ranks.add(openCard.getRank());
        }

        List<Card> result = new ArrayList<>();
        for (Rank rank : ranks) {
            List<Card> cards = findByRank(playerCards, rank);
            if (cards.size() > 0) {
                result.addAll(cards);
            }
        }

        return result;
    }

    public static List<Card> findByRank(List<Card> cards, Rank rank) {
        List<Card> result = new ArrayList<>();

        for (Card card : cards) {
            if (card.getRank().equals(rank)) {
                result.add(card);
            }
        }

        return result;
    }

    public static List<Card> findBySuit(List<Card> cards, Suit suit) {
        List<Card> result = new ArrayList<>();

        for (Card card : cards) {
            if (card.getSuit().equals(suit)) {
                result.add(card);
            }
        }

        return result;
    }

    public static Card getSmallestDefenceCard(Table table) {
        Card trump = table.getTrump();
        List<Card> playerCards = sortCards(table.getPlayerCards(), trump);
        Card attackingCard = getAttackingCard(table);

        if (attackingCard.getSuit().equals(trump.getSuit())) {
            for (Card card : playerCards) {
                if (card.getSuit().equals(trump.getSuit())
                        && card.getRank().getOrder() > attackingCard.getRank().getOrder()) {

                    return card;
                }
            }
        }
        else {
            for (Card card : playerCards) {
                if (card.getSuit().equals(attackingCard.getSuit())
                        && card.getRank().getOrder() > attackingCard.getRank().getOrder()) {

                    return card;
                }
            }
            return getSmallestTrump(playerCards, trump);
        }

        return null;
    }
}
