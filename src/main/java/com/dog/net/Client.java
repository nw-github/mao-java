package com.dog.net;

import java.io.IOException;
import java.net.Socket;

import com.dog.Utils;

public abstract class Client implements Runnable, ConnectionHandler {
    private final String host;
    private final int    port;
    private final int    maxAttempts;
    private final int    timeout;
    private Connection   conn;

    public Client(String host, int port, int maxAttempts, int timeout) {
        this.host        = host;
        this.port        = port;
        this.maxAttempts = maxAttempts;
        this.timeout     = timeout;
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

        for (int i = 0; i < maxAttempts; i++) {
            try {
                conn = new Connection(this, new Socket(host, port));
                onConnect();
                conn.run();
                return;
            } catch (IOException ex) {
                Utils.sleep(timeout);
                continue;
            }
        }
    }

    public void stop() {
        conn.disconnect();
        conn = null;
    }

    public void send(Message message) {
        conn.send(message);
    }

    public boolean isRunning() {
        return conn != null;
    }
}
