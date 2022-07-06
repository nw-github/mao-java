package com.dog.game.net;

public class ClientPlayer {
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

    public void removeCard() {
        mCards--;
    }

    public void addCard() {
        mCards++;
    }
}
