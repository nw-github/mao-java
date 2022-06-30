package com.dog.net;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client implements AutoCloseable, Runnable {
    private Connection mConn;

    public Client(String host, int port, int maxAttempts, int timeout) throws UnknownHostException, IOException {
        int attempts = 0;
        while (true) {
            try {
                var socket = new Socket(host, port);
                mConn = new ClientConnection(socket);
                break;
            } catch (IOException ex) {
                if (attempts == maxAttempts)
                    throw ex;

                attempts++;
                Utils.sleep(timeout);
                continue;
            }
        }
    }

    @Override
    public void close() throws Exception {
        mConn.close();
    }

    public void run() {
        Thread thr = new Thread(mConn);
        thr.start();

        mConn.send(ClientMessage.LOG_IN, new byte[] {
            (byte)0xDE, (byte)0xAD, (byte)0xBE, (byte)0xEF });

        try {
            thr.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
