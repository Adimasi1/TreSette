package model.deal;
import java.util.List;

import model.GameRules;
import model.player.Player;
import model.player.Team;

/**
 * 2v2 variant of {@link Deal}. the deal finishes when all hands are empty.
 */
public final class Deal2v2 extends Deal {
    protected final BotMoveScheduler botScheduler; // hide the parallel Deal field 
    /**
     * Create a 2v2 deal instance.
     * @param dealIndex the sequential index of the deal in the game
     * @param players ordered list of 4 players participating in the deal
     */
    public Deal2v2(int dealIndex, List<Player> players, List<Team> teams) {
        super(dealIndex, players);
        if (players.size() != 4) 
            throw new IllegalArgumentException("Deal2v2 requires exactly 4 players");
        this.botScheduler = new BotMoveScheduler(this, BOT_MOVE_DELAY_MS, teams);
    }

    @Override
    protected boolean isDealFinished() {
        return getPlayers().stream()
                .allMatch(Player::hasNoCards);
    }

    @Override
    protected int determineStartingPlayerIndex() {
        for (int i = 0; i < players.size(); i++) {
            boolean hasStartingCard = players.get(i).getHandCards().stream()
                    .anyMatch(c -> c.equals(GameRules.STARTING_CARD));
            if (hasStartingCard) return i;
        }
        throw new IllegalStateException("STARTING_CARD " + GameRules.STARTING_CARD + " not found");
    }
}
