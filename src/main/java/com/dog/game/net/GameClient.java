package com.dog.game.net;

import com.dog.game.Card;
import com.dog.net.*;
import com.fasterxml.jackson.core.JsonProcessingException;

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
        send(new MessageBuilder(ClientMessage.REGISTER).withString(name).build());
    }

    @Override
    public synchronized void onRecvMessage(Connection source, Message message) {
        try {
            var reader = new MessageReader(message);

            switch (ServerMessage.valueOf(message.type())) {
            case ACCEPTED: {
                handler.onAccept(game = reader.readJson(ClientGame.class));
            } break;
            case GAME_START: {
                gameStarted = true;
                handler.onGameStart(game = reader.readJson(ClientGame.class));
            } break;
            case GAME_END: {
                gameStarted = false;
                handler.onGameEnd(game.getPlayer(reader.readInt()));
            } break;
            case PLAYER_JOIN: {
                handler.onPlayerJoin(game.addPlayer(reader.readJson(ClientPlayer.class)));
            } break;
            case PLAYER_LEAVE: {
                var player = game.removePlayer(reader.readInt());
                game.setDrawDeck(game.getDrawDeck() + player.getCards());
                handler.onPlayerLeave(player);
            } break;
            case PLAY: {
                var player = game.getPlayer(reader.readInt());
                var text   = reader.readString();
                var card   = reader.readJson(Card.class);
                game.removeCard(player, card);

                handler.onPlay(player, text, card);
                game.setDiscardTop(card);
            } break;
            case RECV_CARD: {
                var player  = game.getPlayer(reader.readInt());
                var card    = reader.readJson(Card.class);
                var reason  = reader.readString();
                var newSize = reader.readInt();

                game.addCard(player, card);
                handler.onCardReceived(player, card, reason, newSize);
                game.setDrawDeck(newSize);
            } break;
            }
        } catch (IllegalArgumentException | DeserializationException | JsonProcessingException ex) {
            ex.printStackTrace(); // TODO
            
            stop();
        }
    }

    @Override
    public void onDisconnect() {
        handler.onDisconnect();
    }

    public ClientGame getGame() {
        return game;
    }

    public void play(Card card, String text) {
        if (!gameStarted)
            return;

        send(new MessageBuilder(ClientMessage.PLAY)
            .withJson(card)
            .withString(text)
            .build());
    }

    public void draw() {
        if (!gameStarted)
            return;

        send(new MessageBuilder(ClientMessage.PLAY)
            .withInt(-1)
            .withString("")
            .build());
    }
}
