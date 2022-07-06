package com.dog.game;

import java.util.ArrayList;
import java.util.Collections;

public class Deck extends ArrayList<Card> {
    public Card take(Deck source, int index) {
        var card = source.remove(index);
        add(0, card);
        return card;
    }

    public Card take(Deck source) {
        return take(source, 0);
    }

    public void shuffle() {
        Collections.shuffle(this);
    }

    public Card top() {
        return get(0);
    }
}