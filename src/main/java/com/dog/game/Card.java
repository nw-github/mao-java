package com.dog.game;

enum Suit {
    HEARTS, DIAMONDS, SPADES, CLUBS,
};

enum Face {
    A, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING
};

public class Card {
    private final Suit mSuit;
    private final Face mFace;

    public Card(Suit suit, Face face) {
        mSuit = suit;
        mFace = face;
    }

    public Suit getSuit() { return mSuit; }
    
    public Face getFace() { return mFace; }
}
