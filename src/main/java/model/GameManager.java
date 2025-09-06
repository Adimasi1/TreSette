package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import model.player.Player;
import model.player.Team;
import model.score.ScoreManager;
import model.cards.Card;
import model.sign.SignType;
import model.events.ModelEvents;
import model.deal.Deal;
import model.deal.Deal2v2;
import model.events.DealSnapshot;
import java.util.Observable;
import java.util.Observer;

/**
 * Coordinates the overall flow of a Tre Sette match.
 * 
 * The {@code GameManager} holds the ordered list of {@code players}, the
 * derived {@code teams}, a {@link ScoreManager} instance, the current
 * {@link Deal} and the {@code winningScore} target. It manages deal
 * creation, score updates and game over detection, and implements the
 * {@link java.util.Observable}/{@link java.util.Observer} to elaborate 
 * notifications from the deal and send the relevant events to the 
 * {@link controller.GameController}.
 */
@SuppressWarnings("deprecation")
public final class GameManager extends Observable implements Observer {

    private final List<Player> players;              
    private final List<Team> teams = new ArrayList<>();
    private final ScoreManager scoreManager;         
    private final int winningScore;                  
    // field for current deal
    private Deal currentDeal;                       
    private boolean gameOver = false;
    private boolean paused = false;                  
    private int dealCounter = 0;                    

    /** The GameManager constructor receives the ordered list of players and the winning score.
     *  It initializes the two instances of {@link Team} and the {@link ScoreManager}.
     *  The local convention is to use even-indexed players for team 1, and odd-indexed players for team 2.
     * @param players
     * @param winningScore
     */
    public GameManager(List<Player> players, int winningScore) {
        if (players == null || players.size() != 4)
            throw new IllegalArgumentException("Players must be exactly 4 for 2vs2 mode.");
        this.players = new ArrayList<>(players);
        this.winningScore = winningScore;
        // team1: 0, 2; team2: 1, 3
        Team t1 = new Team("Team1", List.of(players.get(0), players.get(2)));
        Team t2 = new Team("Team2", List.of(players.get(1), players.get(3)));
        teams.add(t1); 
        teams.add(t2);
        scoreManager = new ScoreManager(List.of(t1.getId(), t2.getId()), winningScore);
    }

    // ------------------ Game Flow ------------------
    /**
    * Starts the game lifecycle. Wrapper that delegate startNextDeal()
    * This method is intended to be called once to start the game.
    * It delegates to {@link #startNextDeal()} which performs the
    * actual deal creation. UI/controllers should call {@code startGame()} to
    * begin the match and may call {@link #startNextDeal()} subsequently to
    * request additional deals during the game flow.
     */
    public void startGame() { startNextDeal(); }

    /**
     * Starts a new deal if the game is not over.
     * This method is used to request the next deal (for example after
    * {@link controller.GameController#confirmDealResults()}). It will be ignored when
     * a deal is already active or when the game has finished.
     */
    public void startNextDeal() {
        if (gameOver) return;
        if (currentDeal != null && !currentDeal.isOver()) {
            return; // ignore request
        }
        currentDeal = new Deal2v2(dealCounter, players, teams);
        attachToDeal(currentDeal); // observe the new deal
        if (paused) currentDeal.setPaused(true); // if GameManager is paused, pause the deal
        currentDeal.start();
        dealCounter++;
    }

    /**
     * Pauses the current game: the active deal is suspended
     * Calling multiple times while already paused has no additional effect
     */
    public void pauseGame() { 
        paused = true; 
        if (currentDeal != null) currentDeal.setPaused(true); 
    }
    /**
     * Resumes the game if it was paused, reactivating the deal.
     */
    public void resumeGame() { 
        paused = false; 
        if (currentDeal != null) currentDeal.setPaused(false); 
    }

