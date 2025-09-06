package model.score;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.player.Team;

/**
 * Manages game and deal scoring for teams/participants.
 * Internally this class keeps the progressive {@code gameScores}, the
 * {@code lastDealTeamPoints} snapshot and uses {@link ScoreCalculator} to
 * compute raw points, apply the "cappotto" rule, the end-of-deal bonus and rounding.
 * The {@code winningScoreTarget} defines when the game ends and the class
 * exposes utilities to update scores and to detect game winners.
 */
public class ScoreManager {
    private final Map<String, Integer> gameScores = new HashMap<>();
    private Map<String, Integer> lastDealTeamPoints = new HashMap<>(); 
    private final ScoreCalculator calculator = new ScoreCalculator(); 
    private String lastDealWinnerId; // last deal-trick winner 
    private final int winningScoreTarget;
    private List<String> finalWinnerIds = List.of();

    public ScoreManager(List<String> participantIds, int winningScoreTarget) {
        for (String id : participantIds) 
            gameScores.put(id, 0);
        this.winningScoreTarget = winningScoreTarget;
    }

    /**
     * Compute and update team scores for the given deal
     * 
     * The method aggregates raw team points through {@link ScoreCalculator},
     * applies the last-trick bonus and the cappotto rule, stores a snapshot
     * in {@code lastDealTeamPoints} and accumulates the result into the
     * game scores.
     * @param teams the list of teams participating in the deal
     * @param lastTrickWinnerId id of the team that won the last trick (maybe null)
     * @return map from team id to integer deal points computed for this deal
     */
    public Map<String, Integer> updateTeamGameScores(List<Team> teams, String lastTrickWinnerId) {
        // (1) Raw doubles from Team
        Map<String, Double> rawTeamPoints = calculator.rawTeamPointsFromTeams(teams);
        // (2) Floor once
        Map<String, Integer> dealTeamPoints = calculator.roundRawPoints(rawTeamPoints);
        // (3) bonus
        if(lastTrickWinnerId != null) {
            calculator.applyWinnerBonus(dealTeamPoints, lastTrickWinnerId); 
            lastDealWinnerId = lastTrickWinnerId;
        } else lastDealWinnerId = null;
        // (4) Cappotto
        calculator.applyCappotto(dealTeamPoints);
        // (5) Snapshot
        lastDealTeamPoints = new HashMap<>(dealTeamPoints);
        // (6) Accumulate to the Game score
        accumulate(dealTeamPoints);
        return dealTeamPoints;
    }
    
    /** Progressive accumulation: gameScore += deal points.
     * @param dealPoints map from participant/team id to deal points
     */
    private void accumulate(Map<String, Integer> dealPoints) {
        for (Map.Entry<String, Integer> e : dealPoints.entrySet()) {
            Integer newScore = gameScores.getOrDefault(e.getKey(), 0) + e.getValue();
            gameScores.put(e.getKey(), newScore);
        }
    }

    /**
     * Check whether one or more participants reached the configured
     * {@code winningScoreTarget}. When a winner is found the list of final
     * winner ids is saved and the method returns {@code true}.
     * @return {@code true} if the game has a winner according to the target
     */
    public boolean checkForGameWinner() {
        int max = gameScores.values().stream().max(Integer::compareTo).orElse(-1);
        if (max < winningScoreTarget) return false;
        List<String> winners = gameScores.entrySet().stream()
            .filter(e -> e.getValue() == max && e.getValue() >= winningScoreTarget)
            .map(Map.Entry::getKey)
            .toList();
        finalWinnerIds = List.copyOf(winners);
        return true;
    }
    
    //------------------------ Getters ------------------------
    // single participant score. Not used, but kept for potential future use
    public int getScore(String participantId) { 
        return gameScores.getOrDefault(participantId, 0); 
    }
    public Map<String, Integer> getAllScores() { 
        return new HashMap<>(gameScores); 
    }
    // single team score. Not used, but kept for potential future use
    public int getTeamScores(String teamId) { 
        return gameScores.getOrDefault(teamId, 0); 
    }
    public Map<String, Integer> getLastDealTeamPoints() { 
        return new HashMap<>(lastDealTeamPoints); 
    }
    /* Winner(s) id(s) */
    public String getLastDealWinnerId() { return lastDealWinnerId; }
    public List<String> getFinalWinnerIds(){ return finalWinnerIds; }
    public int getWinningScoreTarget(){ return winningScoreTarget; }
    public Map<String,Integer> getTeamGameScores(){ return new HashMap<>(gameScores); }
}