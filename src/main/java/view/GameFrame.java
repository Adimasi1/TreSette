package view;

import javax.swing.*;
import java.awt.*;

/**
 * Top-level application window used by the TreSette user
 * The frame is non-resizable and has a size of 1100x800
 */
public class GameFrame extends JFrame {
    /**
     * Create the main application frame and initialize default window
     * properties (title, size, close operation and layout)
     */
    public GameFrame() {
        super("TreSette - Gioco Tradizionale");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(1100, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    /**
     * Replace the current content with the provided component.
     * The component is used as the application's main screen. the frame
     * is revalidated and repainted after the swap.
     *
     * @param component the new main component; must be a {@link JComponent}
     */
    public void setScreen(java.awt.Component component) {
        // set content pane with the new component
        setContentPane((JComponent) component);
        revalidate();
        repaint();
    }
}
