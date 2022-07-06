package com.dog.game;

import java.util.*;

import com.dog.Utils;
import com.dog.game.net.*;
import com.dog.net.Message;
import com.dog.net.Server;

public class Game {
    public static final int MIN_PLAYERS = 2;
    public static final int MAX_PLAYERS = 8;
    private static final int STARTING_CARDS = 7;

    private final List<Player> mPlayers = new ArrayList<>();
    private final Deck         mDeck    = new Deck(); // The deck players draw/are punished from
    private final Deck         mDiscard = new Deck(); // The deck players play into
    private final Server       mServer;
    private final int          mMaxPlayers;
    private Suit               mSuit;
    private int                mTurn       = 0;
    private int                mPlayOrder  = +1; // +1 for clockwise, -1 for counterclockwise
    private boolean            mHasStarted = false;

    public Game(Server server, int maxPlayers) {
        mServer     = server;
        mMaxPlayers = Utils.clamp(Game.MIN_PLAYERS, Game.MAX_PLAYERS, maxPlayers);
    }

    public boolean start() {
        if (mHasStarted || mPlayers.size() < MIN_PLAYERS)
            return false;

        for (var suit : Suit.values())
            for (var face : Face.values())
                mDeck.add(new Card(suit, face));

        mDeck.shuffle();
        discard(mDeck, 0);

        for (var player : mPlayers)
            for (int i = 0; i < STARTING_CARDS; i++)
                player.getCards().take(mDeck);

        for (var player : mPlayers)
            mServer.send(player.getConnection(), ClientGame.fromGame(ServerMessage.GAME_START, this, player));
            
        return mHasStarted = true;
    }

    public boolean canAddPlayer() {
        return !isGameStarted() && mPlayers.size() < mMaxPlayers;
    }

    public boolean addPlayer(Player player) {
        if (!canAddPlayer())
            return false;

        mPlayers.add(player);
        return true;
    }

    public boolean removePlayer(Player player) {
        if (player != null && mPlayers.remove(player)) {
            if (isGameStarted()) {
                while (player.getCards().size() != 0)
                    mDeck.take(player.getCards());
                
                mDeck.shuffle();

                if (mPlayers.size() == 1)
                    endGame(mPlayers.get(0));
            }
            return true;
        }

        return false;
    }

    public boolean isGameStarted() {
        return mHasStarted;
    }

    public int getMaxPlayers() {
        return mMaxPlayers;
    }

    /**
     * Play or draw a card for a specific player.
     * @param player Target player
     * @param cardIndex Index into the players cards to play. 
     *  A negative index is treated as drawing a card.
     * @param userText  The user's input text
     * @throws IllegalArgumentException Thrown if the card index >= the player's deck size or the player is invalid
     * @throws GameException Thrown if the game hasn't started
     */
    public void play(Player player, int cardIndex, String userText) throws IllegalArgumentException, GameException {
        if (!isGameStarted())
            throw new GameException("Cannot play before the game has started.");
        
        int index = mPlayers.indexOf(player);
        if (index == -1 || cardIndex >= player.getCards().size())
            throw new IllegalArgumentException("Player or card index is invalid.");

        boolean validTurn = index == mTurn;
        if (cardIndex >= 0) {
            var valid = isValidMove(player.getCards().get(cardIndex));
            var card  = discard(player.getCards(), cardIndex);

            mServer.sendAll(new Message(ServerMessage.PLAY)
                .withInt(player.getId())
                .withString(userText)
                .withString(card.toString()));

            if (!valid)
                punish(player, "Invalid move.");  // TODO: i dont remember what the reason given for an invalid move usually is            

            checkText(player, card, userText);

            switch (card.face()) {
            case JACK:
                mPlayOrder = mPlayOrder == +1 ? -1 : +1;
                break;
            case SEVEN:
                punish(mPlayers.get(nextPlayer()), "Have a nice day!");
                break;
            case ACE:
                if (validTurn)
                    mTurn = nextPlayer();
                break;
            default:
                break;
            }
        } else {
            punish(player, "");
        }

        if (!validTurn)
            punish(player, "Playing out of turn.");
        else
            mTurn = nextPlayer();

        if (player.getCards().size() == 0)
            endGame(player);
    }

