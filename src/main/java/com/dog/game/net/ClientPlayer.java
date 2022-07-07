package com.dog.game.net;

import com.fasterxml.jackson.annotation.JsonCreator;

public class ClientPlayer {
    private final int id;
    private final String name;
    private int cards;

    @JsonCreator
    private ClientPlayer() { id = 0; name = null; cards = 0; }

    public ClientPlayer(Player player) {
        this.id    = player.getId();
        this.name  = player.getName();
        this.cards = player.getCards().size();
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
