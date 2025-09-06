package model.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.cards.Card;

/**
 * Represents a player (human or bot) in the Tre Sette game.
 *
 * This class holds identity information ({@code id}, {@code username}),
 * the player's {@code hand}, the pile of {@code wonCards} and an optional
 * {@code teamId} assigned once. It provides basic operations used by the
 * game flow, for example {@link #playCard(model.cards.Card)} and
 * {@link #addWonCards(java.util.List)}.
 * Subclasses are {@code HumanPlayer} and {@code BotPlayer}.
 */
public abstract class Player {

    protected final String id;            
    protected final String username;     
    private final Hand hand;             
    private final List<Card> wonCards;   
    private String teamId;               
    
    /**
     * Create a player with empty hand and no won cards yet
     * @param id the player's ID
     * @param username the player's username
     */
    public Player(String id, String username) {
        this.id = id;
        this.username = username;
        this.hand = new Hand();
        this.wonCards = new ArrayList<>();
    }

    /**
     * Assign the team once.
     * @param teamId the ID of the team to assign
     */
    public void assignTeam(String teamId) {
        if (this.teamId == null) this.teamId = teamId;
        else if (!this.teamId.equals(teamId)) throw new IllegalStateException("Team already assigned to " + this.teamId + ", cannot reassign to " + teamId);
    }

    /**
    /**
     * Play (and remove) a card from the player's hand.
     * @param card the card to play
     * @return the played card
     */
    public Card playCard(Card card) {
        if (card == null) throw new IllegalArgumentException("card is null");
        boolean removed = hand.removeCard(card);
        if (!removed)
            throw new IllegalArgumentException("Card " + card + " not in player's hand");
        return card;
    }

    // ---------------- Hand management ----------------
    /**
     * Add a card to the hand.
     * @param card the card to add to the hand
     */
    public void addCard(Card card) { 
        hand.addCard(card); 
    }
    /**
     * Move a card within the hand (by index).
     * @param from the source index in the hand
     * @param to the destination index in the hand
     */
    public void moveCard(int from, int to) { 
        hand.moveCard(from, to); 
    }
        
    /**
     * Add won trick cards to the player's won pile.
     * @param cards the list of cards to add
     */
    public void addWonCards(List<Card> cards) {
        if (cards == null) throw new IllegalArgumentException("cards list is null");
        for (Card c : cards)
            if (c == null) throw new IllegalArgumentException("cards list contains null element");
        if (!cards.isEmpty()) 
            wonCards.addAll(cards);
    }
    
    /** Reset hand and won pile for a fresh new game */
    public void resetForNewGame() { 
        hand.clear(); 
        wonCards.clear(); 
    }
    
    // ---------------- Getters ----------------
    public String getId() { return id; }
    public String getUsername() { return username; }
    public List<Card> getHandCards() { 
        return Collections.unmodifiableList(hand.getAllCards()); 
    }

    public List<String> getHandCardsCode() {
        return hand.getAllCardsCode();
    }

    public List<Card> getWonCards() { 
        return Collections.unmodifiableList(wonCards); 
    }
    public String getTeamId() { return teamId; }
    public boolean hasTeam() { return teamId != null; }
    public boolean hasNoCards() { return hand.isEmpty(); }

    @Override public String toString() { 
        return String.format("%s (%s) - Hand cards: %d", username, id, hand.size()); 
    }
}
