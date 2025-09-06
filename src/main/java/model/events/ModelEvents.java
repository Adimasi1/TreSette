package model.events;
import java.util.List;
import java.util.Map;

/**
 * Domain events used by the Observer/Observable flow: Deal -> GameManager -> GameController.
 * Each event is an immutable record implementing the marker interface Event.
 */
public final class ModelEvents {
    private ModelEvents() { /* utility container */ }
    /** Marker interface for all model events. */
    public interface Event {}

    // Deal lifecycle
    public static record DealStarted(DealSnapshot snapshot) implements Event {}
    public static record DealEnded(DealSnapshot snapshot) implements Event {}

    // Trick lifecycle
    public static record TrickStarted(DealSnapshot snapshot) implements Event {}
    public static record TrickEnded(DealSnapshot snapshot) implements Event {}

    // Gameplay
    public static record CardPlayed(String playerId, String cardCode, String cardText, DealSnapshot snapshot) implements Event {}
    public static record Sign(SignEvent event, DealSnapshot snapshot) implements Event {}

    // Scoring and game end
    public static record ScoresUpdated(
        Map<String, Integer> dealPoints,
        Map<String, Integer> cumulativeScores,
        String dealWinnerId,
        DealSnapshot lastDealSnapshot) implements Event {}

    public static record GameEnded(
        Map<String, Integer> finalScores,
        List<String> winnerIds) implements Event {}

}
