package com.dog.game;

enum Suit { HEARTS, DIAMONDS, SPADES, CLUBS };
enum Face { A, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING };

public record Card(
    Suit suit,
    Face face
) { }