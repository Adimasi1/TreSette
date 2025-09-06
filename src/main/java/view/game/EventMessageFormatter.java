package view.game;

import controller.ViewEvent.*;
import model.events.DealSnapshot;

import java.util.*;

/**
 * Centralizes creation of short Italian user messages for view events
 * GameBoardView delegates to this class to keep layout code separate from text logic.
 */
final class EventMessageFormatter {
    private final Map<String, String> playerNames;
    private final List<String> playerIdsInSeatOrder; 

    public EventMessageFormatter(List<String> playerIdsInSeatOrder, Map<String,String> playerNames){
        this.playerIdsInSeatOrder = List.copyOf(playerIdsInSeatOrder);
        this.playerNames = Map.copyOf(playerNames);
    }

    /** Returns a short message or null if the event should not surface a message. */
    public String format(Object event){
        if(event instanceof CardPlayed cardPlayed){
            String name = playerNames.getOrDefault(cardPlayed.playerId(), cardPlayed.playerId());
            return name+": "+cardPlayed.cardText();
        }
        if(event instanceof SignMade signMade){
            String action = switch(signMade.type()){
                case BUSSO -> "fa il busso";
                case VOLO -> "fa il volo";
                case LISCIO -> "fa il liscio";
                default -> "fa un segno"; };
            return signMade.playerName() +" "+ action;
        }
        if(event instanceof TrickEnded trickEnded){
            DealSnapshot snap = trickEnded.snapshot();
            if(snap!=null){
                String w = snap.getLastTrickWinnerId();
                if(w!=null){
                    String name = playerNames.getOrDefault(w, w);
                    return name+" si aggiudica la presa";
                }
            }
            return null;
        }
        if(event instanceof GameEnded gameEnded){
            Set<String> winners = new HashSet<>(gameEnded.winnerIds());
            List<String> names = new ArrayList<>();
            for(String id : playerIdsInSeatOrder){
                if(winners.contains(id)) {
                    names.add(playerNames.getOrDefault(id,id));
                }
            }
            if(names.isEmpty()) 
                names.addAll(gameEnded.winnerIds());
            if (names.size() > 1) {
                return String.join(", ", names)+" vincono la partita";
            }
            return names.get(0)+" vince la partita";
        }
        return null; 
    }
}
