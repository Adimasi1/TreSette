package model.player;

/**
 * Human player marker.
 * Intentionally no extra state or behavior now, 
 * but it makes calls explicit (new HumanPlayer vs bot)
 */
public class HumanPlayer extends Player {

    /**
     * Creates a new human player with the specified id and username.
     * @param id the player's ID
     * @param username the player's username
     */
    public HumanPlayer(String id, String username) {
        super(id, username);
    }
   
}
