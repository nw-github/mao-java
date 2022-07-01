package com.dog;

import com.dog.game.net.ClientMessage;
import com.dog.net.*;

public class Main {
    public static void runServer(int port) {
        System.out.println("Starting server...");

        var server = new Server(port) {
            @Override
            public boolean onConnect(Connection conn) {
                System.out.printf("Connection from %s:%d connected.\n",
                    conn.getSocket().getInetAddress().toString(),
                    conn.getSocket().getPort());

                return getConnections().size() < 2;
            }

            @Override
            public void onRecvMessage(Connection source, Message message) {
                var data = message.data();

                System.out.printf("[%s:%d] Received a %d byte message of type '%s': \n",
                    source.getSocket().getInetAddress().toString(),
                    source.getSocket().getPort(),
                    data != null ? data.length : 0,
                    message.type());
                    
                for (byte b : data)
                    System.out.printf("0x%02X, ", b);
                System.out.println();

                try {
                    String str = message.readString();
                    int i = message.readInt();

                    System.out.printf("[%s:%d] String: '%s' Int: '%d' \n",
                        source.getSocket().getInetAddress().toString(),
                        source.getSocket().getPort(),
                        str,
                        i);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onDisconnect(Connection source) {
                System.out.printf("Connection from %s:%d disconnected.\n",
                    source.getSocket().getInetAddress().toString(),
                    source.getSocket().getPort());
            }
        };

        server.run();
    }

    public static void runClient(String host, int port) {
        System.out.println("Starting client...");

        var client = new Client(host, port, 5, 2000) {
            @Override
            public void onConnect() {
                System.out.println("Connected to server!");

                send(new Message(ClientMessage.LOG_IN)
                    .writeString("Test String")
                    .writeInt(100));
            }

            @Override
            public void onRecvMessage(Connection source, Message message) {
                var data = message.data();

                System.out.printf("Received a %d byte message of type '%s': \n",
                    data != null ? data.length : 0, message.type());

                for (byte b : data)
                    System.out.printf("0x%02X, ", b);
                System.out.println();
            }

            @Override
            public void onDisconnect() {
                System.out.println("Disconnected from server!");
            }
        };

        client.run();
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            runServer(5000);
        } else {
            runClient("127.0.0.1", 5000);
        }
    }
}
