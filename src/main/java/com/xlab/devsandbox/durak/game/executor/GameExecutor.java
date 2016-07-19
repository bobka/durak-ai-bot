package com.xlab.devsandbox.durak.game.executor;

import com.xlab.devsandbox.durak.game.entities.Card;
import com.xlab.devsandbox.durak.game.entities.CardsPair;
import com.xlab.devsandbox.durak.game.entities.Deck;
import com.xlab.devsandbox.durak.game.entities.GameResults;
import com.xlab.devsandbox.durak.game.entities.Player;
import com.xlab.devsandbox.durak.game.entities.Table;
import com.xlab.devsandbox.durak.game.types.DeckType;
import com.xlab.devsandbox.durak.game.types.GameResultType;
import com.xlab.devsandbox.durak.game.types.PlayerRole;
import com.xlab.devsandbox.durak.game.types.Rank;
import com.xlab.devsandbox.durak.game.types.Suit;
import com.xlab.devsandbox.durak.game.utils.DeckGenerator;
import com.xlab.devsandbox.durak.game.utils.Helper;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GameExecutor {

    private static final int CARDS_AMOUNT_IN_HAND = 6;

    private Deck deck;

    private Long playerTimeout;

    public GameExecutor() {
        deck = DeckGenerator.generateDeck(DeckType._36);
        playerTimeout = 10000L;
    }

    public GameExecutor(Long playerTimeout) {
        deck = DeckGenerator.generateDeck(DeckType._36);
        this.playerTimeout = playerTimeout;
    }

    public GameResults playGame(Player playerOne, Player playerTwo) {
        Map<String, Long> gameTime = new HashMap<>();
        List<String> log = new LinkedList<>();

        List<Card> playerOneCards = deck.dealCards(CARDS_AMOUNT_IN_HAND);
        List<Card> playerTwoCards = deck.dealCards(CARDS_AMOUNT_IN_HAND);

        Card trump = deck.getCards().get(deck.getCardsAmount() - 1);
        log.add("Cards in Deck: " + cardsListToString(deck.getCards()));
        log.add("Trump: " + trump);

        boolean turnsOrder = getTurnsOrder(playerOneCards, playerTwoCards, trump);

        Player attacker = turnsOrder ? playerOne : playerTwo;
        List<Card> attackerCards = turnsOrder ? playerOneCards : playerTwoCards;

        Player defender = turnsOrder ? playerTwo : playerOne;
        List<Card> defenderCards = turnsOrder ? playerTwoCards : playerOneCards;

        gameTime.put(attacker.getLogin(), 0L);
        gameTime.put(defender.getLogin(), 0L);

        List<Card> discardPile = new ArrayList<>();

        // end game if someone run out of cards
        while (attackerCards.size() > 0 && defenderCards.size() > 0) {
            log.add("> Round Start");

            log.add(String.format("Attacker '%s' cards: %s", attacker.getLogin(), cardsListToString(attackerCards)));
            log.add(String.format("Defender '%s' cards: %s", defender.getLogin(), cardsListToString(defenderCards)));

            Table table = null;
            boolean skipDefence = false;

            int movesInRound = CARDS_AMOUNT_IN_HAND;
            if (defenderCards.size() < CARDS_AMOUNT_IN_HAND) {
                movesInRound = defenderCards.size();
                log.add("Defender has not enough card for full round, decreased to: " + movesInRound);
            }

            // max 6 moves are allowed
            for (int move = 0; move < movesInRound; move++) {
                log.add(">> Move #" + (move + 1));

                table = (table == null)
                        ? new Table(Table.TableState.builder()
                            .trump(trump)
                            .playerCards(attackerCards)
                            .discardPileCards(discardPile)
                            .deckCardsAmount(deck.getCardsAmount())
                            .opponentCardsAmount(defenderCards.size())
                            .build())

                        : new Table(Table.TableState.builder()
                            .trump(trump)
                            .playerCards(attackerCards)
                            .openCards(table.getOpenCards())
                            .discardPileCards(discardPile)
                            .deckCardsAmount(deck.getCardsAmount())
                            .opponentCardsAmount(defenderCards.size())
                            .build());

                Card attackerCard;
                long attackerMoveStartTime = System.currentTimeMillis();
                try {
                    attackerCard = attacker.getAi().onAttack(table);
                }
                catch (Throwable e) {
                    e.printStackTrace();
                    return new GameResults(defender, attacker, GameResultType.PENALTY, log);
                }
                long attackerMoveEndTime = System.currentTimeMillis();
                if (isTimeout(gameTime, attacker, attackerMoveEndTime - attackerMoveStartTime)) {
                    log.add(String.format("Timeout for player '%s'", attacker.getLogin()));
                    return new GameResults(defender, attacker, GameResultType.PENALTY, log);
                }

                if (attackerCard != null) {
                    log.add(String.format("Attacker '%s' put card: %s", attacker.getLogin(), attackerCard));
                }
                else {
                    log.add("No suitable cards for attack.");
                }

                // lose immediately after wrong move
                if (!isCardValid(PlayerRole.ATTACKER, attackerCard, attackerCards, table)) {
                    log.add("Card is invalid. Game over.");
                    return new GameResults(defender, attacker, GameResultType.PENALTY, log);
                }

                if (attackerCard == null) {
                    break;
                }

                attackerCards.remove(attackerCard);

                // put card on the table
                CardsPair pair = new CardsPair(attackerCard, null);
                List<CardsPair> openCardsPairs = new ArrayList<>(table.getOpenCards());
                openCardsPairs.add(pair);
                table = new Table(Table.TableState.builder()
                        .trump(trump)
                        .playerCards(defenderCards)
                        .openCards(openCardsPairs)
                        .discardPileCards(discardPile)
                        .deckCardsAmount(deck.getCardsAmount())
                        .opponentCardsAmount(attackerCards.size())
                        .build());

                if (skipDefence) {
                    defenderCards.add(attackerCard);
                }
                else {
                    Card defenderCard;
                    long defenderMoveStartTime = System.currentTimeMillis();
                    try {
                        defenderCard = defender.getAi().onDefence(table);
                    }
                    catch (Throwable e) {
                        e.printStackTrace();
                        return new GameResults(attacker, defender, GameResultType.PENALTY, log);
                    }
                    long defenderMoveEndTime = System.currentTimeMillis();
                    if (isTimeout(gameTime, attacker, defenderMoveEndTime - defenderMoveStartTime)) {
                        log.add(String.format("Timeout for player '%s'", defender.getLogin()));
                        return new GameResults(attacker, defender, GameResultType.PENALTY, log);
                    }

                    if (defenderCard != null) {
                        log.add(String.format("Defender '%s' put card: %s", defender.getLogin(), defenderCard));
                    }
                    else {
                        log.add("No suitable cards for defense.");
                    }

                    // lose immediately after wrong move
                    if (!isCardValid(PlayerRole.DEFENDER, defenderCard, defenderCards, table)) {
                        log.add("Card is invalid. Game over.");
                        return new GameResults(attacker, defender, GameResultType.PENALTY, log);
                    }

                    if (defenderCard == null) {
                        for (CardsPair pairToTake : openCardsPairs) {
                            defenderCards.add(pairToTake.getAttackingCard());
                            Card defenderCardToTake = pairToTake.getDefendingCard();
                            if (defenderCardToTake != null) {
                                defenderCards.add(defenderCardToTake);
                            }
                        }
                        skipDefence = true;
                    }
                    else {
                        defenderCards.remove(defenderCard);

                        // put card on the table
                        pair = Helper.getIncompletePair(table);
                        openCardsPairs = new ArrayList<>(table.getOpenCards());
                        openCardsPairs.remove(pair);
                        openCardsPairs.add(new CardsPair(pair.getAttackingCard(), defenderCard));
                        table = new Table(Table.TableState.builder()
                                .trump(trump)
                                .openCards(openCardsPairs)
                                .build());
                    }
                }

                if (attackerCards.size() == 0 || defenderCards.size() == 0) {
                    break;
                }
            }

            if (attackerCards.size() != 0 && defenderCards.size() != 0 && !skipDefence) {
                List<CardsPair> discardedCardPairs = table.getOpenCards();
                for (CardsPair pair : discardedCardPairs) {
                    discardPile.add(pair.getAttackingCard());
                    discardPile.add(pair.getDefendingCard());
                }
            }

            if (deck.getCardsAmount() > 0) {
                log.add("Players are taking cards..");

                // add cards to hands
                List<Card> newAttackerCards = deck.dealCards(CARDS_AMOUNT_IN_HAND - attackerCards.size());
                if (!newAttackerCards.isEmpty()) {
                    log.add("Attacker new cards: " + cardsListToString(newAttackerCards));
                }
                attackerCards.addAll(newAttackerCards);

                List<Card> newDefenderCards = deck.dealCards(CARDS_AMOUNT_IN_HAND - defenderCards.size());
                if (!newDefenderCards.isEmpty()) {
                    log.add("Defender new cards: " + cardsListToString(newDefenderCards));
                }
                defenderCards.addAll(newDefenderCards);

                log.add("Cards left in Deck: " + deck.getCardsAmount());
            }

            if (skipDefence) {
                log.add("No roles swap.");
            }
            else {
                log.add("Swapping roles..");

                // swap players
                Pair<Player, Player> swapAIs = swap(attacker, defender);
                attacker = swapAIs.getLeft();
                defender = swapAIs.getRight();

                // swap cards
                Pair<List<Card>, List<Card>> swapCards = swap(attackerCards, defenderCards);
                attackerCards = swapCards.getLeft();
                defenderCards = swapCards.getRight();
            }
        }

        if (attackerCards.size() == 0 && defenderCards.size() == 0) {
            log.add("Game result is draw.");
            // no matter who is winner/loser, rating will not be changed
            return new GameResults(attacker, defender, GameResultType.DRAW, log);
        }
        else {
            GameResults result = attackerCards.size() == 0
                    ? new GameResults(attacker, defender, GameResultType.REGULAR, log)
                    : new GameResults(defender, attacker, GameResultType.REGULAR, log);

            log.add("> Game over.");
            log.add(">> Winner: " + result.getWinner().getLogin());
            log.add(">> Loser: " + result.getLoser().getLogin());

            return result;
        }
    }

    public boolean isCardValid(PlayerRole role, Card card, List<Card> playerCards, Table table) {
        if (card != null && !playerCards.contains(card)) {
            return false;
        }

        if (card != null && !card.getClass().equals(Card.class)) {
            return false;
        }

        List<CardsPair> openCards = table.getOpenCards();

        // attacker can add only cards with the same ranks as on table
        if (role.equals(PlayerRole.ATTACKER)) {
            if (openCards.size() > 0) {
                if (card == null) {
                    return true;
                }

                for (CardsPair pair : openCards) {
                    Rank attackingCardRank = pair.getAttackingCard().getRank();
                    Card defendingCard = pair.getDefendingCard();
                    Rank defendingCardRank = (defendingCard == null) ? null : defendingCard.getRank();

                    if (card.getRank().equals(attackingCardRank) || card.getRank().equals(defendingCardRank)) {
                        return true;
                    }
                }
                return false;
            }
            else {
                // attacker cannot start round without a card
                if (card == null) {
                    return false;
                }
            }
        }
        // defender must beat correctly attacker card
        else {
            if (card == null) {
                return true;
            }

            for (CardsPair pair : openCards) {
                if (!pair.isComplete()) {
                    Card attackerCard = pair.getAttackingCard();
                    Suit trumpSuit = table.getTrump().getSuit();

                    // defender must beat trump
                    if (attackerCard.getSuit().equals(trumpSuit)
                            && (!card.getSuit().equals(trumpSuit) || card.getRank().getOrder() < attackerCard.getRank().getOrder())) {

                        return false;
                    }
                    // defender must beat non-trump
                    else if (!(card.getSuit().equals(attackerCard.getSuit()) && card.getRank().getOrder() > attackerCard.getRank().getOrder())
                            && !card.getSuit().equals(trumpSuit)) {

                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Return true if order is following: playerOne then playerTwo, otherwise return false
     */
    public boolean getTurnsOrder(List<Card> playerOneCards, List<Card> playerTwoCards, Card trump) {
        Card playerOneTrump = Helper.getSmallestTrump(playerOneCards, trump);
        Card playerTwoTrump = Helper.getSmallestTrump(playerTwoCards, trump);

        // no one has a trump
        if (playerOneTrump == null && playerTwoTrump == null) {
            Random random = new Random(System.currentTimeMillis());
            return (random.nextInt(100) > 50);
        }
        // both have trumps
        else if (playerOneTrump != null && playerTwoTrump != null) {
            int playerOneTrumpRank = playerOneTrump.getRank().getOrder();
            int playerTwoTrumpRank = playerTwoTrump.getRank().getOrder();
            return (playerOneTrumpRank < playerTwoTrumpRank);
        }
        // only one has a trump
        else {
            return (playerOneTrump != null);
        }
    }

    public <T> Pair<T, T> swap(T objectOne, T objectTwo) {
        return Pair.of(objectTwo, objectOne);
    }

    private String cardsListToString(List<Card> cards) {
        StringBuilder builder = new StringBuilder();
        Iterator<Card> iterator = cards.iterator();
        while (iterator.hasNext()) {
            Card card = iterator.next();
            builder.append(card.toString());
            if (iterator.hasNext()) {
                builder.append(" ");
            }
        }
        return builder.toString();
    }

    private boolean isTimeout(Map<String, Long> gameTime, Player player, Long turnTime) {
        String login = player.getLogin();
        Long playerTime = gameTime.get(login);
        Long newTime = playerTime + turnTime;
        if (newTime > playerTimeout) {
            return true;
        }
        else {
            gameTime.put(login, newTime);
            return false;
        }
    }
}
