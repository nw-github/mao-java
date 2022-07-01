package com.dog.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;

import com.dog.Utils;

public abstract class Server implements Runnable, ConnectionHandler {
    private ServerSocket mSocket;
    private List<Connection> mConns = new ArrayList<>();
    private final int mPort;

    public Server(int port) {
        mPort = port;
    }

    public abstract boolean onConnect(Connection conn);
    public abstract void onDisconnect(Connection conn);

    @Override
    public void onClose(Connection conn) {
        if (mConns.indexOf(conn) != -1)
        {
            onDisconnect(conn);
            mConns.remove(conn);
        }
    }

    @Override
    public void run() {
        if (isRunning())
            return;

        try {
            mSocket = new ServerSocket(mPort);
        } catch (Throwable ex) { // UnknownHostException, IOException
            return;
        }

        while (!mSocket.isClosed()) {
            try {
                var socket = mSocket.accept();
                var conn   = new Connection(this, socket);
                if (onConnect(conn)) {
                    new Thread(conn).start(); // TODO: add to list and join() in close()

                    System.out.printf("Received connection from %s:%d!\n",
                        socket.getInetAddress().toString(), socket.getPort());
                    mConns.add(conn);
                } else {
                    System.out.printf("Rejected connection from %s:%d!\n",
                        socket.getInetAddress().toString(), socket.getPort());

                    conn.disconnect();
                }
            } catch (IOException ex) {
                System.out.printf("Exception while accepting client: '%s'!\n", ex.toString());
                continue;
            }
        }
    }

    public void stop() {
        for (var conn : mConns)
            conn.disconnect();
        
        mConns.clear();
        Utils.close(mSocket);
        mSocket = null;
    }

    public void send(Connection target, Message message) {
        target.send(message);
    }

    public void sendAll(Message message) {
        for (var conn : mConns)
            conn.send(message);
    }

    public void sendAllBut(Connection exclude, Message message) {
        for (var conn : mConns)
            if (conn != exclude)
                conn.send(message);
    }

    public boolean isRunning() {
        return mSocket != null;
    }

    protected List<Connection> getConnections() {
        return mConns;
    }
}
