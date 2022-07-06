package com.dog.game.net;

public class ClientPlayer {
    private final int mId;
    private final String mName;
    private int mCards;

    // see Player::toString()
    public ClientPlayer(String playerString) throws IllegalArgumentException {
        var parts = playerString.split(":");
        if (parts.length != 3)
            throw new IllegalArgumentException("The source string was not correctly formatted.");

        try {
            mId    = Integer.parseInt(parts[0]);
            mName  = parts[1];
            mCards = Integer.parseInt(parts[2]);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("The numbers could not be parsed.");
        }
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
