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

    public ClientGame(Message message) throws DeserializationException, IllegalArgumentException {
        mId         = message.readInt();
        var players = message.readString();
        var cards   = message.readString();
        var top     = message.readString();
        mTop        = top.isEmpty() ? null : Card.fromNetString(top);
        mDrawDeck   = message.readInt();

        for (var player : players.split(";")) {
            if (player.isEmpty())
                continue;

            addPlayer(player);
        }

        var player = getPlayer(mId);
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
            mCards.add(0, card);
            return card;
        }

        return null;
    }

    public void removeCard(ClientPlayer player, String source) throws IllegalArgumentException {
        player.removeCard();
        if (isMyPlayer(player))
            mCards.remove(Card.fromNetString(source));
    }

    public ClientPlayer addPlayer(String source) throws IllegalArgumentException {
        var player = new ClientPlayer(source);
        mPlayers.put(player.getId(), player);
        return player;
    }

    public ClientPlayer removePlayer(int id) throws IllegalArgumentException {
        if (!mPlayers.containsKey(id))
            throw new IllegalArgumentException(String.format("Cannot remove player: ID '%d' does not correspond to a player.", id));

        return mPlayers.remove(id);
    }

    public ClientPlayer getPlayer(int id) throws IllegalArgumentException {
        if (!mPlayers.containsKey(id))
            throw new IllegalArgumentException(String.format("Cannot get player: ID '%d' does not correspond to a player.", id));

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
