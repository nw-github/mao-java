package com.dog.game;

import java.util.ArrayList;
import java.util.Collections;

public class Deck extends ArrayList<Card> {
    public void take(Deck source, int index) {
        add(0, source.remove(index));
    }

    public void take(Deck source) {
        take(source, 0);
    }

    public void shuffle() {
        Collections.shuffle(this);
    }

    public Card top() {
        return get(0);
    }
}