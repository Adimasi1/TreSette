package main;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.function.Consumer;

import view.GameFrame;
import view.common.BackgroundLayer;
import view.common.SplashOverlay;
import view.menu.MainMenuPanel;
import view.menu.NewGamePanel;
import view.profileUI.*;
import view.game.GamePanel;
import utils.FantasyNameProvider;

import profile.*;
import controller.GameController;
import controller.ViewEvent.GameEnded;
import model.player.Player;
import model.player.HumanPlayer;
import model.GameDifficultyState;
import model.GameManager;
import model.player.BotPlayer;

/**
 * Entry point for the TreSette application.
 *
 * Responsibilities:
 * <ul>
 * <li> Bootstraps the Swing user interface.
 * <li> Initializes profile services and adapters.
 * <li> Links together model, controller, and view components.
 * <li> Manages navigation between main menu, new game, and profile management.
 * <li> Displays the initial splash screen.
 * <ul>
 */
public class JTresette {
    private JTresette() { }

    /**
     * Starts the TreSette application.
     *
     * This method bootstraps the Swing UI and links the main components.
     * The user interface is created on the Event Dispatch Thread using
     * {@link SwingUtilities#invokeLater(Runnable)}.
     *
     * @param args command line arguments (unused)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameFrame frame = new GameFrame();
            BackgroundLayer background = new BackgroundLayer();
            // var is equivalent here, it will be a Path anyway
            // Path.get put slashes and build the path with the given strings
            var profilesDirectory = Paths.get(System.getProperty("user.home"), ".tresette", "profiles");
            ProfileService profileService = new ProfileService(profilesDirectory);
            ProfilesAdapter profilesAdapter = new ProfilesAdapter(profileService);

            MainMenuPanel menu = buildMainMenu(background, profileService, profilesAdapter);
            background.setCentral(menu);
            frame.setScreen(background);
            frame.setVisible(true);
            showSplash(frame);
        });
    }
    /**
     * Builds the main menu panel and wires its actions to the application flow.
     *
     * @param background the background layer where panels are placed
     * @param profileService service for reading and updating user profiles
     * @param profilesAdapter adapter used by the profiles panel
     * @return the configured main menu panel
     */
    private static MainMenuPanel buildMainMenu(BackgroundLayer background,
                 ProfileService profileService, ProfilesAdapter profilesAdapter) {
        MainMenuPanel menu = new MainMenuPanel();
        menu.setActions(new MainMenuPanel.MainMenuActions() {
            @Override 
            public void onNewGame() { showNewGamePanel(background, menu, profileService); }
            @Override 
            public void onProfiles() { showProfilesPanel(background, menu, profilesAdapter); }
            @Override 
            public void onExit() { System.exit(0); }
        });
        return menu;
    }

    /**
     * Shows the new game configuration panel.
     *
     * The selected profile avatar (if any) is applied to the panel to keep a
     * consistent look with the main menu.
     *
     * @param background the background layer to render into
     * @param menu the main menu panel to return to when the user goes back
     * @param profileService service used to persist profile statistics after games
     */
    private static void showNewGamePanel(BackgroundLayer background, MainMenuPanel menu, ProfileService profileService) {
        NewGamePanel newGamePanel = new NewGamePanel();
        // Set the avatar
        if (SelectedProfileHolder.isSet()) {
            newGamePanel.updateAvatar(SelectedProfileHolder.get().getAvatarPath());
        } else {
            newGamePanel.updateAvatar(null);
        }
        newGamePanel.setActions(new NewGamePanel.Actions(){
            @Override 
            public void onBack() { 
                background.setCentral(menu); 
                background.repaint(); 
            }
            @Override 
            public void onStart(String difficulty, int winningScore) {
                List<Player> players = buildPlayers(difficulty);
                GameManager gameManager = new GameManager(players, winningScore);
                GameController gameController = new GameController(gameManager);
                LinkedHashMap<String,String> playerNamesById = new LinkedHashMap<>();
                for (Player p : players) playerNamesById.put(p.getId(), p.getUsername());
                Runnable backToMenu = () -> { background.setCentral(menu); 
                                              background.repaint(); };
                GamePanel gamePanel = new GamePanel(gameController, playerNamesById, backToMenu);
                background.setFull(gamePanel);
                linkProfileStatsUpdate(gameController, profileService);
                gameController.startGame();
            }
        });
        background.setCentral(newGamePanel);
    }

