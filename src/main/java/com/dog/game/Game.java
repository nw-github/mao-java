package com.dog.game;

import java.util.Arrays;

public class Game {
    private static final int STARTING_CARDS = 7;
    private static final long SPEECH_TIME = 3; // seconds

    private Deck     mDeck      = new Deck();
    private Deck     mDiscard   = new Deck();
    private int      mTurn      = 0;
    private int      mPlayOrder = +1; // +1 for clockwise, -1 for counterclockwise
    private Player[] mPlayers;
    
    public Game(int players) {
        mPlayers = new Player[players]; // TODO: players will probably be passed in as an argument when networking is done

        for (var suit : Suit.values())
            for (var face : Face.values())
                mDeck.add(new Card(suit, face));

        mDeck.shuffle();
        mDiscard.take(mDeck);

        for (var player : mPlayers)
            for (int i = 0; i < STARTING_CARDS; i++)
                player.cards.take(mDeck);
    }

    private boolean isValidMove(Card card) {
        return (card.getFace() == mDiscard.top().getFace()) ||
            (card.getSuit() == mDiscard.top().getSuit());
    }

    private void punish(Player player, String reason) {
        player.cards.take(mDeck);

        // TODO: broadcast the reason in chat? 
    }

    private void nextTurn() {
        mTurn += mPlayOrder;
        if (mTurn < 0)
            mTurn = mPlayers.length - 1;
        if (mTurn == mPlayers.length)
            mTurn = 0;
    }

    private void checkRequirePhrase(Player player, Card card) {
        switch (card.getFace()) {
        case JACK:
            player.phrase = "here comes the badger";
            break;
        case QUEEN:
            player.phrase = "all hail the queen";
            break;
        case KING:
            player.phrase = "all hail the king";
            break;
        default:
            break;
        }

        if (card.getSuit() == Suit.SPADES) {
            switch (card.getFace()) {
            case JACK:
            case QUEEN:
            case KING:
                player.phrase += " of spades";
                break;
            default:
                player.phrase = String.format("%s of spades", card.getSuit().toString());
                break;
            }
        }

        if (player.cards.size() == 1)
            player.phrase = "mao";

        if (player.cards.size() == 0)
            player.phrase = "all hail the king of mao";
        
        // --------------------------
        
        if (player.phrase != null) {
            if (player.checkThread != null) {
                try {
                    var thr = player.checkThread;
                    thr.interrupt();
                    thr.join();
                } catch (InterruptedException ex) {}
            }

            player.checkThread = new Thread(() -> {
                try {
                    Thread.sleep(SPEECH_TIME * 1000);
                } catch (InterruptedException ex) {
                    // TODO: there should be some check to ensure the interruption
                    //       didn't come from something outside our control, and return
                    //       to sleeping if it did
                }

                if (player.phrase != null) {
                    punish(player, String.format("Failure to say '%s'.", player.phrase));
                    player.phrase = null;
                }

                player.checkThread = null;
            });

            player.checkThread.start();
        }
    }

    public void play(Player player, int cardIndex) throws IllegalArgumentException {
        int index = Arrays.asList(mPlayers).indexOf(player);
        if (index == -1 || cardIndex >= player.cards.size())
            throw new IllegalArgumentException("Player or card index is invalid.");

        var card = player.cards.get(cardIndex);
        var valid = isValidMove(card);

        mDiscard.take(player.cards, cardIndex);
        if (index != mTurn) {
            punish(player, "Playing out of turn.");
            return;
        }

        if (!valid) {
            punish(player, "...");  // TODO: i dont remember what the reason given for an invalid move usually is
            return;
        }

        checkRequirePhrase(player, card);

        // TODO: other rules i cant remember the specifics of right now:
        //       there's a face that reverses the order of play
        //       there's a face that gives someone a card

        nextTurn();
    }
}
