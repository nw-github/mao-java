package com.dog.net;

import java.util.Arrays;

public class Message {
    private String type;
    private byte[] data;
    private int dataPointer = 0;

    public Message(String type, byte[] data)
    {
        this.type = type;
        this.data = data;
    }

    public Message(Object type, byte[] data)
    {
        this(type.toString(), data);
    }

    public Message(String type)
    {
        this(type, null);
    }

    public Message(Object type)
    {
        this(type, null);
    }

    public String type() { return type; }
    
    public byte[] data() { return data; }

    // --------------------------

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

    public Message withString(String data) {
        withInt(data.length());

        var result = new byte[data.length() * 2];
        for (int i = 0; i < result.length; i += 2) {
            result[i]     = (byte)(data.charAt(i / 2) & 0xFF);
            result[i + 1] = (byte)(data.charAt(i / 2) >> 8 & 0xFF);
        }
        
        appendBytes(result);
        return this;
    }

    public Message withInt(int data) {
        var result = new byte[4];
        result[0] = (byte)(data & 0xFF);
        result[1] = (byte)((data >> 8) & 0xFF);
        result[2] = (byte)((data >> 16) & 0xFF);
        result[3] = (byte)((data >> 24) & 0xFF);

        appendBytes(result);
        return this;
    }

    // --------------------------

    public void seek(int pos) {
        dataPointer = pos;
    }

    public byte nextByte() {
        return data[dataPointer++];
    }

    public String readString() throws DeserializationException {
        int length = readInt();
        if (dataPointer + length * 2 > data.length)
            throw new DeserializationException("Couldn't deserialize string.");

        var result = new char[length];
        for (int i = 0; i < result.length; i++) {
            result[i] |= nextByte() & 0xFF;
            result[i] |= (nextByte() << 8) & 0xFF;
        }
        return new String(result);
    }

    public int readInt() throws DeserializationException {
        if (dataPointer + 4 > data.length)
            throw new DeserializationException("Couldn't deserialize int.");

        int result = 0;
        result |= nextByte() & 0xFF;
        result |= (nextByte() << 8) & 0xFF00;
        result |= (nextByte() << 16) & 0xFF0000;
        result |= (nextByte() << 24) & 0xFF000000;
        return result;
    }
}
