package com.dog.game.net;

import java.util.*;

import com.dog.game.*;
import com.dog.net.DeserializationException;
import com.dog.net.Message;

public class ClientGame {
    private final Map<Integer, ClientPlayer> mPlayers = new LinkedHashMap<>();
    private final Deck mCards = new Deck();
    private final int mId;
    private Card mTop;
    private int mDrawDeck;

    public ClientGame(Message message) throws DeserializationException {
        mId         = message.readInt();
        var players = message.readString();
        var cards   = message.readString();
        var top     = message.readString();
        mTop        = top.isEmpty() ? null : Card.fromString(top);
        mDrawDeck   = message.readInt();

        for (var player : players.split(";")) {
            if (player.isEmpty())
                continue;

            if (addPlayer(player) == null)
                System.out.printf("Failed parsing player string '%s'\n", player);
        }

        var player = getPlayer(mId);
        player.setCards(0);
        for (var card : cards.split(";")) {
            if (card.isEmpty())
                continue;
            
            if (addCard(player, card) == null)
                System.out.printf("Failed parsing card string '%s'\n", card);
        }
    }

    public Card addCard(ClientPlayer player, String source) throws IllegalArgumentException {
        player.addCard();
        if (isMyPlayer(player)) {
            var card = Card.fromString(source);
            mCards.add(0, card);
            return card;
        }

        return null;
    }

    public void removeCard(ClientPlayer player, String source) throws IllegalArgumentException {
        player.removeCard();
        if (isMyPlayer(player))
            mCards.remove(Card.fromString(source));
    }

    public ClientPlayer addPlayer(String source) throws IllegalArgumentException {
        var parts = source.split(":");
        if (parts.length != 3)
            throw new IllegalArgumentException(String.format("Failed parsing player from source string '%s'\n", source));

        try {
            var player = new ClientPlayer(Integer.parseInt(parts[0]), parts[1], Integer.parseInt(parts[2]));
            mPlayers.put(player.getId(), player);
            return player;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(String.format("Failed parsing player from source string '%s'\n", source));
        }
    }

    public ClientPlayer removePlayer(int id) throws IllegalArgumentException {
        if (!mPlayers.containsKey(id))
            throw new IllegalArgumentException(String.format("Cannot remove player: ID '%d' does not correspond to a player.\n", id));

        return mPlayers.remove(id);
    }

    public ClientPlayer getPlayer(int id) throws IllegalArgumentException {
        if (!mPlayers.containsKey(id))
            throw new IllegalArgumentException(String.format("Cannot get player: ID '%d' does not correspond to a player.\n", id));

        return mPlayers.get(id);
    }
    
    public int getDrawDeck() {
        return mDrawDeck;
    }

    public void setDrawDeck(int newCount) {
        mDrawDeck = newCount;
    }

    public Map<Integer, ClientPlayer> getPlayers() {
        return mPlayers;
    }

    public Deck getCards() {
        return mCards;
    }

    public Card getTopCard() {
        return mTop;
    }

    public void setTopCard(Card card) {
        mTop = card;
    }

    public boolean isMyPlayer(ClientPlayer player) {
        return player.getId() == mId;
    }

    public int getMyId() {
        return mId;
    }

    // Server

    public static Message fromGame(Object type, Card top, Collection<Player> players, int drawDeck, Player player) {
        var cardsBuilder = new StringBuilder();
        if (player != null) {
            for (var card : player.getCards()) {
                cardsBuilder.append(card.toString());
                cardsBuilder.append(';');
            }
        }

        var playersBuilder = new StringBuilder();
        if (players != null) {
            for (var pl : players) {
                playersBuilder.append(pl.toString());
                playersBuilder.append(';');
            }
        }

        return new Message(type)
            .withInt(player.getId())
            .withString(playersBuilder.toString())
            .withString(cardsBuilder.toString())
            .withString(top != null ? top.toString() : "")
            .withInt(drawDeck);
    }

    public static Message fromGame(Object type, Game game, Player player) {
        return fromGame(type,
            game.getPileTop(),
            game.getPlayers(),
            game.getDrawDeckSize(),
            player);
    }
}