    /** Stop current game */
    public void stopGame() {
        paused = true;
        gameOver = true;
        currentDeal = null; // release reference to current deal
    }
    /**
     * This handler is called when the active deal notifies completion. It updates lastDealWinner,
     * performs scoring (team or individual), sends score updates, and checks victory conditions.
     * If no winner can be resolved from the snapshot the scoring phase is skipped.
     */
    private void handleDealEnded(DealSnapshot snapshot) {
        String lastTrickWinnerId = snapshot.getLastTrickWinnerId(); 

        // translate last trick winner playerId to teamId for bonus
        String teamWinnerId = null;
        if (lastTrickWinnerId != null) {
            for (Team t : teams) {
                if (t.getMembers().stream()
                        .anyMatch(p -> p.getId().equals(lastTrickWinnerId))) {
                    teamWinnerId = t.getId();
                    break;
                }
            }
        }
        // 1) update scores (calculate and save lastDealTeamPoints)
        scoreManager.updateTeamGameScores(teams, teamWinnerId);
        // 2) retrieve points for this calculated deal
        Map<String,Integer> dealPoints = scoreManager.getLastDealTeamPoints();
        // 3) notify score update event (deal + cumulative + deal winner)
        setChanged();
        notifyObservers(new ModelEvents.ScoresUpdated(dealPoints, scoreManager.getTeamGameScores(), teamWinnerId, snapshot));
        // 4) check for game over
        if (scoreManager.checkForGameWinner()) {
            List<String> winners = scoreManager.getFinalWinnerIds();
            setChanged();
            notifyObservers(new ModelEvents.GameEnded(scoreManager.getTeamGameScores(), winners));
            gameOver = true;
        }
    }

    // ------------------ Delegated methods towards current deal ------------------
    /**
    /**
     * Whether the given player can send a sign at this moment.
     * It's called by the GameController
     * @param player the player to check
     * @return {@code true} if a deal is active and it authorizes the sign
     */
    public boolean canPlayerMakeSign(Player player) {
        return (currentDeal != null && currentDeal.canPlayerMakeSign(player));
    }
    /**
     * Delegates a sign emission attempt to the current deal.
     * It's called by the game Controller.
     * @param player the player making the sign
     * @param type sign type desired
     */
    public void handlePlayerSign(Player player, SignType type) { 
        if (currentDeal != null) 
            currentDeal.handlePlayerSign(player, type); 
    }

    /**
     * Pass-through allowing tests/UI to request a human play.
     * @param player the player performing the play
     * @param card the card to play
     * @return {@code true} if the play was accepted by the current deal
     */
    public boolean playHumanCard(Player player, Card card) {
        return currentDeal != null && currentDeal.playHumanCard(player, card);
    }
    /**
     * True if no deal is active or the active deal has finished.
     * @return {@code true} when there is no active deal or it is over
     */
    public boolean isCurrentDealOver() { 
        return (currentDeal == null || currentDeal.isOver()); 
    }

    // ------------------ Getters ------------------
    public List<Team> getTeams() { return new ArrayList<>(teams); }
    public List<Player> getPlayers() { return new ArrayList<>(players); }
    public Map<String,Integer> getTotalScores() { return scoreManager.getAllScores(); }
    public Map<String,Integer> getLastDealPoints() { 
        return scoreManager.getLastDealTeamPoints();
    }
    public boolean isGameOver() { return gameOver; }
    public int getWinningScore() { return winningScore; }

    // ------------------ Observable helpers ------------------
    /**
     * Registers a model events observer. If a deal is already active, it is attached to it.
     */
    // ------------------ Osservazione della Deal ------------------
    private void attachToDeal(Deal deal) {
        if (deal != null) {
            deal.addObserver(this);
        }
    }

    /** Updates the game state based on the received from the observable, {@link Deal}.
     *  The method only processes events related to the current deal and processes
     *  {@link ModelEvents.DealEnded} within GameManager, while other events are
    *  forwarded to the {@link controller.GameController}.
     */
    @Override
    public void update(Observable observable, Object argument) {
        if (!(argument instanceof ModelEvents.Event)) {
            return;
        }
        if (argument instanceof ModelEvents.DealEnded) {
            ModelEvents.DealEnded dealEnded = (ModelEvents.DealEnded) argument;
            handleDealEnded(dealEnded.snapshot());
        }
        setChanged();
        notifyObservers(argument);
    }

}
