package com.dog.game.net;

import java.util.*;

import com.dog.game.Game;
import com.dog.game.GameException;
import com.dog.net.*;

public class GameServer extends Server {
    private final static int MAX_TEXT = 200;

    private final Map<Connection, Player> players = new LinkedHashMap<>();
    private final Game game;
    private final Object gameLock = new Object();
    private int nextId = 1;

    public GameServer(int port, int maxPlayers) {
        super(port);

        game = new Game(this, maxPlayers);
    }

    @Override
    public boolean onConnect(Connection conn) {
        synchronized (gameLock) {
            System.out.printf("New connection from %s: %s\n", conn.toString(),
                game.canAddPlayer() ? "Accepted!" : "Rejected!");

            return game.canAddPlayer();
        }
    }

    @Override
    public void onRecvMessage(Connection source, Message message) {
        try {
            synchronized (gameLock) {
                switch (ClientMessage.valueOf(message.type())) {
                case REGISTER: {
                    if (players.containsKey(source))
                        return;

                    var player = new Player(source, nextId(), filterUserText(message.readString()));
                    if (!game.addPlayer(player)) {
                        System.out.printf("Registration from %s rejected: cannot join.\n", source.toString());

                        source.disconnect();
                        return;
                    }
                    players.put(source, player);

                    send(source, ClientGame.fromGame(ServerMessage.ACCEPTED, game, player));
                    sendAll(new Message(ServerMessage.PLAYER_JOIN)
                        .withString(player.toString()));

                    if (players.size() == 1) // TODO: temporary
                        player.setIsDealer(true);
                    if (players.size() == game.getMaxPlayers())
                        game.start();
                } break;
                case PLAY: {
                    var index = message.readInt();
                    var text  = filterUserText(message.readString());
                    if (text.length() > MAX_TEXT)
                        text = text.substring(MAX_TEXT);

                    game.play(players.get(source), index, text);
                } break;
                case PUNISH: {
                    var player = players.get(source);
                    if (player == null || !game.isGameStarted() || !player.isDealer())
                        return;

                    game.punish(getPlayerById(message.readInt()), message.readString());
                } break;
                }
            }
        } catch (DeserializationException | IllegalArgumentException | GameException ex) {
            ex.printStackTrace(); // TODO 
        }
    }

    @Override
    public void onDisconnect(Connection conn) {
        System.out.printf("Connection from %s disconnected\n", conn.toString());

        synchronized (gameLock) {
            var player = players.get(conn);
            if (game.removePlayer(player)) {
                sendAllExcept(conn, new Message(ServerMessage.PLAYER_LEAVE)
                    .withInt(player.getId()));

                players.remove(conn);
            }
        }
    }
   
    private static String filterUserText(String name) {
        return name.replaceAll("[^a-zA-Z\\d]", "");
    }

    private Player getPlayerById(int id) {
        synchronized (gameLock) {
            for (var player : game.getPlayers())
                if (player.getId() == id)
                    return player;

            return null;
        }
    }

    private synchronized int nextId() {
        return nextId++;
    }
}
