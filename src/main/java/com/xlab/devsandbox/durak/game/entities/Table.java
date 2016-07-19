package com.xlab.devsandbox.durak.game.entities;

import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

public class Table {

    @Getter
    private final Card trump;

    @Getter
    private final List<Card> playerCards;

    @Getter
    private final List<Card> discardPileCards;

    @Getter
    private final List<CardsPair> openCards;

    @Getter
    private final Integer deckCardsAmount;

    @Getter
    private final Integer opponentCardsAmount;

    public Table(final TableState state) {
        this.trump = state.trump;

        List<Card> playerCards = state.playerCards;
        this.playerCards = Collections.unmodifiableList(
                playerCards == null ? Collections.<Card>emptyList() : playerCards);

        List<Card> discardPileCards = state.discardPileCards;
        this.discardPileCards = Collections.unmodifiableList(
                discardPileCards == null ? Collections.<Card>emptyList() : discardPileCards);

        List<CardsPair> openCards = state.openCards;
        this.openCards = Collections.unmodifiableList(
                openCards == null ? Collections.<CardsPair>emptyList() : openCards);

        this.deckCardsAmount = state.deckCardsAmount;
        this.opponentCardsAmount = state.opponentCardsAmount;
    }

    @Builder
    public static class TableState {
        private final Card trump;
        private final List<Card> playerCards;
        private final List<Card> discardPileCards;
        private final List<CardsPair> openCards;
        private final Integer deckCardsAmount;
        private final Integer opponentCardsAmount;
    }
}
