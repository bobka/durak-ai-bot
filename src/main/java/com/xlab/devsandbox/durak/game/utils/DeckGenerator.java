package com.xlab.devsandbox.durak.game.utils;

import com.xlab.devsandbox.durak.game.entities.Card;
import com.xlab.devsandbox.durak.game.entities.Deck;
import com.xlab.devsandbox.durak.game.types.DeckType;
import com.xlab.devsandbox.durak.game.types.Rank;
import com.xlab.devsandbox.durak.game.types.Suit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeckGenerator {

    public static Deck generateDeck(DeckType type) {
        List<Card> cards = new ArrayList<>();

        for (Suit suit : type.getSuits()) {
            for (Rank rank : type.getRanks()) {
                cards.add(new Card(suit, rank));
            }
        }

        Collections.shuffle(cards);
        return new Deck(cards, type);
    }
}
