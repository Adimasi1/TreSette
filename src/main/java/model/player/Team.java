package model.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.cards.Card;

/**
 * Represents a team in Tre Sette.
 * 
 * A {@code Team} holds an {@code id} and up to two {@link Player} members.
 * Members are assigned the team's id through {@link Player#assignTeam(String)} at
 * construction and the internal member list is exposed as an unmodifiable view.
 */
public final class Team {
    private final String id;
    private final List<Player> members;

    /**
     * Create a team with the given id and members. The provided member list is
     * validated for size and duplicates, and members are assigned the team id.
     * @param id the team's identifier
     * @param members the list of players to include (up to 2)
     */
    public Team(String id, List<Player> members) {
        this.id = id;
        if (members.size() > 2) {
            throw new IllegalArgumentException("Team cannot have more than 2 members in Tre Sette");
        }
        List<Player> copy = new ArrayList<>(members);
        int duplicate = (int) copy.stream()
                        .distinct()
                        .count();
        if (duplicate != copy.size()) {
            throw new IllegalArgumentException("Duplicate player in team");
        }

        for (Player p : copy) {
            p.assignTeam(id);
        }
        this.members = Collections.unmodifiableList(copy);
    }

    /**
     * Aggregate the raw points of all won cards for this team's members in the
     * current deal.
     * @return sum of raw card points.
     */
    public double getCurrentDealRawPoints() {
        double sum = 0;
        for (Player p : members) {
            for (Card c : p.getWonCards()) {
                sum += (c.getValue().getPoints());
            }
        }
        return sum;
    }

    /**
     * Check whether the specified player is a member of this team.
     * @param player the player to check
     * @return {@code true} if the player is contained in the team's members
     */
    public boolean contains(Player player) { return members.contains(player); }

    public String getId() { return id; }
    public List<Player> getMembers() { return members; }
}
