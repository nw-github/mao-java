package com.dog.game.net;

import java.util.*;

import com.dog.game.Card;
import com.dog.game.Game;
import com.dog.game.GameException;
import com.dog.net.*;
import com.fasterxml.jackson.core.JsonProcessingException;

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
            var reader = new MessageReader(message);

            synchronized (gameLock) {
                switch (ClientMessage.valueOf(message.type())) {
                case REGISTER: {
                    if (players.containsKey(source))
                        return;

                    var player = new Player(source, nextId(), filterUserText(reader.readString()));
                    if (!game.addPlayer(player)) {
                        System.out.printf("Registration from %s rejected: cannot join.\n", source.toString());

                        source.disconnect();
                        return;
                    }
                    players.put(source, player);

                    send(source, new MessageBuilder(ServerMessage.ACCEPTED)
                        .withJson(new ClientGame(game, player))
                        .build());
                    sendAll(new MessageBuilder(ServerMessage.PLAYER_JOIN)
                        .withJson(new ClientPlayer(player))
                        .build());

                    if (players.size() == 1) // TODO: temporary
                        player.setIsDealer(true);
                    if (players.size() == game.getMaxPlayers())
                        game.start();
                } break;
                case PLAY: {
                    var card = reader.readJson(Card.class);
                    var text = filterUserText(reader.readString());
                    if (text.length() > MAX_TEXT)
                        text = text.substring(MAX_TEXT);

                    game.play(players.get(source), card, text);
                } break;
                case PUNISH: {
                    var player = players.get(source);
                    if (player == null || !game.isGameStarted() || !player.isDealer())
                        return;

                    game.punish(getPlayerById(reader.readInt()), reader.readString());
                } break;
                }
            }
        } catch (IllegalArgumentException | GameException ex) {
            ex.printStackTrace(); // TODO 
        } catch (DeserializationException | JsonProcessingException ex) {
            ex.printStackTrace(); // TODO 
        }
    }

    @Override
    public void onDisconnect(Connection conn) {
        System.out.printf("Connection from %s disconnected\n", conn.toString());

        synchronized (gameLock) {
            var player = players.get(conn);
            if (game.removePlayer(player)) {
                sendAllExcept(conn, new MessageBuilder(ServerMessage.PLAYER_LEAVE)
                    .withInt(player.getId())
                    .build());

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
