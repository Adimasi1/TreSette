package model.sign;

import java.util.List;

import model.board.Table;
import model.events.SignEvent;
import model.player.Player;
import model.player.BotPlayer;

/**
 * Manages sign emission rules during a deal.
 * 
 * This class enforces that only one sign can be used per trick and that
 * only the player whose turn it is may send a sign. It keeps the
 * list of {@code players} (index 0 is the human) and the boolean
 * {@code signUsedThisTrick} tracking whether a sign was already sent in the
 * current trick. The human teammate is at index 2.
 */
public final class SignManager {
    private final List<Player> players;
    private boolean signUsedThisTrick = false; 

    /**
     * Create a SignManager for the deal participants.
     * @param players ordered list of players
     */
    public SignManager(List<Player> players) { 
        this.players = players; 
    }

    /**
     * Returns whether the given player is currently allowed to emit a sign
     * according to the rules (one sign per trick, must be the current turn,
     * and table must be empty).
     * @param player the player that wants to sign
     * @param table the current {@link Table} instance used to check if the table is empty
     * @param currentTurnPlayer the player whose turn it currently is
     * @return {@code true} when sign emission is allowed now, {@code false} otherwise
     */
    public boolean canPlayerMakeSign(Player player, Table table, Player currentTurnPlayer) {
        if (signUsedThisTrick) return false;
        if (player == null || table == null) return false;
        if (player != currentTurnPlayer) return false;
        if (!table.getCardsOnTable().isEmpty()) return false;
        return true;
    }

    /**
     * Send a sign and return the generated {@link SignEvent}.
     * <p>
     * If sign emission is not allowed according to {@link #canPlayerMakeSign},
     * the method currently throws {@link IllegalStateException}.
     * @param sender the player who sends the sign
     * @param type the {@link SignType} to emit
     * @param table the current {@link Table} used to validate the emission rules
     * @param currentTurnPlayer the player whose turn it currently is
     * @return the created {@link SignEvent}
     */
    public SignEvent sendSign(Player sender, SignType type, Table table, Player currentTurnPlayer) {
        if (!canPlayerMakeSign(sender, table, currentTurnPlayer)) {
            throw new IllegalStateException("Player cannot make a sign now");
        }
        signUsedThisTrick = true;
        // Flag the event as coming from the human's teammate only when the sender is at index 2
        int senderIndex = players.indexOf(sender);
        boolean fromTeammateOfHuman = (senderIndex == 2);
        SignEvent event = new SignEvent(sender, type, fromTeammateOfHuman);
        // Notify all bots so they can observe the sign (currently not implemented)
        for (Player p : players) {
            if (p instanceof BotPlayer bot) {
                bot.onSign(event);
            }
        }
        return event;
    }

    /**
     * Reset sign state for a new deal.
     */
    public void resetDeal() { signUsedThisTrick = false; }
    /**
     * Called when a trick ends to allow sign emission in the next trick.
     */
    public void onTrickEnded() { signUsedThisTrick = false; }

}
