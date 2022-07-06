package com.dog.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;

import com.dog.Utils;

public abstract class Server implements Runnable, ConnectionHandler {
    private final int mPort;
    private ServerSocket mSocket;
    private Map<Connection, Thread> mConns = new HashMap<>();
    private boolean mIsRunning = false;

    public Server(int port) {
        mPort = port;
    }

    public abstract boolean onConnect(Connection conn);
    public abstract void onDisconnect(Connection conn);

    @Override
    public void onClose(Connection conn) {
        synchronized(mConns) {
            if (mConns.containsKey(conn)) {
                mConns.remove(conn);
                onDisconnect(conn);
            }
        }
    }

    @Override
    public void run() {
        if (isRunning())
            return;

        try {
            mSocket = new ServerSocket(mPort);
            mIsRunning = true;
        } catch (Throwable ex) {
            System.out.printf("Exception starting server: '%s'!\n", ex.toString());
            return;
        }

        while (isRunning()) {
            try {
                var conn = new Connection(this, mSocket.accept());
                synchronized (mConns) {
                    if (!isRunning()) {
                        conn.disconnect();
                        return;
                    }

                    mConns.put(conn, new Thread(conn));
                    mConns.get(conn).start();
                }
            } catch (IOException ex) {
                System.out.printf("Exception while running server: '%s'!\n", ex.toString());
                continue;
            }
        }
    }

    public void stop() {
        mIsRunning = false;

        for (var conn : getConnections())
            conn.disconnect();

        Utils.close(mSocket);
    }

    public void send(Connection target, Message message) {
        target.send(message);
    }

    public void sendAll(Message message) {
        sendAllExcept(null, message);
    }

    public void sendAllExcept(Connection exclude, Message message) {
        for (var conn : getConnections())
            if (conn != exclude)
                conn.send(message);
    }

    public boolean isRunning() {
        return mIsRunning;
    }

    public Connection[] getConnections() {
        synchronized (mConns) {
            return mConns.keySet().toArray(new Connection[mConns.size()]);
        }
    }
}
