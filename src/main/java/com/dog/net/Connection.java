package com.dog.net;

import java.io.*;
import java.net.Socket;

public abstract class Connection implements AutoCloseable, Runnable {
    private Socket mSocket;
    private DataInputStream mInput;
    private DataOutputStream mOutput;

    public Connection(Socket socket) throws IOException {
        mSocket = socket;
        mInput  = new DataInputStream(mSocket.getInputStream());
        mOutput = new DataOutputStream(mSocket.getOutputStream());
    }

    public abstract void recv(String type, byte[] data) throws IOException;

    @Override
    public void close() {
        if (isConnected()) {
            System.out.printf("Connection from %s:%d closing.\n",
                mSocket.getInetAddress().toString(), mSocket.getPort());

            try {
                mInput.close();
                mOutput.close();
                mSocket.close();
            } catch (IOException ex) {}
        }
    }

    public void run() {
        while (isConnected()) {
            try {
                String type = mInput.readUTF();
                int length  = mInput.readInt(); // TODO: length verification/max
                byte[] data = null;
                if (length > 0)
                    data = mInput.readNBytes(length);

                recv(type, data);
            } catch (IOException ex) {
                System.out.printf("IOException when reading: %s\n", ex.toString());
                close();
            }
        }
    }

    public void send(String type, byte[] data) {
        if (!isConnected())
            return;

        try {
            mOutput.writeUTF(type);
            if (data != null && data.length != 0) {
                mOutput.writeInt(data.length);
                mOutput.write(data);
            } else {
                mOutput.writeInt(0);
            }
        } catch (IOException ex) {
            System.out.printf("IOException when sending: %s\n", ex.toString());
            close();
        }
    }

    public void send(Object type, byte[] data) {
        send(type.toString(), data);
    }

    public boolean isConnected() {
        return mSocket != null && !mSocket.isClosed();
    }
}
