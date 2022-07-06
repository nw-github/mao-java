package com.dog.game.net;

import com.dog.game.Card;
import com.dog.net.*;

public class GameClient extends Client {
    private final ClientHandler mHandler;
    private final String mName;
    private ClientGame mGame;
    private boolean mGameStarted = false;

    public GameClient(String name, String host, int port, int maxAttempts, int timeout, ClientHandler handler) {
        super(host, port, maxAttempts, timeout);
        
        mHandler = handler;
        mHandler.create(this);
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
                mHandler.onAccept(mGame);
            } break;
            case GAME_START: {
                mGame = new ClientGame(message);
                mGameStarted = true;
                mHandler.onGameStart(mGame);
            } break;
            case GAME_END: {
                mGameStarted = false;
                mHandler.onGameEnd(mGame.getPlayer(message.readInt()));
            } break;
            case PLAYER_JOIN: {
                mHandler.onPlayerJoin(mGame.addPlayer(message.readString()));
            } break;
            case PLAYER_LEAVE: {
                var player = mGame.removePlayer(message.readInt());
                mGame.setDrawDeck(mGame.getDrawDeck() + player.getCards());
                mHandler.onPlayerLeave(player);
            } break;
            case PLAY: {
                var player  = mGame.getPlayer(message.readInt());
                var text    = message.readString();
                var card    = message.readString();

                mGame.removeCard(player, card);

                var played = Card.fromString(card);
                mHandler.onPlay(player, text, played);
                mGame.setTopCard(played);
            } break;
            case RECV_CARD: {
                var player  = mGame.getPlayer(message.readInt());
                var card    = message.readString();
                var reason  = message.readString();
                var newSize = message.readInt();

                mHandler.onCardReceived(player, mGame.addCard(player, card), reason, newSize);
                mGame.setDrawDeck(newSize);
            } break;
            }
        } catch (IllegalArgumentException | DeserializationException ex) {
            ex.printStackTrace(); // TODO
            
            stop();
        }
    }

    @Override
    public void onDisconnect() {
        mHandler.onDisconnect();
    }

    public ClientGame getGameState() {
        return mGame;
    }

    public void play(int index, String text) {
        if (!mGameStarted)
            return;

        send(new Message(ClientMessage.PLAY)
            .withInt(index)
            .withString(text));
    }

    public void draw() {
        if (!mGameStarted)
            return;

        send(new Message(ClientMessage.PLAY)
            .withInt(-1)
            .withString(""));
    }
}
