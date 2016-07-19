package com.xlab.devsandbox.durak.game.entities;

import com.xlab.devsandbox.durak.game.types.DeckType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class Deck {

    @Getter
    private List<Card> cards;

    @Getter
    private DeckType deckType;

    public List<Card> dealCards(int amount) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            Card card = getCard();
            if (card != null) {
                cards.add(card);
            }
        }
        return cards;
    }

    public Card getCard() {
        if (getCardsAmount() > 0) {
            Card card = getCards().get(0);
            getCards().remove(card);
            return card;
        }
        else {
            return null;
        }
    }

    public int getCardsAmount() {
        return cards.size();
    }
}
