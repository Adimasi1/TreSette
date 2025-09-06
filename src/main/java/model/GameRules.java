package model;

import java.util.List;
import java.util.Map;

import model.board.Table;
import model.cards.Card;
import model.cards.CardSuit;
import model.cards.CardValue;
import model.player.Player;

/** Core rules and constants for Tre Sette. This is a static utility
 * All the methods are used by different classes (ex. Deal) in order
 * to enforce game rules and validate moves.
 */
public final class GameRules {
    private GameRules() {}

    // --------- Constants ---------
    public static final int TOTAL_DECK_CARDS = 40;
    public static final int CARDS_PER_PLAYER = 10;
    public static final int MIN_PLAYERS = 2;
    public static final int MAX_PLAYERS = 4;

    // 4 of Denari: starting card
    public static final Card STARTING_CARD = new Card(CardSuit.DENARI, CardValue.QUATTRO);

    // Winning scores
    public static final int WINNING_SCORE_31 = 31;
    public static final int WINNING_SCORE_21 = 21;
    public static final int WINNING_SCORE_11 = 11;
    public static final int LAST_TRICK_BONUS = 1;
    public static final int CAPPOTTO_SCORE = 17;

    // --------- Game rules methods ---------
    /** True if the card may be legally played (follow the palo suit if possible)
     * @param player the player who's playing the card
     * @param card the card played
     * @param table the current game table
     * @return true if the play respects the palo suit
     */
    public static boolean isValidPlay(Player player, Card card, Table table) {
        if (table.isEmpty()) return true;
        CardSuit palo = table.getPalo().orElseThrow(() ->
                                new IllegalStateException("Palo is missing!"));
        if (card.getSuit() == palo) return true;
        boolean hasPalo = player.getHandCards().stream()
                                .anyMatch(c -> c.getSuit() == palo);
        return !hasPalo;
    }

    /** Return the current winning card respecting palo and highest game value.
     * @param cardsOnTable
     * @param palo
     * @return winning card (never null)
     */
    public static Card getWinningCard(List<Card> cardsOnTable, CardSuit palo) {
        if (cardsOnTable == null || cardsOnTable.isEmpty()) {
            throw new IllegalArgumentException("cardsOnTable null or empty");
        }
        Card best = null; 
        int bestValue = -1;
        for (Card c : cardsOnTable) {
            if (c.getSuit() == palo) {
                int v = c.getValue().getGameValue();
                if (v > bestValue) { 
                    bestValue = v; 
                    best = c; }
            }
        }
        if (best == null) { // very unlikely and should not happen
            throw new IllegalStateException("No card with palo " + palo + " on table.");
        }
        return best;
    }

    /** Determine if challenger beats currentWinning under given palo.
     * @param challenger a card that challenges the current winning card
     * @param currentWinning the current winning card
     * @param palo the current palo suit
     * @return true if challenger becomes new winner
     */
    public static boolean cardBeats(Card challenger, Card currentWinning, CardSuit palo) {
        if (challenger == null) return false;
        if (currentWinning == null) return true;
        // challenger needs to have the palo suit to beat
        if (challenger.getSuit() != palo) return false;
        // currentWinning should always have the palo. If not it's a violated invariant
        if (currentWinning.getSuit() != palo) {
            throw new IllegalStateException("currentWinning without palo suit (logic error)");
        }
        return challenger.getValue().getGameValue() > currentWinning.getValue().getGameValue();
    }

    /** Determine trick winner from map of plays.
     * @param plays map of players to their played cards
     * @param palo the current palo suit
     * @return winning player (never null)
     */
    public static Player getTrickWinner(Map<Player, Card> plays, CardSuit palo) {
        if (plays == null || plays.isEmpty()) {
            throw new IllegalArgumentException("plays null or empty");
        }
        if (palo == null) {
            throw new IllegalArgumentException("palo null");
        }
        Player winner = null; 
        int best = -1;
        for (Map.Entry<Player, Card> e : plays.entrySet()) {
            Card c = e.getValue();
            if (c.getSuit() == palo) {
                int v = c.getValue().getGameValue();
                if (v > best) { // compare values
                    best = v;
                    winner = e.getKey();
                }
            }
        }
        if (winner == null) {
            throw new IllegalStateException("No card with palo " + palo + " among plays");
        }
        return winner;
    }
}
