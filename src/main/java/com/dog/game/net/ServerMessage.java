package com.dog.game.net;

// Server -> Client
public enum ServerMessage {
    ACCEPTED,     // ClientGame
    PLAYER_JOIN,  // String player
    PLAYER_LEAVE, // int id
    GAME_START,   // ClientGame
};