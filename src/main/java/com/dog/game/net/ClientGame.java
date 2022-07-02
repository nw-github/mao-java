package com.dog.game.net;

import java.util.*;

import com.dog.game.Card;
import com.dog.game.Game;
import com.dog.net.DeserializationException;
import com.dog.net.Message;

class ClientPlayer {
    private final int mId;
    private final String mName;
    private int mCards;

    public ClientPlayer(int id, String name, int cards) {
        mId    = id;
        mName  = name;
        mCards = cards;
    }

    public String getName() {
        return mName;
    }

    public int getId() {
        return mId;
    }

    public int getCards() {
        return mCards;
    }

    public void setCards(int cards) {
        mCards = cards;
    }
}

public class ClientGame {
    private final Map<Integer, ClientPlayer> mPlayers = new LinkedHashMap<>();
    private final List<Card> mCards = new ArrayList<>();
    private final Card mTop;
    private final int mId;

    public ClientGame(Message message) throws DeserializationException {
        mId         = message.readInt();
        var players = message.readString();
        var cards   = message.readString();
        var top     = message.readString();
        mTop        = top.isEmpty() ? null : Card.fromString(top);

        for (var player : players.split(";")) {
            if (player.isEmpty())
                continue;

            if (addPlayer(player) == null)
                System.out.printf("Failed parsing player string '%s'\n", player);
        }

        for (var card : cards.split(";")) {
            if (card.isEmpty())
                continue;

            if (addCard(card) == null)
                System.out.printf("Failed parsing card string '%s'\n", card);
        }
    }

    public Card addCard(String source) {
        try {
            var card = Card.fromString(source);
            mCards.add(card);
            return card;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public ClientPlayer addPlayer(String source) {
        var parts = source.split(":");
        if (parts.length != 3)
            return null;

        try {
            var player = new ClientPlayer(Integer.parseInt(parts[0]), parts[1], Integer.parseInt(parts[2]));
            mPlayers.put(player.getId(), player);
            return player;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public ClientPlayer removePlayer(int id) {
        return mPlayers.remove(id);
    }



    public int getId() {
        return mId;
    }

    public Map<Integer, ClientPlayer> getPlayers() {
        return mPlayers;
    }

    public List<Card> getCards() {
        return mCards;
    }

    public Card getTopCard() {
        return mTop;
    }

    // Server

    public static Message fromGame(Object type, Card top, Collection<Player> players, Player player) {
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
            .withString(top != null ? top.toString() : "");
    }

    public static Message fromGame(Object type, Game game, Player player) {
        return fromGame(type, game.getPileTop(), Arrays.asList(game.getPlayers()), player);
    }
}
