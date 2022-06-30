package com.dog.net;

import java.io.*;
import java.net.Socket;

public class ClientConnection extends Connection {
    public ClientConnection(Socket socket) throws IOException {
        super(socket);
    }

    @Override
    public void recv(String type, byte[] data) throws IOException {
        System.out.printf("Received a message of type '%s' and length %d.\n",
            type, data != null ? data.length : 0);
    }
}
