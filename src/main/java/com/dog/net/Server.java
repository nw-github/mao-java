package com.dog.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.*;

public class Server implements AutoCloseable, Runnable {
    private final int mMaxPlayers;
    private ServerSocket mSocket;
    private List<Connection> mPending = Collections.synchronizedList(new ArrayList<Connection>());
    private List<Connection> mPlayers = Collections.synchronizedList(new ArrayList<Connection>());

    public Server(int port, int maxPlayers) throws UnknownHostException, IOException {
        mSocket     = new ServerSocket(port);
        mMaxPlayers = maxPlayers;
    }

    @Override
    public void close() throws Exception {
        synchronized (mPlayers)
        {
            for (var conn : mPending)
                conn.close();
            for (var conn : mPlayers)
                conn.close();
            
            mPending.clear();
            mPlayers.clear();
            mSocket.close();
        }
    }

    public void run() {
        while (!mSocket.isClosed()) {
            try {
                var socket = mSocket.accept();
                var conn   = new ServerConnection(this, socket);
                new Thread(conn).start(); // TODO: add to list and join() in close()

                System.out.printf("Received connection from %s:%d!\n",
                    socket.getInetAddress().toString(), socket.getPort());
                mPending.add(conn);
            } catch (IOException ex) {
                System.out.printf("Exception while accepting client: '%s'!\n", ex.toString());
                continue;
            }
        }
    }
}
