package com.dog.game.net;

import java.util.*;

import com.dog.game.Game;
import com.dog.net.*;

public class GameServer extends Server {
    private final int mMaxPlayers;

    private Map<Connection, Player> mPlayers = new LinkedHashMap<>();
    private Game mGame;
    private int mNextId = 1;

    public GameServer(int port, int maxPlayers) {
        super(port);

        if (maxPlayers > Game.MAX_PLAYERS)
            maxPlayers = Game.MAX_PLAYERS;

        mMaxPlayers = maxPlayers;
    }

    @Override
    public boolean onConnect(Connection conn) {
        System.out.printf("New connection from %s: %s\n", conn.toString(),
            canPlayerJoin() ? "Accepted!" : "Rejected!");

        return canPlayerJoin();
    }

    @Override
    public synchronized void onRecvMessage(Connection source, Message message) {
        try {
            synchronized (mPlayers) {
                switch (ClientMessage.valueOf(message.type())) {
                case REGISTER: {
                    if (mPlayers.containsKey(source))
                        return;

                    if (!canPlayerJoin()) {
                        System.out.printf("Registration from %s rejected: cannot join.\n", source.toString());

                        source.disconnect();
                        return;
                    }                    

                    var player = new Player(source, nextId(), filterPlayerName(message.readString()));
                    send(source, ClientGame.fromGame(ServerMessage.ACCEPTED, null, mPlayers.values(), player));
                    sendAll(new Message(ServerMessage.PLAYER_JOIN)
                        .withString(player.toString()));

                    mPlayers.put(source, player);
                    if (mPlayers.size() == 1) // TODO: temporary
                        player.setDealer(true);
                    if (mPlayers.size() == mMaxPlayers)
                        startGame();
                } break;
                case PLAY: {
                    var player = mPlayers.get(source);
                    if (player == null || !isGameStarted())
                        return;

                    synchronized (mGame) {
                        mGame.play(player, message.readInt());
                    }
                } break;
                case PUNISH: {
                    var player = mPlayers.get(source);
                    if (player == null || !isGameStarted() || !player.isDealer())
                        return;

                    var target = getPlayerById(message.readInt());
                    if (target == null)
                        return;

                    synchronized (mGame) {
                        mGame.punish(player, message.readString());
                    }
                } break;
                }
            }
        } catch (DeserializationException ex) {
            ex.printStackTrace(); // TODO: handle malformed message
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace(); // TODO: handle malformed message
        }
    }

    @Override
    public void onDisconnect(Connection conn) {
        System.out.printf("Connection from %s disconnected\n", conn.toString());

        // TODO: stop/readjust the game
        synchronized (mPlayers) {
            var player = mPlayers.get(conn);
            if (player != null) {
                sendAllExcept(conn, new Message(ServerMessage.PLAYER_LEAVE)
                    .withInt(player.getId()));
                mPlayers.remove(conn);
            }
        }
    }
    
    public boolean startGame() {
        if (isGameStarted())
            return false;

        synchronized (mPlayers) {
            if (mPlayers.size() < Game.MIN_PLAYERS)
                return false;

            synchronized (mGame = new Game(this, mPlayers.values().toArray(new Player[mPlayers.size()]))) {
                for (var player : mGame.getPlayers())
                    send(player.getConnection(), ClientGame.fromGame(ServerMessage.GAME_START, mGame, player));
                
                return true;
            }
        }
    }

    public boolean isGameStarted() {
        return mGame != null;
    }
   
    public static String filterPlayerName(String name) {
        return name.replaceAll("[^a-zA-Z\\d]", "");
    }

    private boolean canPlayerJoin() {
        synchronized (mPlayers) {
            return mPlayers.size() < mMaxPlayers && !isGameStarted();
        }
    }

    private Player getPlayerById(int id) {
        synchronized (mPlayers) {
            for (var player : mPlayers.values())
                if (player.getId() == id)
                    return player;

            return null;
        }
    }

    private synchronized int nextId() {
        return mNextId++;
    }
}
