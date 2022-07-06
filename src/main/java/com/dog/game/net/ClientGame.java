package com.dog.game.net;

import java.util.*;

import com.dog.game.*;
import com.fasterxml.jackson.annotation.JsonCreator;

public class ClientGame {
    private final Map<Integer, ClientPlayer> players;
    private final Deck cards;
    private final int localId;
    private Card discardTop;
    private int drawDeck;

    @JsonCreator
    private ClientGame() { players = null; cards = null; localId = 0; }

    public ClientGame(Game game, Player player) {
        this.localId   = player.getId();
        this.cards     = player.getCards();
        this.discardTop = game.getDiscardTop();
        this.drawDeck   = game.getDrawDeckSize();
        this.players    = new HashMap<Integer, ClientPlayer>();
        for (var pl : game.getPlayers())
            this.players.put(pl.getId(), new ClientPlayer(pl));
    }

    public void addCard(ClientPlayer player, Card card) throws IllegalArgumentException {
        player.addCard();
        if (isLocalPlayer(player)) {
            if (card == null)
                throw new IllegalArgumentException("Card cannot be null if adding to the local player.");
            cards.add(0, card);
        }
    }

    public void removeCard(ClientPlayer player, Card card) throws IllegalArgumentException {
        player.removeCard();
        if (isLocalPlayer(player))
            cards.remove(card);
    }

    public ClientPlayer addPlayer(ClientPlayer player) throws IllegalArgumentException {
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
    
    public boolean isLocalPlayer(ClientPlayer player) {
        return player.getId() == localId;
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

    public Card getDiscardTop() {
        return discardTop;
    }

    public void setDiscardTop(Card card) {
        discardTop = card;
    }

    public int getLocalId() {
        return localId;
    }
}
