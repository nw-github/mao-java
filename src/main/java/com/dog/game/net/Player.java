package com.dog.game.net;

import com.dog.game.Deck;
import com.dog.net.Connection;

public class Player {
    private final Deck         cards    = new Deck();
    private final Connection   conn;
    private final String       name;
    private final int          id;
    private boolean            isDealer = false;

    public Player(Connection conn, int id, String name) {
        this.conn = conn;
        this.name = name;
        this.id   = id;
    }

    public Deck getCards() {
        return cards;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public boolean isDealer() {
        return isDealer;
    }

    public Connection getConnection() {
        return conn;
    }

    public void setIsDealer(boolean isDealer) {
        this.isDealer = isDealer;
    }

    @Override
    public String toString() {
        return String.format("%d:%s:%d", getId(), getName(), getCards().size());
    }
}
