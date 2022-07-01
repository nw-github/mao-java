package com.dog.net;

import java.io.IOException;
import java.net.Socket;

import com.dog.Utils;

public abstract class Client implements Runnable, ConnectionHandler {
    private final String mHost;
    private final int mPort;
    private final int mMaxAttepts;
    private final int mTimeout;

    private Connection mConn;

    public Client(String host, int port, int maxAttempts, int timeout) {
        mHost       = host;
        mPort       = port;
        mMaxAttepts = maxAttempts;
        mTimeout    = timeout;
    }

    public abstract void onConnect();
    public abstract void onDisconnect();

    @Override
    public void onClose(Connection conn) {
        onDisconnect();
    }

    @Override
    public void run() {
        if (isRunning())
            return;

        for (int i = 0; i < mMaxAttepts; i++) {
            try {
                var socket = new Socket(mHost, mPort);
                mConn = new Connection(this, socket);
                onConnect();
                mConn.run();
                return;
            } catch (IOException ex) {
                Utils.sleep(mTimeout);
                continue;
            }
        }
    }

    public void stop() {
        mConn.stop();
        mConn = null;
    }

    public void send(Message message) {
        mConn.send(message);
    }

    public boolean isRunning() {
        return mConn != null;
    }
}
