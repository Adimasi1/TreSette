package controller;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import model.sign.SignType;
import model.player.Player;
import model.GameManager;
import model.cards.Card;
import model.events.ModelEvents;
import model.events.SignEvent;

/**
 * Controller class linking the model and the UI.
 * This class adapts model {@link model.events.ModelEvents} into
 * {@link ViewEvent} records and publishes them to UI observers.
 * It also exposes an imperative API used by the UI to
 * request model actions (i.e. start, pause, play, move cards, sign).
 *
 */
@SuppressWarnings("deprecation")
public final class GameController extends Observable implements Observer {

    private final GameManager gameManager;

    public GameController(GameManager gameManager) {
        this.gameManager = gameManager;
        gameManager.addObserver(this);
    }

    // ---------- UI -> Controller -> Model ------------
    /**
     * Starts the game by delegating to {@link GameManager#startGame()}.
     * This initializes and starts the first deal of the match.
     */
    public void startGame() { gameManager.startGame(); }
    
    /**
     * Attempts to play the card identified by {@code cardCode} for the
     * player with id {@code playerId}.
     *
     * @param playerId id of the player acting the play
     * @param cardCode code of the card to play
     * @return {@code true} if the play was accepted by the model
     *         or {@code false} if the card was not found rejected the play
     */
    public boolean playCard(String playerId, String cardCode) {
        Player player = findPlayer(playerId);
        if (player == null) throw new IllegalArgumentException("Unknown player id: " + playerId);

        Card card = player.getHandCards().stream()
            .filter(c -> cardCode != null && cardCode.equals(c.getCode()))
            .findFirst()
            .orElse(null);
        if (card == null) return false;
        return gameManager.playHumanCard(player, card);
    }
    /**
     * Attempts to emit a sign (segno) on behalf of the given player.
     *
     * @param playerId id of the player attempting the sign
     * @param type the requested {@link SignType}
     * @return {@code true} if the model accepted the sign
     *         {@code false} when the current deal does not allow that player to sign
     */
    public boolean makeSign(String playerId, SignType type) {
        Player p = findPlayer(playerId);
        if (p == null) throw new IllegalArgumentException("Unknown player id: " + playerId);
        if (!gameManager.canPlayerMakeSign(p)) return false;
        gameManager.handlePlayerSign(p, type);
        return true;
    }

    /** Pause the model game loop. Delegates to {@link GameManager#pauseGame()}. */
    public void pause() { gameManager.pauseGame(); }

    /** Resume the model game loop. Delegates to {@link GameManager#resumeGame()}. */
    public void resume() { gameManager.resumeGame(); }

    /** Start the next deal. Delegates to {@link GameManager#startNextDeal()}. */
    public void startNextDeal() { gameManager.startNextDeal(); }

    /** Stop the game. Delegates to {@link GameManager#stopGame()}. */
    public void stopGame() { gameManager.stopGame(); }

    /**
     * Accept end-of-deal results and start the next deal.
     */
    public void confirmDealResults(){
        if(gameManager.isCurrentDealOver() && !gameManager.isGameOver()){
            gameManager.resumeGame(); 
            gameManager.startNextDeal();
        }
    }

    /**
     * @return {@code true} if there is no active deal or if the current deal already ended.
     */
    public boolean isCurrentDealOver(){ 
        return gameManager.isCurrentDealOver(); 
    }
 
    /** Move a card in the human player's hand 
     *  @param from source card index
     *  @param to target card index
    */
    public List<String> moveHumanCard(int from, int to) {
        Player player = findPlayer("P1");
        if (player == null) throw new IllegalStateException("The Human player is not found");
        int size = player.getHandCards().size();
        if(to < 0) to = 0; 
        else if(to > size) to = size;
        player.moveCard(from, to);
        return List.copyOf(player.getHandCardsCode());
    }
    // ---------- Model -> Controller -> UI ------------
    //

    // -------------- Observer of GameManager (ModelEvents) --------------
    /** the Override of update handles the model events by choosing the right
     *  {@link ViewEvent} to publish (notifying the UI)
     *  @param observable the observable object
     *  @param argument the event argument
     * 
     */
    @Override
    public void update(Observable observable, Object argument) {
        if (!(argument instanceof ModelEvents.Event)) {
            return;
        }
        if (argument instanceof ModelEvents.DealStarted) {
            ModelEvents.DealStarted event = (ModelEvents.DealStarted) argument;
            publish(new ViewEvent.DealStarted(event.snapshot()));
            return;
        }
        if (argument instanceof ModelEvents.TrickStarted) {
            ModelEvents.TrickStarted event = (ModelEvents.TrickStarted) argument;
            publish(new ViewEvent.TrickStarted(event.snapshot()));
            return;
        }
        if (argument instanceof ModelEvents.CardPlayed) {
            ModelEvents.CardPlayed event = (ModelEvents.CardPlayed) argument;
            publish(new ViewEvent.CardPlayed(event.playerId(), event.cardCode(), event.cardText(), event.snapshot()));
            return;
        }
        if (argument instanceof ModelEvents.TrickEnded) {
            ModelEvents.TrickEnded event = (ModelEvents.TrickEnded) argument;
            publish(new ViewEvent.TrickEnded(event.snapshot()));
            return;
        }
        if (argument instanceof ModelEvents.Sign) {
            ModelEvents.Sign event = (ModelEvents.Sign) argument;
            SignEvent sign = event.event();
            publish(new ViewEvent.SignMade(sign.getSender().getId(), 
                    sign.getSender().getUsername(), sign.getType(), event.snapshot()));
            return;
        }
        if (argument instanceof ModelEvents.DealEnded) {
            ModelEvents.DealEnded event = (ModelEvents.DealEnded) argument;
            gameManager.pauseGame();
            publish(new ViewEvent.DealEnded(event.snapshot()));
            return;
        }
        if (argument instanceof ModelEvents.ScoresUpdated) {
            ModelEvents.ScoresUpdated event = (ModelEvents.ScoresUpdated) argument;
            publish(new ViewEvent.ScoresUpdated(event.dealPoints(), event.cumulativeScores(), event.dealWinnerId(), event.lastDealSnapshot()));
            return;
        }
        if (argument instanceof ModelEvents.GameEnded) {
            ModelEvents.GameEnded event = (ModelEvents.GameEnded) argument;
            publish(new ViewEvent.GameEnded(event.finalScores(), event.winnerIds()));
        }
    }

    // ---------- Getters and Helpers ----------
    public int getPlayerCount(){ return gameManager.getPlayers().size(); }
    public Map<String,Integer> getLastDealPoints(){ return gameManager.getLastDealPoints(); }
    public String getPlayerName(String id) { 
        Player p = findPlayer(id); 
        if (p == null) return id; 
        return p.getUsername(); 
    }

    public List<String> getPlayerIds() {
        return gameManager.getPlayers().stream()
                          .map(Player::getId)
                          .toList();
    }
    public Map<String,String> getPlayerNames(){
        LinkedHashMap<String,String> map = new LinkedHashMap<>();
        for (Player p : gameManager.getPlayers()) 
            map.put(p.getId(), p.getUsername());
        return map;
    }

    /** Find a player by their ID */
    private Player findPlayer(String id) {
        Player player = gameManager.getPlayers().stream()
                          .filter(p -> p.getId().equals(id))
                          .findFirst()
                          .orElse(null);
        return player;
    }

    /** Publish a view event to observers */
    private void publish(ViewEvent event){
        setChanged();
        notifyObservers(event);
    }
}
