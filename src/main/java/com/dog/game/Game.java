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

    private final List<Player> players = new ArrayList<>();
    private final Deck         deck    = new Deck(); // The deck players draw/are punished from
    private final Deck         discard = new Deck(); // The deck players play into
    private final Server       server;
    private final int          maxPlayers;
    private Suit               currentSuit;
    private int                currentTurn = 0;
    private int                playOrder   = +1; // +1 for clockwise, -1 for counterclockwise
    private boolean            hasStarted  = false;

    public Game(Server server, int maxPlayers) {
        this.server     = server;
        this.maxPlayers = Utils.clamp(Game.MIN_PLAYERS, Game.MAX_PLAYERS, maxPlayers);
    }

    public boolean start() {
        if (hasStarted || players.size() < MIN_PLAYERS)
            return false;

        for (var suit : Suit.values())
            for (var face : Face.values())
                deck.add(new Card(suit, face));

        deck.shuffle();
        discard(deck, 0);

        for (var player : players)
            for (int i = 0; i < STARTING_CARDS; i++)
                player.getCards().take(deck);

        for (var player : players)
            server.send(player.getConnection(), ClientGame.fromGame(ServerMessage.GAME_START, this, player));
            
        return hasStarted = true;
    }

    public boolean canAddPlayer() {
        return !isGameStarted() && players.size() < maxPlayers;
    }

    public boolean addPlayer(Player player) {
        if (!canAddPlayer())
            return false;

        players.add(player);
        return true;
    }

    public boolean removePlayer(Player player) {
        if (player != null && players.remove(player)) {
            if (isGameStarted()) {
                while (player.getCards().size() != 0)
                    deck.take(player.getCards());
                
                deck.shuffle();

                if (players.size() == 1)
                    endGame(players.get(0));
            }
            return true;
        }

        return false;
    }

    public boolean isGameStarted() {
        return hasStarted;
    }

    public int getMaxPlayers() {
        return maxPlayers;
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
        
        int index = players.indexOf(player);
        if (index == -1 || cardIndex >= player.getCards().size())
            throw new IllegalArgumentException("Player or card index is invalid.");

        boolean validTurn = index == currentTurn;
        if (cardIndex >= 0) {
            var valid = isValidMove(player.getCards().get(cardIndex));
            var card  = discard(player.getCards(), cardIndex);

            server.sendAll(new Message(ServerMessage.PLAY)
                .withInt(player.getId())
                .withString(userText)
                .withString(card.toNetString()));

            if (!valid)
                punish(player, "Invalid move.");  // TODO: i dont remember what the reason given for an invalid move usually is            

            checkText(player, card, userText);

            switch (card.face()) {
            case JACK:
                playOrder = playOrder == +1 ? -1 : +1;
                break;
            case SEVEN:
                punish(players.get(nextPlayer()), "Have a nice day!");
                break;
            case ACE:
                if (validTurn)
                    currentTurn = nextPlayer();
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
            currentTurn = nextPlayer();

        if (player.getCards().size() == 0)
            endGame(player);
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Card getDiscardTop() {
        if (discard.size() != 0)
            return discard.top();
        return null;
    }

    public int getDrawDeckSize() {
        return deck.size();
    }

    public void punish(Player player, String reason) throws IllegalArgumentException, GameException {
        if (!isGameStarted())
            throw new GameException("Cannot play before the game has started.");
        if (players.indexOf(player) == -1)
            throw new IllegalArgumentException("Player is invalid.");

        if (deck.size() > 0)
        {
            var card = player.getCards().take(deck);
            if (deck.size() == 0)
            {
                var top = discard.top();
                discard.remove(top);

                deck.addAll(discard);
                deck.shuffle();

                discard.clear();
                discard.add(top);
            }

            server.sendAllExcept(player.getConnection(), new Message(ServerMessage.RECV_CARD)
                .withInt(player.getId())
                .withString("")
                .withString(reason)
                .withInt(deck.size()));
            server.send(player.getConnection(), new Message(ServerMessage.RECV_CARD)
                .withInt(player.getId())
                .withString(card.toNetString())
                .withString(reason)
                .withInt(deck.size()));
        }
    }

    // Utility

    private boolean isValidMove(Card card) {
        return (card.face() == discard.top().face()) ||
            (card.suit() == currentSuit);
    }

    private int nextPlayer() {
        int turn = currentTurn + playOrder;
        if (turn < 0)
            turn = players.size() - 1;
        if (turn == players.size())
            turn = 0;
        return turn;
    }

    private void checkText(Player player, Card card, String userText) throws IllegalArgumentException, GameException {
        String spades = card.suit() == Suit.SPADES ? " of spades" : "";

        var phrases = new ArrayList<String>();
        switch (card.face()) {
        case JACK:
            phrases.add("Here comes the badger" + spades);
            break;
        case QUEEN:
            phrases.add("All hail the queen" + spades);
            break;
        case KING:
            phrases.add("All hail the king" + spades);
            break;
        default:
            if (card.suit() == Suit.SPADES)
                phrases.add(card.toString());
            
            if (card.face() == Face.SEVEN)
                phrases.add("Have a nice day");

            break;
        }

        if (player.getCards().size() == 1)
            phrases.add("Mao");

        if (player.getCards().size() == 0)
            phrases.add("All hail the king of Mao");

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
                currentSuit = Suit.valueOf(userText);
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

    private static boolean compare(String userText, String target) {
        var replacements = new String[][] {
            {"1", "ace"},
            {"a", "ace"},
            {"2", "two"},
            {"3", "three"},
            {"4", "four"},
            {"5", "five"},
            {"6", "six"},
            {"7", "seven"},
            {"8", "eight"},
            {"9", "nine"},
            {"j", "jack"},
            {"q", "queen"},
            {"k", "king"},
        };

        userText = userText.toLowerCase().strip();
        for (var pair : replacements)
            userText.replace(pair[0], pair[1]);

        if (Utils.endsWithAny(userText, ".", "!", "?"))
            userText = userText.substring(0, userText.length() - 1);

        return userText.equals(target.toLowerCase());
    }

    private Card discard(Deck deck, int index) {
        var card = deck.get(index);
        discard.take(deck, index);

        currentSuit = card.suit();
        return card;
    }

    private void endGame(Player winner) {
        server.sendAll(new Message(ServerMessage.GAME_END)
            .withInt(winner.getId()));

        hasStarted = false;
        server.stop(); // TODO: reset server/go back to lobby
    }
}
