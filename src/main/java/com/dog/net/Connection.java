package com.dog.net;

import java.io.*;
import java.net.Socket;

import com.dog.Utils;

public class Connection implements Runnable {
    private Socket mSocket;
    private DataInputStream mInput;
    private DataOutputStream mOutput;
    private ConnectionHandler mHandler;

    public Connection(ConnectionHandler handler, Socket socket) throws IOException {
        mHandler = handler;
        mSocket  = socket;
        mInput   = new DataInputStream(mSocket.getInputStream());
        mOutput  = new DataOutputStream(mSocket.getOutputStream());
    }

    @Override
    public void run() {
        while (isConnected()) {
            try {
                String type = mInput.readUTF();
                int length  = mInput.readInt();
                byte[] data = null;
                if (length > 0)
                    data = mInput.readNBytes(length);

                mHandler.onRecvMessage(this, new Message(type, data));
            } catch (IOException ex) {
                System.out.printf("IOException when reading: %s\n", ex.toString());
                disconnect();
            }
        }
    }

    public void disconnect() {
        if (isConnected()) {
            mHandler.onClose(this);

            Utils.close(mInput);
            Utils.close(mOutput);
            Utils.close(mSocket);
        }
    }

    // TODO: better message system
    public void send(Message message) {
        if (!isConnected())
            return;

        try {
            mOutput.writeUTF(message.type());
            
            var data = message.data();
            if (data != null && data.length != 0) {
                mOutput.writeInt(data.length);
                mOutput.write(data);
            } else {
                mOutput.writeInt(0);
            }
        } catch (IOException ex) {
            System.out.printf("IOException when sending: %s\n", ex.toString());
            disconnect();
        }
    }

    public boolean isConnected() {
        return mSocket != null && !mSocket.isClosed();
    }

    public Socket getSocket() {
        return mSocket;
    }
}
