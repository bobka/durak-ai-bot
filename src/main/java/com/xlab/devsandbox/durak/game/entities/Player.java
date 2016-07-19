package com.xlab.devsandbox.durak.game.entities;

import com.xlab.devsandbox.durak.game.ai.Ai;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Player {

    private String login;
    private Ai ai;
}
