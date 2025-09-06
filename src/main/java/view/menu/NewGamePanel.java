package view.menu;

import view.common.WMenuPanel;
import view.common.AvatarComponents;
import view.LayoutConstant;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

import utils.AudioManager;

/** Panel to configure and start a new game: difficulty, mode, target score */
public class NewGamePanel extends WMenuPanel {
    // this interface defines the actions that can be performed from this panel
    public interface Actions {
        void onBack();
        void onStart(String difficulty, int winningScore);
    }

    private Actions actions;
    private final NewGameState state = new NewGameState();

    private JLabel avatarLabel;
    private CardLayout cardLayout;
    private JPanel cardPanel;

    private JButton difficultyBtn;
    private JButton scoreBtn;
    private JButton startBtn;
    private JButton backBtn;

    public NewGamePanel() {
        buildNewGameMenu();
        updateMainButtonsText();
    }

    /** Sets the actions for the new game panel */
    public void setActions(Actions actions) { this.actions = actions; }

    /** Builds the new game menu */
    private void buildNewGameMenu() {
        add(Box.createVerticalStrut(40));
        addTitleSection("Tre Sette");
        add(Box.createVerticalStrut(10));
        addSubtitleSection("Nuova Partita");
        add(Box.createVerticalStrut(30));
        add(buildWoodBox());
        add(Box.createVerticalStrut(24));
        avatarLabel = AvatarComponents.createAvatar(null, LayoutConstant.AVATAR_SIZE_DEFAULT);
        add(avatarLabel);

        add(Box.createVerticalGlue());
    }


    /** Updates the avatar displayed at the top of this panel. */
    public void updateAvatar(String avatarPath) {
        if (avatarLabel == null) return;
        int targetSize;
        if (avatarPath == null || avatarPath.isBlank()) {
            targetSize = LayoutConstant.AVATAR_SIZE_DEFAULT;
        } else {
            targetSize = LayoutConstant.AVATAR_SIZE_PROFILE;
        }
        AvatarComponents.updateAvatar(avatarLabel, avatarPath, targetSize);
    }

    /** Builds the main new game menu */
    private JPanel buildMainNewGameMenu() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        difficultyBtn = stdButton("Difficoltà");
        scoreBtn = stdButton("Punteggio");
        startBtn = stdButton("Avvia Partita");
        backBtn = stdButton("Indietro");
        difficultyBtn.addActionListener(new java.awt.event.ActionListener() {
            @Override 
            public void actionPerformed(java.awt.event.ActionEvent event) {
                AudioManager.playClick();
                cardLayout.show(cardPanel, "diff");
            }
        });
        scoreBtn.addActionListener(new java.awt.event.ActionListener() {
            @Override 
            public void actionPerformed(java.awt.event.ActionEvent event) {
                AudioManager.playClick();
                cardLayout.show(cardPanel, "score");
            }
        });
        startBtn.addActionListener(new java.awt.event.ActionListener() {
            @Override 
            public void actionPerformed(java.awt.event.ActionEvent event) {
                AudioManager.playClick();
                if (actions != null) actions.onStart(state.getDifficulty(), state.getWinningScore());
            }
        });
        backBtn.addActionListener(new java.awt.event.ActionListener() {
            @Override 
            public void actionPerformed(java.awt.event.ActionEvent event) {
                AudioManager.playClick();
                if (actions != null) actions.onBack();
            }
        });
        panel.add(Box.createVerticalGlue());
        panel.add(difficultyBtn);
        panel.add(Box.createVerticalStrut(10));
        panel.add(scoreBtn);
        panel.add(Box.createVerticalStrut(10));
        panel.add(startBtn);
        panel.add(Box.createVerticalStrut(10));
        panel.add(backBtn);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JComponent buildWoodBox() {
        JPanel box = createWoodBox(new Dimension(400, 300));

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false);
        cardPanel.add(buildMainNewGameMenu(), "main");
        cardPanel.add(buildDifficultyMenu(), "diff");
        cardPanel.add(buildScoreMenu(), "score");
        box.add(cardPanel);
        return box;
    }
    /* Builds the difficulty submenu */
    private JPanel buildDifficultyMenu() {
        JPanel panel = submenuBase();
        JButton easy = stdButton("Facile");
        JButton medium = stdButton("Medio");
        JButton hard = stdButton("Difficile");
        JButton back = stdButton("Indietro");
        easy.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { selectDifficulty("EASY"); }
        });
        medium.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { selectDifficulty("MEDIUM"); }
        });
        hard.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { selectDifficulty("HARD"); }
        });
        back.addActionListener(commonBackListener());
        addSubmenuButtons(panel, easy, medium, hard, back);
        return panel;
    }

    /* Builds the score submenu */
    private JPanel buildScoreMenu() {
        JPanel panel = submenuBase();
        JButton s11 = stdButton("11 punti");
        JButton s21 = stdButton("21 punti");
        JButton s31 = stdButton("31 punti");
        JButton back = stdButton("Indietro");
        s11.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent event) { 
                selectScore(11); }
        });
        s21.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent event) { 
                selectScore(21); }
        });
        s31.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent event) { 
                selectScore(31); }
        });
        back.addActionListener(commonBackListener());
        addSubmenuButtons(panel, s11, s21, s31, back);
        return panel;
    }
    /* Creates the base panel for submenus */
    private JPanel submenuBase() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(Box.createVerticalGlue());
        return panel;
    }
    /* Adds buttons to the submenu panel */
    private void addSubmenuButtons(JPanel panel, JButton... buttons) {
        for (int i = 0; i < buttons.length; i++) {
            panel.add(buttons[i]);
            if (i < buttons.length - 1)
                panel.add(Box.createVerticalStrut(10));
        }
        panel.add(Box.createVerticalGlue());
    }

    /* Creates a standard button with shared menu styling */
    private JButton stdButton(String text) {
        JButton b = new JButton(text);
        styleMenuButton(b);
        return b;
    }
    /* Selects the game difficulty */
    private void selectDifficulty(String difficulty) {
        AudioManager.playClick();
        state.setDifficulty(difficulty);
        cardLayout.show(cardPanel, "main");
        updateMainButtonsText();
    }
    /* Selects the game score */
    private void selectScore(int score) {
        AudioManager.playClick();
        state.setWinningScore(score);
        cardLayout.show(cardPanel, "main");
        updateMainButtonsText();
    }
    
    private ActionListener commonBackListener() {
        return new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent event) {
                AudioManager.playClick();
                cardLayout.show(cardPanel, "main");
            }
        };
    }
    /* Updates the text of the main menu buttons */
    private void updateMainButtonsText() {
        difficultyBtn.setText("Difficoltà: " + state.difficultyLabel());
        scoreBtn.setText("Punteggio: " + state.getWinningScore());
    }
}
