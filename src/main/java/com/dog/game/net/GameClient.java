package com.dog.game.net;

import com.dog.game.Card;
import com.dog.net.*;

public class GameClient extends Client {
    private final ClientHandler handler;
    private final String name;
    private ClientGame game;
    private boolean gameStarted = false;

    public GameClient(String name, String host, int port, int maxAttempts, int timeout, ClientHandler handler) {
        super(host, port, maxAttempts, timeout);
        
        this.name    = name;
        this.handler = handler;
        this.handler.create(this);
    }

    @Override
    public void onConnect() {
        send(new Message(ClientMessage.REGISTER).withString(name));
    }

    @Override
    public synchronized void onRecvMessage(Connection source, Message message) {
        try {
            switch (ServerMessage.valueOf(message.type())) {
            case ACCEPTED: {
                game = new ClientGame(message);
                handler.onAccept(game);
            } break;
            case GAME_START: {
                game = new ClientGame(message);
                gameStarted = true;
                handler.onGameStart(game);
            } break;
            case GAME_END: {
                gameStarted = false;
                handler.onGameEnd(game.getPlayer(message.readInt()));
            } break;
            case PLAYER_JOIN: {
                handler.onPlayerJoin(game.addPlayer(message.readString()));
            } break;
            case PLAYER_LEAVE: {
                var player = game.removePlayer(message.readInt());
                game.setDrawDeck(game.getDrawDeck() + player.getCards());
                handler.onPlayerLeave(player);
            } break;
            case PLAY: {
                var player  = game.getPlayer(message.readInt());
                var text    = message.readString();
                var card    = message.readString();

                game.removeCard(player, card);

                var played = Card.fromNetString(card);
                handler.onPlay(player, text, played);
                game.setTopCard(played);
            } break;
            case RECV_CARD: {
                var player  = game.getPlayer(message.readInt());
                var card    = message.readString();
                var reason  = message.readString();
                var newSize = message.readInt();

                handler.onCardReceived(player, game.addCard(player, card), reason, newSize);
                game.setDrawDeck(newSize);
            } break;
            }
        } catch (IllegalArgumentException | DeserializationException ex) {
            ex.printStackTrace(); // TODO
            
            stop();
        }
    }

    @Override
    public void onDisconnect() {
        handler.onDisconnect();
    }

    public ClientGame getGameState() {
        return game;
    }

    public void play(int index, String text) {
        if (!gameStarted)
            return;

        send(new Message(ClientMessage.PLAY)
            .withInt(index)
            .withString(text));
    }

    public void draw() {
        if (!gameStarted)
            return;

        send(new Message(ClientMessage.PLAY)
            .withInt(-1)
            .withString(""));
    }
}
