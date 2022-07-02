package com.dog.game;

import java.util.Arrays;

import com.dog.game.net.Player;
import com.dog.net.Server;

public class Game {
    private static final int STARTING_CARDS = 7;
    public static final int MIN_PLAYERS = 2;
    public static final int MAX_PLAYERS = 8;

    private final Player[] mPlayers;
    private final Server   mServer;
    private Deck           mDeck      = new Deck();
    private Deck           mDiscard   = new Deck();
    private int            mTurn      = 0;
    private int            mPlayOrder = +1; // +1 for clockwise, -1 for counterclockwise

    public Game(Server server, Player[] players) {
        mPlayers = players;
        mServer  = server;

        for (var suit : Suit.values())
            for (var face : Face.values())
                mDeck.add(new Card(suit, face));

        mDeck.shuffle();
        mDiscard.take(mDeck);

        for (var player : mPlayers)
            for (int i = 0; i < STARTING_CARDS; i++)
                player.getCards().take(mDeck);
    }

    public void punish(Player player, String reason) {
        player.getCards().take(mDeck);

        // TODO: broadcast the reason in chat?
        
    }

    public void play(Player player, int cardIndex) throws IllegalArgumentException {
        int index = Arrays.asList(mPlayers).indexOf(player);
        if (index == -1 || cardIndex >= player.getCards().size())
            throw new IllegalArgumentException("Player or card index is invalid.");

        var card  = player.getCards().get(cardIndex);
        var valid = isValidMove(card);

        mDiscard.take(player.getCards(), cardIndex);
        if (index != mTurn) {
            punish(player, "Playing out of turn.");
            return;
        }

        if (!valid) {
            punish(player, "Wrong card.");  // TODO: i dont remember what the reason given for an invalid move usually is
            return;
        }

        // TODO: other rules i cant remember the specifics of right now:
        //       there's a face that reverses the order of play
        //       there's a face that gives someone a card

        nextTurn();
    }

    public Player[] getPlayers() {
        return mPlayers;
    }

    public Card getPileTop() {
        return mDiscard.top();
    }

    private boolean isValidMove(Card card) {
        return (card.face() == mDiscard.top().face()) ||
            (card.suit() == mDiscard.top().suit());
    }

    private void nextTurn() {
        mTurn += mPlayOrder;
        if (mTurn < 0)
            mTurn = mPlayers.length - 1;
        if (mTurn == mPlayers.length)
            mTurn = 0;
    }

}
