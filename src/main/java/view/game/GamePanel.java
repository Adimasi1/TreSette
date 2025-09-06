package view.game;

import controller.GameController;
import controller.ViewEvent.*;
import controller.ViewEvent;
import model.events.DealSnapshot;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import utils.AudioManager;
import view.menu.PauseOverlay;
import view.score.ScorePopupLayer;

/** GamePanel is the primary container for the GUI.
 *  It composes the top status bar, the central game board view, the human
 *  player's hand bar and overlay layers used for pause and score popups.
 *  It links the the {@link controller.GameController} to the other visual
 *  components. It forwards the events received to children classes.
 */
@SuppressWarnings("deprecation")
public class GamePanel extends JPanel implements Observer {
    private static final Integer OVERLAY_LAYER = 50;
    private static final Integer POPUP_LAYER = 60;
    private final GameController controller;
    private final Runnable backToMenu;
    private final GameTopPanel topBar;
    private final GameBoardView boardView;
    private final HumanHandPanel humanBar;
    private PauseOverlay pauseOverlay; 
    private final ScorePopupLayer scorePopupLayer; 

    /**
     * Create a GamePanel wired to a controller and player names.
     *
     * @param controller the {@link GameController} driving game logic and sending view events
     * @param playerNames a map of player id and names
     * @param backToMenu callback executed when the user chooses to return to the main menu
     */
    public GamePanel (GameController controller, Map<String,String> playerNames, Runnable backToMenu) {
        this.controller = controller;
        this.backToMenu = backToMenu;
        setOpaque(false);
        setLayout(new BorderLayout()); // BorderLayout offers flexibility in arranging components

        topBar = new GameTopPanel(() -> togglePauseMenu());
        add(topBar, BorderLayout.NORTH); 
        boardView = new GameBoardView(playerNames, message -> topBar.pushEvent(message));
        add(boardView, BorderLayout.CENTER); 
        humanBar = new HumanHandPanel(controller, message -> topBar.pushEvent(message));
        add(humanBar, BorderLayout.SOUTH);
        scorePopupLayer = new ScorePopupLayer(controller, backToMenu, (a,b)-> topBar.setScores(a,b));
        boardView.add(scorePopupLayer, POPUP_LAYER);
        scorePopupLayer.setBounds(0, 0, boardView.getWidth(), boardView.getHeight());

        controller.addObserver(this);

        boardView.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                scorePopupLayer.setBounds(0, 0, boardView.getWidth(), boardView.getHeight());
            }
        });
    }

    /* Toggles the pause menu overlay */
    private void togglePauseMenu() {
        if (pauseOverlay != null) {
            hideOverlay();
            controller.resume();
            return;
        }
        controller.pause();
        pauseOverlay = new PauseOverlay(new PauseOverlay.Actions() {
            @Override
            public void onContinue() {
                hideOverlay();
                controller.resume();
            }

            @Override
            public void onMainMenu() {
                controller.stopGame();
                hideOverlay();
                if (backToMenu != null) {
                    backToMenu.run();
                }
                controller.deleteObserver(GamePanel.this);       
            }

            @Override
            public void onExit() {
                System.exit(0);
            }
        });

        boardView.add(pauseOverlay, OVERLAY_LAYER);
        pauseOverlay.setBounds(0, 0, boardView.getWidth(), boardView.getHeight());
        boardView.revalidate();
        boardView.repaint();
    }

    /** Hides the pause menu overlay */
    private void hideOverlay() {
        boardView.remove(pauseOverlay);
        pauseOverlay = null;
        boardView.repaint();
    }

    /**
     * Observer method to be invoked by the {@code GameController}.
     * This method receives view events and dispatches them to child components
     * (board view, human hand and score layer). It also triggers short audio
     * effects for important events and requests snapshot extraction and UI 
     * updates when appropriate.
     *
     * @param observable the observable source (usually the controller)
     * @param arg the event object; expected to be a subtype of {@link controller.ViewEvent}
     */
    @Override
    public void update(Observable observable, Object arg){
        if(!(arg instanceof ViewEvent event)) return;
        if (event instanceof CardPlayed) {
            AudioManager.playPlayingCard();
        } else if (event instanceof SignMade sign) {
            // Play sign audio for bot signs (human sound is already played at send time)
            if (!"P1".equals(sign.playerId())) {
                switch (sign.type()) {
                    case BUSSO -> AudioManager.playKnock();
                    case VOLO -> AudioManager.playFlying();
                    default -> { /* LISCIO: no audio */ }
                }
            }
        } else if (event instanceof GameEnded gameEnded) {
            boolean humanWon = gameEnded.winnerIds().contains("P1") ||
                               gameEnded.winnerIds().contains("Team1");
            if (humanWon) AudioManager.playWinner();
            else AudioManager.playGameOver();
        }

        humanBar.onEvent(event);
        boardView.onEvent(event);

        DealSnapshot snap = boardView.extractSnapshot(event);
        if (snap != null) { // not all the events will produce a snapshot
            boardView.updateSnapshot(snap);
        }
        scorePopupLayer.onEvent(event);
    }

}
