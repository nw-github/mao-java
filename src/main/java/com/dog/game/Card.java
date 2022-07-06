package com.dog.game;

import com.dog.Utils;

public record Card(Suit suit, Face face) {
    @Override
    public String toString() {
        return String.format("%s of %s", Utils.toTitleCase(face().toString()), suit().toString().toLowerCase());
    }
}
