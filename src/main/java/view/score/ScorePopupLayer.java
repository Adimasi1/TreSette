package view.score;

import controller.GameController;
import controller.ViewEvent.*;
import controller.ViewEvent;

import java.util.Map;
import java.util.List;

import javax.swing.*;

/**
 * Layered component that manages end-of-deal and end-of-game popups.
 * This component listens for view events (passed via {@link #onEvent(ViewEvent)})
 * and displays an appropriate popup summarizing the most recent deal results or
 * the final match outcome. It also exposes a small callback interface
 * ({@link TopBarUpdater}) used to update an external score display (for
 * example the top bar) when new cumulative scores arrive.
 *
 * The visual popup is produced by the {@link ScorePopup} instance
 * this layer is responsible for wiring it to controller actions and handling
 * layout within its parent container.
 */
public final class ScorePopupLayer extends JComponent {
    @FunctionalInterface
    public interface TopBarUpdater { void update(int left, int right); }
    private final GameController controller;
    private final ScorePopup popup = new ScorePopup();
    private Map<String,Integer> cumulativeScores = Map.of();
    private Map<String,Integer> lastDealPoints = Map.of();
    private List<String> lastDealWinnerIds = List.of();
    private boolean lastDealTie = false;
    private List<String> finalWinnerIds = List.of();
    private boolean finalTie = false;

    private boolean gameEnded = false;

    private final TopBarUpdater topBarUpdater;

    private Runnable dealAction;
    private Runnable gameAction;

    public ScorePopupLayer(GameController controller, Runnable backToMenu, TopBarUpdater topBarUpdater){
        this.controller = controller;
        this.topBarUpdater = topBarUpdater;
        setOpaque(false);
        setLayout(null);
        add(popup);

        this.dealAction = () -> {
            if(gameEnded) { 
                popup.setVisibility(false); return; 
            }
            popup.setVisibility(false);
            controller.confirmDealResults();
        };
        this.gameAction = () -> { 
            if(backToMenu != null) {
                backToMenu.run();
            }
        };
    }

    // --- Internal state update helpers ---
    private void updateFromScoresUpdated(ScoresUpdated scoreUpdated){
        // Event contract guarantees non-null maps. So, there is no need to check
        cumulativeScores = scoreUpdated.cumulativeScores();
        lastDealPoints = scoreUpdated.dealPoints();
        if(scoreUpdated.dealWinnerId() == null) lastDealWinnerIds = List.of();
        else lastDealWinnerIds = List.of(scoreUpdated.dealWinnerId());
        updateTopBar(cumulativeScores);
    }

    private void updateFromGameEnded(GameEnded gameEnded){
        // finalScores and winnerIds are not null when GameEnded is fired
        cumulativeScores = gameEnded.finalScores();
        finalWinnerIds = List.copyOf(gameEnded.winnerIds());
        finalTie = finalWinnerIds.size() > 1;
        updateTopBar(cumulativeScores);
    }

    /**
     * Consume a view event and update internal popup state accordingly.
     *
     * - {@code ScoresUpdated}: update cumulative and per-deal scores and show the deal popup
     * - {@code GameEnded}: update final scores and show the end-of-game popup
     * - {@code DealStarted}: hide any visible popup
     *
     * @param event the view event emitted by the controller
     */
    public void onEvent(ViewEvent event){
        if(event instanceof DealStarted) {
            popup.setVisibility(false);
        } else if(event instanceof ScoresUpdated) {
            ScoresUpdated scoresUpdated = (ScoresUpdated) event;
            updateFromScoresUpdated(scoresUpdated);
            if(!gameEnded) {
                showDealPopup();
            }
        } else if(event instanceof GameEnded) {
            GameEnded gameEnded = (GameEnded) event;
            updateFromGameEnded(gameEnded);
            this.gameEnded = true;
            popup.setVisibility(false);
            showGamePopup();
        }
    }

    /* Shows the deal end popup with latest scores. */
    private void showDealPopup(){
        popup.showDeal(lastDealPoints, lastDealWinnerIds, lastDealTie, controller.getPlayerNames(), dealAction);
        layoutChildren();
    }

    /* Shows the game over popup with final scores. */
    private void showGamePopup(){
        popup.showGame(cumulativeScores, finalWinnerIds, finalTie, controller.getPlayerNames(), gameAction);
        layoutChildren();
    }

    /* Lays out the popup components to fill the parent. */
    private void layoutChildren(){
        popup.setBounds(0,0, getWidth(), getHeight());
    }

    /* Updates the top bar with the latest scores. */
    private void updateTopBar(Map<String,Integer> scores){
        if(topBarUpdater == null || scores == null || scores.isEmpty()) return;
        int t1 = scores.getOrDefault("Team1", 0);
        int t2 = scores.getOrDefault("Team2", 0);
        topBarUpdater.update(t1, t2);
    }
}
