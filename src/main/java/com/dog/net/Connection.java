package com.dog.net;

import java.io.*;
import java.net.Socket;

import com.dog.Utils;

public class Connection implements Runnable {
    private final ConnectionHandler handler;
    private final Socket            socket;
    private final DataInputStream   input;
    private final DataOutputStream  output;

    public Connection(ConnectionHandler handler, Socket socket) throws IOException {
        this.handler = handler;
        this.socket  = socket;
        this.input   = new DataInputStream(socket.getInputStream());
        this.output  = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        while (isConnected()) {
            try {
                String type = input.readUTF();
                int length  = input.readInt();
                byte[] data = null;
                if (length > 0)
                    data = input.readNBytes(length);

                handler.onRecvMessage(this, new Message(type, data));
            } catch (IOException ex) {
                disconnect();
            }
        }
    }

    public void disconnect() {
        if (isConnected()) {
            Utils.close(input);
            Utils.close(output);
            Utils.close(socket);
            
            handler.onClose(this);
        }
    }

    public void send(Message message) {
        if (!isConnected())
            return;

        try {
            output.writeUTF(message.type());
            
            var data = message.data();
            if (data != null && data.length != 0) {
                output.writeInt(data.length);
                output.write(data);
            } else {
                output.writeInt(0);
            }
        } catch (IOException ex) {
            disconnect();
        }
    }

    public boolean isConnected() {
        return !socket.isClosed();
    }

    @Override
    public String toString() {
        return String.format("%s:%d",
            socket.getInetAddress().toString(),
            socket.getPort());
    }
}
