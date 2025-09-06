package model.board;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import model.cards.Card;
import model.cards.CardSuit;
import model.player.Player;

/**
 * Represents a single trick in TreSette, storing the cards played by each player in order.
 * Used to determine the winner of the trick and to collect cards for scoring.
 */
public class Trick {

    private CardSuit palo;
    private final LinkedHashMap<Player, Card> plays = new LinkedHashMap<>();

    /** Adds a play to the trick. */
    public void addPlay(Player player, Card card) {
        // not strictly necessary, but useful for validation
        if (player == null || card == null) throw new IllegalArgumentException("player or card null");
        // set the Palo if it's the first card
        if (plays.isEmpty()) {
            palo = card.getSuit();
        }
        plays.put(player, card);
    }
    // Methods used to extract the Trick state
    public Optional<CardSuit> getPalo() { return Optional.ofNullable(palo); }
    public Map<Player, Card> getPlays() { return Collections.unmodifiableMap(plays); }
    public List<Card> getCards() { return new ArrayList<>(plays.values()); }
    public boolean isEmpty() { return plays.isEmpty(); }
    public int size() { return plays.size(); }
}
