package com.dog.game.net;

import com.dog.game.Deck;
import com.dog.net.Connection;

public class Player {
    private final Connection mConn;
    private final String     mName;
    private final Deck       mCards = new Deck();

    public Player(Connection conn, String name) {
        mConn  = conn;
        mName  = name;
    }

    public Deck getCards() {
        return mCards;
    }

    public String getName() {
        return mName;
    }
}