    /**
     * Shows the profiles management panel.
     *
     * When an avatar is chosen, the main menu avatar preview is updated
     *
     * @param background the background layer to render into
     * @param menu the main menu panel to return to
     * @param profilesAdapter adapter exposing profile operations to the UI
     */
    private static void showProfilesPanel(BackgroundLayer background, MainMenuPanel menu, 
                                            ProfilesAdapter profilesAdapter) {
        Runnable back = () -> { background.setCentral(menu); 
                                background.repaint(); };
        Consumer<UserProfile> onAvatarSelect = userProfile -> { // check the userProfile is still available
                                                if (userProfile != null) {
                                                    menu.updateAvatar(userProfile.getAvatarPath());
                                                    }
                                                };
        ProfilesPanel profilesPanel = new ProfilesPanel(profilesAdapter, back, onAvatarSelect);
        background.setCentral(profilesPanel);
    }

    /**
     * Creates the list of players for a match.
     *
     * The first player is the human, the others are bots whose names are
     * generated by {@link FantasyNameProvider}.
     *
     * @param difficulty textual difficulty code (EASY, MEDIUM, HARD)
     * @return list of four players in table order
     */
    private static List<Player> buildPlayers(String difficulty) {
        List<Player> players = new ArrayList<>();
        String humanName = "Tu";
        if(SelectedProfileHolder.isSet()) {
            humanName = SelectedProfileHolder.get().getNickname();
        } 
        FantasyNameProvider.reserve(humanName);
        players.add(new HumanPlayer("P1", humanName));
        GameDifficultyState diff = parseDifficulty(difficulty);
        players.add(new BotPlayer("P2", FantasyNameProvider.next(), diff));
        players.add(new BotPlayer("P3", FantasyNameProvider.next(), diff));
        players.add(new BotPlayer("P4", FantasyNameProvider.next(), diff));
        return players;
    }

    @SuppressWarnings("deprecation")
    /**
     * Connects the game controller to a profile statistics updater.
     *
     * When the game ends, the current profile is updated with a win or loss
     * depending on the outcome for the human player.
     *
     * @param gameController the game controller emitting view events
     * @param profileService service used to persist profile changes
     */
    private static void linkProfileStatsUpdate(GameController gameController, ProfileService profileService) {
        if(!SelectedProfileHolder.isSet()) return;
        UserProfile current = SelectedProfileHolder.get();
        gameController.addObserver(new java.util.Observer(){
            @Override 
            public void update(java.util.Observable o, Object arg){
                if(!(arg instanceof GameEnded gameEnded)) return;
                boolean humanWon = gameEnded.winnerIds().contains("P1") || 
                                   gameEnded.winnerIds().contains("Team1");
                profileService.recordGameResult(current.getNickname(), humanWon)
                               .ifPresent(SelectedProfileHolder::set);
            }
        });
    }

    /**
     * Shows a simple splash overlay that disables interactions for a short time.
     *
     * @param frame the application window
     */
    private static void showSplash(GameFrame frame) {
        JRootPane root = frame.getRootPane();
        JComponent glass = (JComponent) root.getGlassPane();
        glass.setLayout(new BorderLayout());
        Container content = frame.getContentPane();
        setEnabledComponents(content, false);
        SplashOverlay splash = new SplashOverlay(() -> {
            glass.setVisible(false);
            setEnabledComponents(content, true);
            content.repaint();
        });
        // additional listeners: only for safety, but not real necessary
        glass.addMouseListener(new java.awt.event.MouseAdapter(){});
        glass.addMouseMotionListener(new java.awt.event.MouseMotionAdapter(){});
        glass.addKeyListener(new java.awt.event.KeyAdapter(){});
        glass.removeAll();
        glass.add(splash, BorderLayout.CENTER);
        glass.setVisible(true);
    }

    /**
     * Enables or disables a component and all of its descendants.
     *
     * @param component the root component
     * @param enabled true to enable, false to disable
     */
    private static void setEnabledComponents(Component component, boolean enabled){
        component.setEnabled(enabled);
        if(component instanceof Container cont){
            for(Component child : cont.getComponents())
                setEnabledComponents(child, enabled);
        }
    }

    /**
     * Parses a textual difficulty code into a {@link GameDifficultyState}.
     * Accepts EASY, MEDIUM, HARD. Any other value defaults to EASY.
     *
     * @param difficulty textual difficulty code
     * @return the matching difficulty, or EASY if unknown or null
     */
    private static GameDifficultyState parseDifficulty(String difficulty){
        if (difficulty == null) return GameDifficultyState.EASY;
        if ("MEDIUM".equals(difficulty)) return GameDifficultyState.MEDIUM;
        if ("HARD".equals(difficulty)) return GameDifficultyState.HARD;
        return GameDifficultyState.EASY;
    }
}