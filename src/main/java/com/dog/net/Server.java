package com.dog.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;

import com.dog.Utils;

public abstract class Server implements Runnable, ConnectionHandler {
    private final Map<Connection, Thread> conns = new HashMap<>();
    private final int port;
    private ServerSocket socket;
    private boolean isRunning = false;

    public Server(int port) {
        this.port = port;
    }

    public abstract boolean onConnect(Connection conn);
    public abstract void onDisconnect(Connection conn);

    @Override
    public void onClose(Connection conn) {
        synchronized(conns) {
            if (conns.containsKey(conn)) {
                conns.remove(conn);
                onDisconnect(conn);
            }
        }
    }

    @Override
    public void run() {
        if (isRunning())
            return;

        try {
            socket = new ServerSocket(port);
            isRunning = true;
        } catch (Throwable ex) {
            System.out.printf("Exception starting server: '%s'!\n", ex.toString());
            return;
        }

        while (isRunning()) {
            try {
                var conn = new Connection(this, socket.accept());
                synchronized (conns) {
                    if (!isRunning()) {
                        conn.disconnect();
                        return;
                    }

                    conns.put(conn, new Thread(conn));
                    conns.get(conn).start();
                }
            } catch (IOException ex) {
                System.out.printf("Exception while running server: '%s'!\n", ex.toString());
                continue;
            }
        }
    }

    public void stop() {
        isRunning = false;

        for (var conn : getConnections())
            conn.disconnect();

        Utils.close(socket);
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
        return isRunning;
    }

    public Connection[] getConnections() {
        synchronized (conns) {
            return conns.keySet().toArray(new Connection[conns.size()]);
        }
    }
}
