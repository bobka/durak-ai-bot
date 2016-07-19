package com.xlab.devsandbox.durak.game.ai;

import com.xlab.devsandbox.durak.game.entities.Card;
import com.xlab.devsandbox.durak.game.entities.CardsPair;
import com.xlab.devsandbox.durak.game.entities.Table;
import com.xlab.devsandbox.durak.game.utils.Helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AiImpl implements Ai {

    private int terminus = 4;
    private int diff = 4;
    private int maxHand = 6;

    public void setTerminus(int terminus) {
        this.maxHand = terminus;
    }

    @Override
    public Card onAttack(Table table) {
        Card trump = table.getTrump();
        List<Card> myCards = Helper.sortCards(table.getPlayerCards(), trump);
        Boolean isTaking = false;
        if (table.getOpenCards().size() == 0) { // первый ход
            List<Card> playerCards = table.getPlayerCards(); // пробуем найти повторяющиеся карты
            Boolean force = false;
            if (table.getDeckCardsAmount() <= terminus) { // если карт в колоде немного, то ищем с козырями
                force = true;
            }
            List<Card> nonUnique = findNonUniqueRank(playerCards, table.getTrump(), force); // пара не уникальных карт
            if (nonUnique.size() > 1) {
                if (table.getOpponentCardsAmount() >= nonUnique.size()) {
                    Card candidate = nonUnique.get(0);
                    if ((candidate.getRank().getOrder() - myCards.get(0).getRank().getOrder() < diff) || myCards.size() > maxHand) {
                        // если разница парной карты с минимально возможной меньше трех, то ходим с парной
                        // или на руках много карт -  надо скидывать
                        return candidate;
                    }
                } else { // мало карт у противника, накинем ему самую большую нашу
                    return myCards.get(myCards.size() - 1);
                }
            }
            return myCards.get(0);
        } else {
            // последующий ход
            List<Card> intersection = Helper.sortCards(Helper.getCardsIntersection(table), trump);
            if (intersection.size() == 0) { // если нечего подкинуть
                return null;
            } else { // подкидываем
                Card toAttack = null;
                for (CardsPair pairs : table.getOpenCards()) {
                    if (pairs.getAttackingCard() != null) { // ходить той же картой, что ходил ранее
                        List<Card> sameRank = Helper.findByRank(intersection, pairs.getAttackingCard().getRank());
                        sameRank = Helper.sortCards(sameRank, trump);
                        if (sameRank.size() > 0) {
                            toAttack = sameRank.get(0);
                        }
                    }
                    if (pairs.getDefendingCard() != null) { // ходить картой той масти, что бился ранее
                        List<Card> sameSuit = Helper.findBySuit(intersection, pairs.getDefendingCard().getSuit());
                        sameSuit = Helper.sortCards(sameSuit, trump);
                        if (sameSuit.size() > 0 && !pairs.getDefendingCard().getSuit().equals(trump.getSuit())) {
                            toAttack = sameSuit.get(0);
                        }
                    } else {
                        isTaking = true;
                    }
                }

                if (toAttack != null) {// если есть того же номинала или той же масти
                    if (!toAttack.getSuit().equals(trump.getSuit())) {// если не козырь то спокойно подкидываем
                        return toAttack;
                    } else if (table.getDeckCardsAmount() <= terminus && !isTaking) { // если козырь, то смотрим сколько осталось в колоде и не берет ли противник
                        return toAttack;
                    }
                } else {
//                    System.out.println("?");
                }

                intersection = Helper.sortCards(intersection, trump);
                for (Card card : intersection) {
                    if (card.getSuit().equals(trump.getSuit())) {
                        if (table.getDeckCardsAmount() > terminus || isTaking) {
                            // козырь
                            return null;
                        } else {
                            return card;
                        }
                    } else {
                        return card;
                    }
                }
//                System.out.println("End");
                // никогда сюда не попадем
                return null;
            }
        }


    }

    @Override
    public Card onDefence(Table table) {
        Card trump = table.getTrump();
        Card toDefence = Helper.getSmallestDefenceCard(table);
        Card attacker = Helper.getAttackingCard(table);
        if (toDefence != null) {
            List<Card> candidates = findDefenceCards(table.getPlayerCards(), attacker, trump);
            candidates = Helper.sortCards(candidates, trump);
            for (Card candidate : candidates) {
                for (CardsPair cardsPair : table.getOpenCards()) {
                    if (cardsPair.getAttackingCard() != null && candidate.getRank().equals(cardsPair.getAttackingCard().getRank())
                            || cardsPair.getDefendingCard() != null && candidate.getRank().equals(cardsPair.getDefendingCard().getRank())) {
                        if (!candidate.getSuit().equals(trump.getSuit()))
                            return candidate;
                    }
                }
            }
            // не нашли парных или кандидат на защиту - козырь
            if (toDefence.getSuit().equals(trump.getSuit())) { // если предлагает биться козырем
                if (!attacker.getSuit().equals(trump.getSuit()) &&
                        (table.getDeckCardsAmount() <= terminus
                                || (toDefence.getRank().getOrder() < (diff * 2 - 1)))
//                        || (attacker.getRank().getOrder() < diff * 2 && toDefence.getRank().getOrder() < diff * 2)
//                        || ((attacker.getRank().getOrder() - toDefence.getRank().getOrder()) < diff)
                        ) {
                    // если козырь мелкий или конец игры
                    return toDefence;
                } else if (attacker.getSuit().equals(trump.getSuit())) {
                    return toDefence;
                } else {
                    return null;
                }
            }
            return toDefence;
        } else {
            return null;
        }
    }


    private List<Card> findDefenceCards(List<Card> playerCards, Card attacker, Card trump) {
        ArrayList<Card> result = new ArrayList<>();
        for (Card player : playerCards) {
            if (player.getSuit().equals(attacker.getSuit())
                    && player.getRank().ordinal() > attacker.getRank().ordinal()) {
                result.add(player);
            } else {
                if (player.getSuit().equals(trump.getSuit())) {// если у нас козырь, а у атакующего не козырь
                    result.add(player);
                }
            }
        }
        return result;
    }

    private List<Card> findNonUniqueRank(List<Card> cards, Card trump, Boolean force) {
        List<Card> result = new ArrayList<>();
        Map<Integer, List<Card>> map = new HashMap<>();
        for (Card card : cards) {
            if (!card.getSuit().equals(trump.getSuit()) || force) {
                Integer order = card.getRank().getOrder();
                if (map.get(order) != null) {
                    map.get(order).add(card);
                } else {
                    ArrayList tmp = new ArrayList(1);
                    tmp.add(card);
                    map.put(order, tmp);
                }
            }
        }
        int tmpSize = 0;
        int currentRank = 15;
        for (Map.Entry<Integer, List<Card>> entry : map.entrySet()) {
            if (entry.getValue().size() > tmpSize || entry.getValue().get(0).getRank().getOrder() < currentRank) {
                result = entry.getValue();
                tmpSize = entry.getValue().size();
                currentRank = entry.getValue().get(0).getRank().getOrder();
            }
        }
        return result;
    }

}
