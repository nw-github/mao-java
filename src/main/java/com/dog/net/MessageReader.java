package com.dog.net;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class MessageReader {
    private static final ObjectMapper JSON = new ObjectMapper();
    private final Message message;
    private int dataPointer = 0;
    
    public MessageReader(Message message) {
        this.message = message;
    }

    public void seek(int pos) {
        dataPointer = pos;
    }

    public byte nextByte() {
        return message.data()[dataPointer++];
    }

    public String readString() throws DeserializationException {
        int length = readInt();
        if (dataPointer + length * 2 > message.data().length)
            throw new DeserializationException("Couldn't deserialize string.");

        var result = new char[length];
        for (int i = 0; i < result.length; i++) {
            result[i] |= nextByte() & 0xFF;
            result[i] |= (nextByte() << 8) & 0xFF;
        }
        return new String(result);
    }

    public int readInt() throws DeserializationException {
        if (dataPointer + 4 > message.data().length)
            throw new DeserializationException("Couldn't deserialize int.");

        int result = 0;
        result |= nextByte() & 0xFF;
        result |= (nextByte() << 8) & 0xFF00;
        result |= (nextByte() << 16) & 0xFF0000;
        result |= (nextByte() << 24) & 0xFF000000;
        return result;
    }

    public<T> T readJson(Class<T> clazz) throws DeserializationException, JsonMappingException, JsonProcessingException {
        return JSON.readValue(readString(), clazz);
    }
}
