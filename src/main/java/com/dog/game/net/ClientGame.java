package com.dog.game.net;

import java.util.*;

import com.dog.game.*;
import com.dog.net.DeserializationException;
import com.dog.net.Message;

public class ClientGame {
    private final Map<Integer, ClientPlayer> players = new LinkedHashMap<>();
    private final Deck cards = new Deck();
    private final int id;
    private Card top;
    private int drawDeck;

    public ClientGame(Message message) throws DeserializationException, IllegalArgumentException {
        this.id       = message.readInt();
        var players   = message.readString();
        var cards     = message.readString();
        var top       = message.readString();
        this.top      = top.isEmpty() ? null : Card.fromNetString(top);
        this.drawDeck = message.readInt();

        for (var player : players.split(";")) {
            if (player.isEmpty())
                continue;

            addPlayer(player);
        }

        var player = getPlayer(id);
        player.setCards(0);
        for (var card : cards.split(";")) {
            if (card.isEmpty())
                continue;

            addCard(player, card);
        }
    }

    public Card addCard(ClientPlayer player, String source) throws IllegalArgumentException {
        player.addCard();
        if (isMyPlayer(player)) {
            var card = Card.fromNetString(source);
            cards.add(0, card);
            return card;
        }

        return null;
    }

    public void removeCard(ClientPlayer player, String source) throws IllegalArgumentException {
        player.removeCard();
        if (isMyPlayer(player))
            cards.remove(Card.fromNetString(source));
    }

    public ClientPlayer addPlayer(String source) throws IllegalArgumentException {
        var player = new ClientPlayer(source);
        players.put(player.getId(), player);
        return player;
    }

    public ClientPlayer removePlayer(int id) throws IllegalArgumentException {
        if (!players.containsKey(id))
            throw new IllegalArgumentException(String.format("Cannot remove player: ID '%d' does not correspond to a player.", id));

        return players.remove(id);
    }

    public ClientPlayer getPlayer(int id) throws IllegalArgumentException {
        if (!players.containsKey(id))
            throw new IllegalArgumentException(String.format("Cannot get player: ID '%d' does not correspond to a player.", id));

        return players.get(id);
    }
    
    public int getDrawDeck() {
        return drawDeck;
    }

    public void setDrawDeck(int newCount) {
        drawDeck = newCount;
    }

    public Map<Integer, ClientPlayer> getPlayers() {
        return players;
    }

    public Deck getCards() {
        return cards;
    }

    public Card getTopCard() {
        return top;
    }

    public void setTopCard(Card card) {
        top = card;
    }

    public boolean isMyPlayer(ClientPlayer player) {
        return player.getId() == id;
    }

    public int getMyId() {
        return id;
    }

    public static Message fromGame(Object type, Game game, Player player) {
        var cardsBuilder = new StringBuilder();
        for (var card : player.getCards()) {
            cardsBuilder.append(card.toNetString());
            cardsBuilder.append(';');
        }

        var playersBuilder = new StringBuilder();
        for (var pl : game.getPlayers()) {
            playersBuilder.append(pl.toString());
            playersBuilder.append(';');
        }

        var top = game.getDiscardTop();
        return new Message(type)
            .withInt(player.getId())
            .withString(playersBuilder.toString())
            .withString(cardsBuilder.toString())
            .withString(top != null ? top.toNetString() : "")
            .withInt(game.getDrawDeckSize());
    }
}
