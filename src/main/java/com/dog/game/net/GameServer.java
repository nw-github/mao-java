package com.dog.game.net;

import java.util.*;

import com.dog.game.Game;
import com.dog.game.GameException;
import com.dog.net.*;

public class GameServer extends Server {
    private final static int MAX_TEXT = 200;

    private final Map<Connection, Player> mPlayers = new LinkedHashMap<>();
    private final Game mGame;
    private final Object mGameLock = new Object();
    private int mNextId = 1;

    public GameServer(int port, int maxPlayers) {
        super(port);

        mGame = new Game(this, maxPlayers);
    }

    @Override
    public boolean onConnect(Connection conn) {
        synchronized (mGameLock) {
            System.out.printf("New connection from %s: %s\n", conn.toString(),
                mGame.canAddPlayer() ? "Accepted!" : "Rejected!");

            return mGame.canAddPlayer();
        }
    }

    @Override
    public void onRecvMessage(Connection source, Message message) {
        try {
            synchronized (mGameLock) {
                switch (ClientMessage.valueOf(message.type())) {
                case REGISTER: {
                    if (mPlayers.containsKey(source))
                        return;

                    var player = new Player(source, nextId(), filterUserText(message.readString()));
                    if (!mGame.addPlayer(player)) {
                        System.out.printf("Registration from %s rejected: cannot join.\n", source.toString());

                        source.disconnect();
                        return;
                    }
                    mPlayers.put(source, player);

                    send(source, ClientGame.fromGame(ServerMessage.ACCEPTED, null, mPlayers.values(), 0, player));
                    sendAll(new Message(ServerMessage.PLAYER_JOIN)
                        .withString(player.toString()));

                    if (mPlayers.size() == 1) // TODO: temporary
                        player.setDealer(true);
                    if (mPlayers.size() == mGame.getMaxPlayers())
                        mGame.start();
                } break;
                case PLAY: {
                    var index = message.readInt();
                    var text  = filterUserText(message.readString());
                    if (text.length() > MAX_TEXT)
                        text = text.substring(MAX_TEXT);

                    mGame.play(mPlayers.get(source), index, text);
                } break;
                case PUNISH: {
                    var player = mPlayers.get(source);
                    if (player == null || !mGame.isGameStarted() || !player.isDealer())
                        return;

                    mGame.punish(getPlayerById(message.readInt()), message.readString());
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

        synchronized (mGameLock) {
            var player = mPlayers.get(conn);
            if (mGame.removePlayer(player)) {
                sendAllExcept(conn, new Message(ServerMessage.PLAYER_LEAVE)
                    .withInt(player.getId()));

                mPlayers.remove(conn);
            }
        }
    }
   
    private static String filterUserText(String name) {
        return name.replaceAll("[^a-zA-Z\\d]", "");
    }

    private Player getPlayerById(int id) {
        synchronized (mGameLock) {
            for (var player : mGame.getPlayers())
                if (player.getId() == id)
                    return player;

            return null;
        }
    }

    private synchronized int nextId() {
        return mNextId++;
    }
}
