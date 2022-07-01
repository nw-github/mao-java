package com.dog.net;

public interface ConnectionHandler {
    public void onRecvMessage(Connection source, Message message);
    public void onClose(Connection source);
}
