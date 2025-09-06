package model.events;

import model.player.Player;
import model.sign.SignType;

/**
 * Event object emitted when a player sends a sign (segno) in the Tre Sette game.
 * 
 * Contains the sender, the type of sign, and whether it was sent by the human's teammate.
 */
public final class SignEvent {
    private final Player sender;    
    private final SignType type;
    private final boolean fromTeammateOfHuman;  

    public SignEvent(Player sender, SignType type, boolean fromTeammateOfHuman) {
        this.sender = sender;
        this.type = type;
        this.fromTeammateOfHuman = fromTeammateOfHuman;
    }

    public Player getSender() { return sender; }
    public SignType getType() { return type; }
    public boolean isFromTeammateOfHuman() { return fromTeammateOfHuman; }
    
    public String getDisplayMessage() { 
        return sender.getUsername() + " made the sign " + type.name().toLowerCase(); 
    }
}
