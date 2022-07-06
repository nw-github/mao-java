package com.dog.game;

public record Card(Suit suit, Face face) {
    @Override
    public String toString() {
        return String.format("%s:%s", suit().toString(), face().toString());
    }

    public static Card fromString(String source) throws IllegalArgumentException {
        var parts = source.split(":");
        if (parts.length != 2)
            throw new IllegalArgumentException("The source string cannot be parsed into a Card.");

        return new Card(Suit.valueOf(parts[0]), Face.valueOf(parts[1]));
    }
}
