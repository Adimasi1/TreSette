package view.menu;

import view.common.WMenuPanel;
import javax.swing.*;
import java.awt.*;
import utils.AudioManager;

/**
 * Pause overlay: wooden styled box with 3 buttons.
 * Render the panel and call provided callbacks. 
 */
public class PauseOverlay extends WMenuPanel {
    private static final int PAUSE_BOX_WIDTH = 360;
    private static final int PAUSE_BOX_HEIGHT = 300;
    private static final int PAUSE_BUTTON_GAP = 16;
    public interface Actions { void onContinue(); 
                               void onMainMenu(); 
                               void onExit(); }
    /** Constructor that initializes the pause overlay with the given actions. */
    public PauseOverlay(Actions action){
        setOpaque(false);
        setLayout(new GridBagLayout());
        Dimension preferredSize = new Dimension(PAUSE_BOX_WIDTH, PAUSE_BOX_HEIGHT);
        JPanel woodBox = createWoodBox(preferredSize);

        woodBox.add(Box.createVerticalStrut(20));
        addButton(woodBox, "Continua", () -> {
            AudioManager.playClick();
            if(action != null) action.onContinue();
        });
        woodBox.add(Box.createVerticalStrut(PAUSE_BUTTON_GAP));
        addButton(woodBox, "Menu Principale", () -> {
            AudioManager.playClick();
            if(action != null) action.onMainMenu();
        });
        woodBox.add(Box.createVerticalStrut(PAUSE_BUTTON_GAP));
        addButton(woodBox, "Esci", () -> {
            AudioManager.playClick();
            if(action != null) action.onExit();
        });
        woodBox.add(Box.createVerticalGlue());

        GridBagConstraints gridConstraints = new GridBagConstraints();
        gridConstraints.gridx = 0;
        gridConstraints.gridy = 0;
        gridConstraints.anchor = GridBagConstraints.CENTER;
        add(woodBox, gridConstraints);
    }

    /** Adds a button to the specified box with the given text and action */
    private void addButton(JPanel box, String text, Runnable action){
        JButton button = new JButton(text);
        styleMenuButton(button);
        button.addActionListener(new java.awt.event.ActionListener(){
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e){
                action.run();
            }
        });
        box.add(button);
    }
}
