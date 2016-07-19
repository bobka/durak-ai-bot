package com.xlab.devsandbox.durak.game.types;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public enum DeckType {

    _36(Arrays.asList(Suit.values()), Arrays.asList(Rank.SIX, Rank.SEVEN, Rank.EIGHT, Rank.NINE, Rank.TEN,
            Rank.JACK, Rank.QUEEN, Rank.KING, Rank.ACE)),

    _52(Arrays.asList(Suit.values()), Arrays.asList(Rank.values()));

    private List<Suit> suits;
    private List<Rank> ranks;

}
