package model.player;

import java.util.List;

import model.GameDifficultyState;
import model.board.Table;
import model.cards.Card;
import model.sign.SignType;
import model.events.SignEvent;

/**
 * This class represents a bot player: the decision-making is delegated to the
 * {@link BotStrategyEngine}, due to its complexity
 */

public class BotPlayer extends Player {
    private final BotStrategyEngine strategyEngine; // delega tutta la strategia
    /** A bot player is built with a specific strategy engine, 
     *  according to the level of difficulty
     * @param id the player's ID
     * @param username the player's username
     * @param difficulty the difficulty level
     */
    public BotPlayer(String id, String username, GameDifficultyState difficulty) {
        super(id, username);
        this.strategyEngine = new BotStrategyEngine(difficulty);
    }

    /**
     * Decide which card to play under current difficulty
     * @return the chosen legal card 
     */
    public Card decideCard(Table table, Team team) {
        List<Card> handCards = getHandCards();
        if (handCards.isEmpty()) throw new IllegalStateException("No cards in hand");
        return strategyEngine.chooseCard(table, handCards, team);
    }

    /**
     * Decide whether to emit a sign, and which, this turn.
     * @return chosen sign type or NONE
     */
    public SignType decideSign(Table table) {
        return strategyEngine.chooseSign(table, getHandCards());
    }
    /**
     * Notified when any sign is emitted at the table
     * Currently just forwards to the BotStrategyEngine for future adaptation
     */
    public void onSign(SignEvent event){
        strategyEngine.observeSign(event);
    }
}