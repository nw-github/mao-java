package com.dog.net;

public record Message(
    String type,
    byte[] data
) {
    public Message(Object type, byte[] data)
    {
        this(type.toString(), data);
    }
}
