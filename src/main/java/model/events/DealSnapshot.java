package model.events;

import java.util.List;
import java.util.Map;

/**
 * Immutable snapshot of the current deal state in the Tre Sette game
 * 
 * Used to transfer all relevant deal information from the model to the controller and UI.
 * Contains player hands, table cards, scores, and other deal state.
 */
public final class DealSnapshot {
    private final int dealIndex;                   
    private final String currentPlayerId;
    private final Map<String,Integer> handSizes;   // <playerId, cards in hand>
    private final Map<String,Integer> wonCards;    // <playerId, number of won cards>
    private final List<String> tableCards;        
    private final String lastTrickWinnerId;        
    private final boolean canCurrentPlayerSign;
    private final boolean paused;
    private final List<String> humanHand;          

    public DealSnapshot(int dealIndex,
                        String currentPlayerId,
                        Map<String,Integer> handSizes,
                        Map<String,Integer> wonCards,
                        List<String> tableCards,
                        String lastTrickWinnerId,
                        boolean canCurrentPlayerSign,
                        boolean paused,
                        List<String> humanHand) {

        this.dealIndex = dealIndex;
        this.currentPlayerId = currentPlayerId;
        this.handSizes = Map.copyOf(handSizes);
        this.wonCards = Map.copyOf(wonCards);
        this.tableCards = List.copyOf(tableCards);
        this.lastTrickWinnerId = lastTrickWinnerId;
        this.canCurrentPlayerSign = canCurrentPlayerSign;
        this.paused = paused;
        if (humanHand == null) this.humanHand = List.of(); 
        else this.humanHand = List.copyOf(humanHand);      
    }
    public int getDealIndex() { return dealIndex; }
    public String getCurrentPlayerId() { return currentPlayerId; }
    public Map<String,Integer> getHandSizes() { return handSizes; }
    public Map<String,Integer> getWonCards() { return wonCards; }
    public List<String> getTableCards() { return tableCards; }
    public String getLastTrickWinnerId() { return lastTrickWinnerId; }
    public boolean canCurrentPlayerSign() { return canCurrentPlayerSign; }
    public boolean isPaused() { return paused; }
    public List<String> getHumanHand() { return humanHand; }
}
