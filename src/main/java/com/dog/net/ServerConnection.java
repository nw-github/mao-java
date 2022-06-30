package com.dog.net;

import java.io.*;
import java.net.Socket;

public class ServerConnection extends Connection {
    private Server mServer;

    public ServerConnection(Server server, Socket socket) throws IOException {
        super(socket);

        mServer = server;
    }

    @Override
    public void close()
    {
        super.close();
        // mServer.remove(this);
    }

    @Override
    public void recv(String type, byte[] data) throws IOException {
        System.out.printf("Received a %d byte message of type '%s': \n",
            data != null ? data.length : 0, type);
        for (byte b : data)
            System.out.printf("0x%02X, ", b);
        System.out.println();
    }
}
