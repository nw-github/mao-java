package com.dog.game.net;

// Server -> Client
public enum ServerMessage {
    ACCEPTED,        // ClientGame
    PLAYER_JOIN,     // String player
    PLAYER_LEAVE,    // int id
    GAME_START,      // ClientGame

    RECV_CARD,       // int id, Card card, String reason, int newDeckSize
    PLAY,            // int id, String text, Card played
    GAME_END,        // int victorId
};