    public List<Player> getPlayers() {
        return mPlayers;
    }

    public Card getPileTop() {
        return mDiscard.top();
    }

    public int getDrawDeckSize() {
        return mDeck.size();
    }

    public void punish(Player player, String reason) throws IllegalArgumentException, GameException {
        if (!isGameStarted())
            throw new GameException("Cannot play before the game has started.");
        if (mPlayers.indexOf(player) == -1)
            throw new IllegalArgumentException("Player is invalid.");

        if (mDeck.size() > 0)
        {
            var card = player.getCards().take(mDeck);
            if (mDeck.size() == 0)
            {
                var top = mDiscard.top();
                mDiscard.remove(top);

                mDeck.addAll(mDiscard);
                mDeck.shuffle();

                mDiscard.clear();
                mDiscard.add(top);
            }

            mServer.sendAllExcept(player.getConnection(), new Message(ServerMessage.RECV_CARD)
                .withInt(player.getId())
                .withString("")
                .withString(reason)
                .withInt(mDeck.size()));
            mServer.send(player.getConnection(), new Message(ServerMessage.RECV_CARD)
                .withInt(player.getId())
                .withString(card.toString())
                .withString(reason)
                .withInt(mDeck.size()));
        }
    }

    // Utility

    private boolean isValidMove(Card card) {
        return (card.face() == mDiscard.top().face()) ||
            (card.suit() == mSuit);
    }

    private int nextPlayer() {
        int turn = mTurn + mPlayOrder;
        if (turn < 0)
            turn = mPlayers.size() - 1;
        if (turn == mPlayers.size())
            turn = 0;
        return turn;
    }

    private void checkText(Player player, Card card, String userText) throws IllegalArgumentException, GameException {
        String spades = card.suit() == Suit.SPADES ? " of spades" : "";

        var phrases = new ArrayList<String>();
        switch (card.face()) {
        case JACK:
            phrases.add("here comes the badger" + spades);
            break;
        case QUEEN:
            phrases.add("all hail the queen" + spades);
            break;
        case KING:
            phrases.add("all hail the king" + spades);
            break;
        default:
            if (card.suit() == Suit.SPADES)
                phrases.add(String.format("%s of spades", card.face().toString()));
            
            if (card.face() == Face.SEVEN)
                phrases.add("have a nice day");
            break;
        }

        if (player.getCards().size() == 1)
            phrases.add("mao");

        if (player.getCards().size() == 0)
            phrases.add("all hail the king of mao");

        var parts = Arrays.asList(userText.split("\n"));
        var it    = parts.iterator();
        while (it.hasNext()) {
            var current = it.next();
            if (matchText(card, phrases, current))
                it.remove();
        }

        for (var phrase : phrases)
            punish(player, String.format("Failure to say '%s'", phrase));

        if (!userText.isEmpty() && parts.size() > 0)
            punish(player, "Talking.");
    }

    private boolean matchText(Card card, ArrayList<String> phrases, String userText) {
        if (card.face() == Face.JACK) {
            try {
                mSuit = Suit.valueOf(userText);
                return true;
            } catch (IllegalArgumentException ex) { }
        }

        var it = phrases.iterator();
        while (it.hasNext()) {
            if (compare(userText, it.next())) {
                it.remove();
                return true;
            }
        }

        return false;
    }

    private boolean compare(String userText, String target) {
        // TODO: compare less rigidly to allow typos/punctuation
        return userText.toLowerCase().strip().equals(target.toLowerCase());
    }

    private Card discard(Deck deck, int index) {
        var card = deck.get(index);
        mDiscard.take(deck, index);

        mSuit = card.suit();
        return card;
    }

    private void endGame(Player winner) {
        mServer.sendAll(new Message(ServerMessage.GAME_END)
            .withInt(winner.getId()));

        mHasStarted = false;
        // TODO: reset server/go back to lobby
    }
}
