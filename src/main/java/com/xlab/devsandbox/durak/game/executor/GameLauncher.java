package com.xlab.devsandbox.durak.game.executor;

import com.xlab.devsandbox.durak.game.ai.AiImpl;
import com.xlab.devsandbox.durak.game.ai.BotAi;
import com.xlab.devsandbox.durak.game.entities.GameResults;
import com.xlab.devsandbox.durak.game.entities.Player;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class GameLauncher {

    public static void main(String[] args) {
        try {
            Map<String, BigDecimal> results = new HashMap<>(2);
//            for (int j = 6; j < 10; j++) {
//                System.out.println("\nTry " + j);
                AiImpl impl = new AiImpl();
//                impl.setTerminus(j);
                Player p1 = new Player("bobka_rus", impl);
                Player p2 = new Player("bot", new BotAi());

                results.put(p1.getLogin(), BigDecimal.ZERO);
                results.put(p2.getLogin(), BigDecimal.ZERO);

                for (int i = 0; i < 100000; i++) {
                    GameExecutor gameExecutor = new GameExecutor();
                    GameResults result = gameExecutor.playGame(p1, p2);

//            for (String logRecord : result.getLogs()) {
//                System.out.println(logRecord);
//            }
                    results.put(result.getWinner().getLogin(), results.get(result.getWinner().getLogin()).add(BigDecimal.ONE));
                }
                System.out.println("\nStatistics: ");
                System.out.println(p1.getLogin() + ": " + results.get(p1.getLogin()));
                System.out.println(p2.getLogin() + ": " + results.get(p2.getLogin()));

//                System.out.println("Coefficient " + results.get(p1.getLogin()).divideAndRemainder(BigDecimal.valueOf(100000))[0] + "." + results.get(p1.getLogin()).divideAndRemainder(BigDecimal.valueOf(100000))[1]);
//            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
