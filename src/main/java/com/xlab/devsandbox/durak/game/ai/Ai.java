package com.xlab.devsandbox.durak.game.ai;

import com.xlab.devsandbox.durak.game.entities.Card;
import com.xlab.devsandbox.durak.game.entities.Table;

public interface Ai {

    /**
     * Called by executor every move when your role is Attacker
     * @param table cards on the table
     * @return attacking card
     */
    Card onAttack(Table table);

    /**
     * Called by executor every move when your role is Defender
     * @param table cards on the table
     * @return defending card
     */
    Card onDefence(Table table);
}
