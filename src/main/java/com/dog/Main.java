package com.dog;

import java.util.concurrent.ThreadLocalRandom;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.dog.game.Card;
import com.dog.game.net.*;
import com.dog.ui.Application;

public class Main {
    public static void runServer(int port) {
        System.out.printf("Starting server on port %d...\n", port);

        var server = new GameServer(5000, 2);
        server.run();
    }

    public static void runClient(String host, int port) {
        final var name = "Player" + ThreadLocalRandom.current().nextInt(1000, 9999);
        System.out.printf("Connecting to %s:%d with name '%s'...\n", host, port, name);

        var client = new GameClient(name, host, port, 5, 2000, new ClientHandler() {
            GameClient gameClient;

            @Override
            public void create(GameClient client) {
                this.gameClient = client;
            }

            @Override
            public void onAccept(ClientGame game) {
                if (game.getPlayers().size() != 0) {
                    System.out.printf("Current players: \n\t");
                    for (var item : game.getPlayers().entrySet())
                        System.out.printf("'%s:%d', ", item.getValue().getName(), item.getKey());
                    System.out.printf("\n");
                }
            }

            @Override
            public void onPlayerJoin(ClientPlayer player) {
                System.out.printf("[Game] %s:%d joined the game.\n", player.getName(), player.getId());
            }

            @Override
            public void onPlayerLeave(ClientPlayer player) {
                System.out.printf("[Game] %s:%d left the game.\n", player.getName(), player.getId());
            }

            @Override
            public void onGameStart(ClientGame game) {
                System.out.printf("\n[Game Start] (%s) (%d cards drawable)\n\t",
                    game.getDiscardTop().toString(),
                    game.getDrawDeck());
                
                for (var item : game.getPlayers().entrySet())
                    System.out.printf("%s:%d: %d cards, ", item.getValue().getName(), item.getKey(), item.getValue().getCards());
                System.out.printf("\n\n");
                
                printDeck();

                if (game.getLocalId() == 1)
                    gameClient.play(game.getCards().get(0), "");
            }

            @Override
            public void onGameEnd(ClientPlayer winner) {
                System.out.printf("Game over: %s wins!\n", winner.getName());

                gameClient.stop();
            }

            @Override
            public void onPlay(ClientPlayer player, String text, Card played) {
                if (!gameClient.getGame().isLocalPlayer(player)) {
                    System.out.printf("%s played '%s' onto '%s' (now has %d cards)\n",
                        player.getName(),
                        played.toString(),
                        gameClient.getGame().getDiscardTop().toString(),
                        player.getCards());
                    if (!text.isEmpty())
                        System.out.printf("\t%s\n", text);

                    gameClient.play(gameClient.getGame().getCards().get(0), "");
                } else {
                    System.out.printf("Played '%s' onto '%s' (now has %d cards)\n",
                        played.toString(),
                        gameClient.getGame().getDiscardTop().toString(),
                        player.getCards());
                    if (!text.isEmpty())
                        System.out.printf("\t%s\n", text);
                }
            }

            @Override
            public void onCardReceived(ClientPlayer player, Card card, String reason, int newSize) {
                if (card != null) {
                    System.out.printf("\t> Received '%s' (now has %d cards)\n", card.toString(), player.getCards());
                } else {
                    System.out.printf("\t- Received a card (now has %d cards)\n", player.getCards());
                }
                
                if (!reason.isEmpty())
                    System.out.printf("\t\tReason: %s\n", reason);

                System.out.printf("\t\tDeck now has %d cards%s\n", 
                    newSize,
                    newSize >= gameClient.getGame().getDrawDeck() ? " (was reshuffled)" : "");
            }

            @Override
            public void onDisconnect() {
                System.out.printf("Connection to server closed.\n");
            }

            private void printDeck() {
                System.out.printf("My cards: \n\t");
                for (var card : gameClient.getGame().getCards())
                    System.out.printf("%s, ", card.toString());
                System.out.printf("\n\n");
            }
        });

        client.run();
    }

    public static void main(String[] args) {
        // if (args.length > 0) {
        //     runServer(5000);
        // } else {
        //     runClient("127.0.0.1", 5000);
        // }

        var config = new Lwjgl3ApplicationConfiguration();
        config.setForegroundFPS(0);
        config.useVsync(true);
        config.setTitle("Mao");
        config.setWindowedMode(800, 480);

        new Lwjgl3Application(new Application(), config);
    }
}
