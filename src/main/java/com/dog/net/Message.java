package com.dog.net;

public record Message(
    String type, byte[] data
) { }
