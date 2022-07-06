package com.dog.game.net;

import com.dog.game.Deck;
import com.dog.net.Connection;

public class Player {
    private final Connection   mConn;
    private final String       mName;
    private final Deck         mCards    = new Deck();
    private final int          mId;
    private boolean            mIsDealer = false;

    public Player(Connection conn, int id, String name) {
        mConn = conn;
        mName = name;
        mId   = id;
    }

    public Deck getCards() {
        return mCards;
    }

    public String getName() {
        return mName;
    }

    public int getId() {
        return mId;
    }

    public boolean isDealer() {
        return mIsDealer;
    }

    public Connection getConnection() {
        return mConn;
    }

    public void setDealer(boolean isDealer) {
        mIsDealer = isDealer;
    }

    @Override
    public String toString() {
        return String.format("%d:%s:%d", getId(), getName(), getCards().size());
    }
}
