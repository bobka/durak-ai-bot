package com.xlab.devsandbox.durak.game.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class CardsPair {

    @Getter
    private final Card attackingCard;

    @Getter
    private final Card defendingCard;

    public boolean isComplete() {
        return attackingCard != null && defendingCard != null;
    }
}
