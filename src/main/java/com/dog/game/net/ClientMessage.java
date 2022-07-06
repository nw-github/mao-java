package com.dog.game.net;

// Client -> Server
public enum ClientMessage {
    REGISTER,
    PLAY,       // int cardIndex /* negative to draw */, String text
    PUNISH,     // int target, String reason
};