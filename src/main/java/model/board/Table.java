package model.board;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import model.cards.Card;
import model.cards.CardSuit;
import model.player.Player;

/**
 * It represents the table in a TreSette game, holding the cards currently played in the ongoing trick.
 * Maintains the order of plays and provides access to the cards on the table
 */
public class Table {

    private Trick currentTrick;
    /**
     * Create a new table with an empty current trick.
     */
    public Table() { 
        currentTrick = new Trick(); 
    }

    /** Adds a card to the current trick. */
    public void addCard(Player player, Card card) {
        currentTrick.addPlay(player, card);
    }

    /** Clear table for new trick.
     * @return list of cards that were on table 
     */
    public List<Card> clearTableAndReturnCards() {
        List<Card> played = new ArrayList<>(currentTrick.getCards());
        currentTrick = new Trick();
        return played;
    }

    // Those methods are used to extract the Table state
    public Optional<CardSuit> getPalo() { return currentTrick.getPalo(); }
    public Map<Player, Card> getCardsOnTable() { return currentTrick.getPlays(); }
    public boolean isEmpty() { return currentTrick.isEmpty(); }
    public int size() { return currentTrick.size(); }

}
