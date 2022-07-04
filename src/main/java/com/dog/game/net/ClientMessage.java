package com.dog.game.net;

// Client -> Server
public enum ClientMessage {
    REGISTER,
    PLAY,       // int cardIndex
    PUNISH,     // int target, String reason
};