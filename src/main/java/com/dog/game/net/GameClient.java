package com.dog.game.net;

import com.dog.net.*;

public class GameClient extends Client {
    private final String mName;
    private ClientGame mGame;

    public GameClient(String name, String host, int port, int maxAttempts, int timeout) {
        super(host, port, maxAttempts, timeout);
        
        mName = name;
    }

    @Override
    public void onConnect() {
        send(new Message(ClientMessage.REGISTER).withString(mName));
    }

    @Override
    public synchronized void onRecvMessage(Connection source, Message message) {
        try {
            switch (ServerMessage.valueOf(message.type())) {
            case ACCEPTED: {
                mGame = new ClientGame(message);

                if (mGame.getPlayers().size() != 0) {
                    System.out.printf("[%d] Current players: \n", mGame.getId());
                    for (var item : mGame.getPlayers().entrySet())
                        System.out.printf("\t [%d] %s\n", item.getKey(), item.getValue().getName());
                }
            } break;
            case GAME_START: {
                mGame = new ClientGame(message);

                System.out.printf("First card: %s of %s\n", mGame.getTopCard().face().toString(), mGame.getTopCard().suit().toString());
                for (var item : mGame.getPlayers().entrySet())
                    System.out.printf("\t[%d] %s : %d cards\n", item.getKey(), item.getValue().getName(), item.getValue().getCards());
                System.out.printf("[%d] My cards: \n", mGame.getId());

                for (var card : mGame.getCards())
                    System.out.printf("\t%s of %s\n", card.face().toString(), card.suit().toString());
            } break;
            case PLAYER_JOIN: {
                var string = message.readString();
                var player = mGame.addPlayer(string);
                if (player == null) {
                    System.out.printf("Failed parsing player from source string '%s'\n", string);
                    return;
                }

                System.out.printf("[%d] %s joined the game.\n", player.getId(), player.getName());
            } break;
            case PLAYER_LEAVE: {
                var player = mGame.removePlayer(message.readInt());
                if (player == null)
                    return;

                System.out.printf("[%d] %s left the game.\n", player.getId(), player.getName());
            } break;
            }
        } catch (IllegalArgumentException | DeserializationException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onDisconnect() {

    }
}
