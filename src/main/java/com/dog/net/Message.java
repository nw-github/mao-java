package com.dog.net;

import java.util.Arrays;

public class Message {
    private String mType;
    private byte[] mData;
    private int mDataPointer = 0;

    public Message(String type, byte[] data)
    {
        mType = type;
        mData = data;
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

    public String type() { return mType; }
    
    public byte[] data() { return mData; }

    // --------------------------

    private void appendBytes(byte[] data) {
        if (mData != null && mData.length > 0) {
            var result = Arrays.copyOf(mData, mData.length + data.length);
            for (int i = 0; i < data.length; i++)
                result[i + mData.length] = data[i];
            mData = result;
        } else {
            mData = data;
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
        mDataPointer = pos;
    }

    public byte nextByte() {
        return mData[mDataPointer++];
    }

    public String readString() throws DeserializationException {
        int length = readInt();
        if (mDataPointer + length * 2 > mData.length)
            throw new DeserializationException("Couldn't deserialize string.");

        var result = new char[length];
        for (int i = 0; i < result.length; i++) {
            result[i] |= nextByte() & 0xFF;
            result[i] |= (nextByte() << 8) & 0xFF;
        }
        return new String(result);
    }

    public int readInt() throws DeserializationException {
        if (mDataPointer + 4 > mData.length)
            throw new DeserializationException("Couldn't deserialize int.");

        int result = 0;
        result |= nextByte() & 0xFF;
        result |= (nextByte() << 8) & 0xFF00;
        result |= (nextByte() << 16) & 0xFF0000;
        result |= (nextByte() << 24) & 0xFF000000;
        return result;
    }
}
