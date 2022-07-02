package com.dog;

import java.util.concurrent.ThreadLocalRandom;

import com.dog.game.net.GameClient;
import com.dog.game.net.GameServer;

public class Main {
    public static void runServer(int port) {
        System.out.printf("Starting server on port %d...\n", port);

        var server = new GameServer(5000, 2);
        server.run();
    }

    public static void runClient(String host, int port) {
        final var name = "Player" + ThreadLocalRandom.current().nextInt(1000, 9999);
        System.out.printf("Connecting to %s:%d with name '%s'...\n", host, port, name);

        var client = new GameClient(name, host, port, 5, 2000);
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
