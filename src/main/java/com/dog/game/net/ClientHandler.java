package com.dog.game.net;

import com.dog.game.Card;

public interface ClientHandler {
    public void create(GameClient client);

    public void onAccept(ClientGame game);
    public void onPlayerJoin(ClientPlayer player);
    public void onPlayerLeave(ClientPlayer player);
    public void onGameStart(ClientGame game);
    public void onGameEnd(ClientPlayer winner);

    public void onPlay(ClientPlayer player, String text, Card played);
    public void onCardReceived(ClientPlayer player, Card card, String reason, int newSize);
    
    public void onDisconnect();
}
