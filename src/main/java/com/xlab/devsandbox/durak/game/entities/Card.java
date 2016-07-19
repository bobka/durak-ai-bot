package com.xlab.devsandbox.durak.game.entities;

import com.xlab.devsandbox.durak.game.types.Rank;
import com.xlab.devsandbox.durak.game.types.Suit;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Card implements Comparable<Card> {

    @Getter
    private Suit suit;

    @Getter
    private Rank rank;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (obj.getClass().equals(Card.class)) {
            Card card = (Card) obj;
            return this.getSuit().equals(card.getSuit()) && this.getRank().equals(card.getRank());
        }

        return false;
    }

    @Override
    public String toString() {
        return "[" + rank + " " + suit + "]";
    }

    @Override
    public int compareTo(Card o) {
        return Integer.compare(this.getRank().getOrder(), o.getRank().getOrder());
    }
}
