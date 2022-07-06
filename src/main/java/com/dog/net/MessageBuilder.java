package com.dog.net;

import java.util.Arrays;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MessageBuilder {
    private static final ObjectMapper JSON = new ObjectMapper();
    private final String type;
    private byte[] data;

    public MessageBuilder(String type, byte[] data) {
        this.type = type;
        this.data = data;
    }

    public MessageBuilder(Object type, byte[] data) {
        this(type.toString(), data);
    }

    public MessageBuilder(String type) {
        this(type, null);
    }

    public MessageBuilder(Object type) {
        this(type, null);
    }

    private void appendBytes(byte[] data) {
        if (this.data != null && this.data.length > 0) {
            var result = Arrays.copyOf(this.data, this.data.length + data.length);
            for (int i = 0; i < data.length; i++)
                result[i + this.data.length] = data[i];
            
            this.data = result;
        } else {
            this.data = data;
        }
    }

    public MessageBuilder withString(String data) {
        withInt(data.length());

        var result = new byte[data.length() * 2];
        for (int i = 0; i < result.length; i += 2) {
            result[i]     = (byte)(data.charAt(i / 2) & 0xFF);
            result[i + 1] = (byte)(data.charAt(i / 2) >> 8 & 0xFF);
        }
        
        appendBytes(result);
        return this;
    }

    public MessageBuilder withInt(int data) {
        var result = new byte[4];
        result[0] = (byte)(data & 0xFF);
        result[1] = (byte)((data >> 8) & 0xFF);
        result[2] = (byte)((data >> 16) & 0xFF);
        result[3] = (byte)((data >> 24) & 0xFF);

        appendBytes(result);
        return this;
    }

    public MessageBuilder withJson(Object object) {
        try {
            return withString(JSON.writeValueAsString(object));
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            return this;
        }
    }

    public Message build() {
        return new Message(type, data);
    }
}
