package com.xlab.devsandbox.durak.game.entities;

import com.xlab.devsandbox.durak.game.types.GameResultType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GameResults {

    private Player winner;
    private Player loser;
    private GameResultType resultType;
    private List<String> logs;

}
