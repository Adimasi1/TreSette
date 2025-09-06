package controller;

import java.util.Map;
import model.events.DealSnapshot;
import model.sign.SignType;

/**
 * Events sent to the UI. Each event is a immutable record
 * containing only the data needed by the UI (identifiers, textual labels,
 * and a immutable {@link DealSnapshot} when relevant).
 */
public interface ViewEvent {

    /** Notifies that a new deal has started. The snapshot describes the initial state. */
    public record DealStarted(DealSnapshot snapshot) implements ViewEvent {}

    /** Notifies that a new trick has started within the active deal. */
    public record TrickStarted(DealSnapshot snapshot) implements ViewEvent {}

    /** Notifies that a player has played a card; the snapshot represents the post-play state. */
    public record CardPlayed(String playerId, String cardCode, String cardText,
                             DealSnapshot snapshot) implements ViewEvent {}

    /** Notifies that a trick has ended; snapshot holds the state after trick resolution. */
    public record TrickEnded(DealSnapshot snapshot) implements ViewEvent {}

    /** Notifies that a sign (segno) was made by a player. Includes basic player info. */
    public record SignMade(String playerId, String playerName, SignType type, 
                           DealSnapshot snapshot) implements ViewEvent {}

    /** Notifies that the current deal ended; snapshot is the final deal state. */
    public record DealEnded(DealSnapshot snapshot) implements ViewEvent {}

    /** Notifies updated scores after a deal resolution. */
    public record ScoresUpdated(Map<String,Integer> dealPoints, Map<String,Integer> cumulativeScores,
                                String dealWinnerId, DealSnapshot snapshot) implements ViewEvent {}

    /** Notifies that the entire game has finished; provides final scores and winners. */
    public record GameEnded(Map<String,Integer> finalScores, 
                            java.util.List<String> winnerIds) implements ViewEvent {}

}
