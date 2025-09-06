package view.menu;

import javax.swing.*;
import java.awt.*;
import view.common.AvatarComponents;
import view.LayoutConstant;
import view.common.WMenuPanel;
import utils.AudioManager;

/** Main menu panel decoupled from controller and model
 * This is the main menu showed to the user at the start of the application
 */
public class MainMenuPanel extends WMenuPanel {
    // this interface defines the actions available in the main menu
    public interface MainMenuActions {
        void onNewGame();
        void onProfiles();
        void onExit();
    }

    private final JButton newGameBtn = new JButton("Nuova Partita");
    private final JButton profilesBtn = new JButton("Gestione Profili");
    private final JButton exitBtn = new JButton("Esci");
    private JLabel avatarLabel; 
    private String currentAvatarPath;
    private MainMenuActions actions;
    
    public MainMenuPanel() {
        add(Box.createVerticalStrut(40)); // space above title
        addTitleSection();
        add(Box.createVerticalStrut(30));

        add(buildMenuBox());
        add(Box.createVerticalStrut(24));

        currentAvatarPath = null;
        avatarLabel = AvatarComponents.createAvatar(currentAvatarPath, LayoutConstant.AVATAR_SIZE_DEFAULT);
        add(avatarLabel);
        add(Box.createVerticalGlue());
    }

    // --- extracted constructor helpers ---
    private void addTitleSection() {
        super.addTitleSection("Tre Sette");
        add(Box.createVerticalStrut(10));
        super.addSubtitleSection("Gioco di carte tradizionale");
    }

    /** Builds the menu buttons box */
    private JComponent buildMenuBox() {
        JPanel menuBox = createWoodBox(new Dimension(400, 300));
        menuBox.setLayout(new BoxLayout(menuBox, BoxLayout.Y_AXIS));
        super.styleMenuButton(newGameBtn);
        super.styleMenuButton(profilesBtn);
        super.styleMenuButton(exitBtn);
        addMenuButtonListeners();

        menuBox.add(Box.createVerticalGlue());
        menuBox.add(newGameBtn);
        menuBox.add(Box.createVerticalStrut(10));
        menuBox.add(profilesBtn);
        menuBox.add(Box.createVerticalStrut(10));
        menuBox.add(exitBtn);
        menuBox.add(Box.createVerticalGlue());
        return menuBox;
    }

    /** Sets the actions for the main menu. */
    public void setActions(MainMenuActions actions) {
        this.actions = actions;
    }

    /** Updates the avatar displayed in the main menu
     *  If the avatar path is invalid, a default avatar will be shown.
     */
    public void updateAvatar(String avatarPath) {
        currentAvatarPath = avatarPath;
        AvatarComponents.updateAvatar(avatarLabel, currentAvatarPath, LayoutConstant.AVATAR_SIZE_PROFILE);
    }
    
    private void addMenuButtonListeners() {
        newGameBtn.addActionListener(new java.awt.event.ActionListener() {
            @Override 
            public void actionPerformed(java.awt.event.ActionEvent event) {
                AudioManager.playClick();
                if (actions != null) actions.onNewGame();
            }
        });
        profilesBtn.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent event) {
                AudioManager.playClick();
                if (actions != null) actions.onProfiles();
            }
        });
        exitBtn.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent event) {
                AudioManager.playClick();
                if (actions != null) actions.onExit();
            }
        });
    }
}
