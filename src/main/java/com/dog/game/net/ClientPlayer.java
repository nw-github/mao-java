package com.dog.game.net;

public class ClientPlayer {
    private final int id;
    private final String name;
    private int cards;

    // see Player::toString()
    public ClientPlayer(String playerString) throws IllegalArgumentException {
        var parts = playerString.split(":");
        if (parts.length != 3)
            throw new IllegalArgumentException("The source string was not correctly formatted.");

        try {
            id    = Integer.parseInt(parts[0]);
            name  = parts[1];
            cards = Integer.parseInt(parts[2]);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("The numbers could not be parsed.");
        }
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public int getCards() {
        return cards;
    }

    public void setCards(int cards) {
        this.cards = cards;
    }

    public void removeCard() {
        cards--;
    }

    public void addCard() {
        cards++;
    }
}
