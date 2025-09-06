package model.score;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.player.Team;
import model.GameRules;

/**
 * Helper utility that encapsulates score calculation logic used by
 * {@link ScoreManager}.
 * 
 * It can compute raw team points from {@link model.player.Team} instances,
 * apply the cappotto rule and the last-trick bonus, and round double
 * points to integer deal points.
 */
class ScoreCalculator {

    /**
     * Compute raw double points per team from a list of {@link Team}
     * instances.
     * @param teams list of teams to aggregate
     * @return map from team id to raw (double) points for the current deal
     */
    public Map<String, Double> rawTeamPointsFromTeams(List<Team> teams) {
        Map<String, Double> map = new HashMap<>();
        for (Team t : teams) {
            map.put(t.getId(), t.getCurrentDealRawPoints());
        }
        return map;
    }

    /**
     * Apply the cappotto rule to the supplied match points map. When one
     * participant has zero points, other participants receive the
     * {@link CAPPOTTO_SCORE} value.
     * @param matchPoints a mutable map from participant/team id to integer deal points
     */
    public void applyCappotto(Map<String, Integer> matchPoints) {
        boolean cappotto = matchPoints.values().stream()
                            .anyMatch(p -> p == 0);
        if (!cappotto) return;
        for (Map.Entry<String, Integer> entry : matchPoints.entrySet()) {
            if (entry.getValue() == 0) {
                continue;
            } else {
                entry.setValue(GameRules.CAPPOTTO_SCORE); // 17 points
            }
        }
    }

    /**
     * Apply the last-trick winner bonus (+1) to the winner entry in the
     * mutable {@code playersDealPoints} map.
     * @param playersDealPoints mutable map of deal points by participant/team id
     * @param winnerId id of the winner to award the bonus to
     */
    public void applyWinnerBonus(Map<String, Integer> playersDealPoints, String winnerId) {
        if (winnerId == null) return;
        int newPts = playersDealPoints.getOrDefault(winnerId, 0) + 1;
        playersDealPoints.put(winnerId, newPts);
    }

    /**
     * Rounds raw double points to integer deal points using the following rule: 
     * floor the value, but bump by one when the double
     * part is {@code >= 0.9}.
     * @param raw map from id to raw double points
     * @return new map from id to rounded integer points
     */
    public Map<String, Integer> roundRawPoints(Map<String, Double> raw) {
        Map<String, Integer> out = new HashMap<>();
        for (Map.Entry<String, Double> entry : raw.entrySet()) {
            double value = entry.getValue();
            int base = (int) Math.floor(value);
            double diff = value - base;
            if (diff >= 0.9) base += 1;
            out.put(entry.getKey(), base);
        }
        return out;
    }
}
