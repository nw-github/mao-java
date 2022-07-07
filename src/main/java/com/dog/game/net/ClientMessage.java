package com.dog.game.net;

// Client -> Server
public enum ClientMessage {
    REGISTER,
    PLAY,       // Card card, String text
    PUNISH,     // int target, String reason
};