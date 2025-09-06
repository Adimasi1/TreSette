package view.game;

import javax.swing.*;
import java.awt.*;

// keep only local constants here
import utils.AudioManager;

/**
 * Top bar of the game screen.
 * Left: score box. Right: event box. Center: Menu button horizontally centered.
 */
public class GameTopPanel extends JPanel {
    private static final int TOP_OFFSET = 6;
    private final JButton menuButton = new JButton("Menu");
    private final EventBoxPanel eventBox = new EventBoxPanel();
    // Simplified inline score component replacing ScorePanel
    private final JLabel scoreLabel = new JLabel();

    /** Constructor for the GameTopBar
     *  It takes a Runnable for menu actions.
     */
    public GameTopPanel(Runnable menuAction){
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(2,4,0,4));
        setLayout(null); // manual layout (absolute positioning in doLayout)
        styleMenuButton();
        if (menuAction != null) {
            menuButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    AudioManager.playClick();
                    menuAction.run();
                }
            });
        }
        add(eventBox);
        styleScoreLabel();
        add(scoreLabel);
        add(menuButton);
    }
    private void styleScoreLabel(){
        scoreLabel.setOpaque(false);
        scoreLabel.setFont(new Font("Serif", Font.BOLD, 18));
        scoreLabel.setForeground(new Color(25,25,25));
        scoreLabel.setText("<html>Punteggi:<br>Il Tuo Team: 0<br>Team Avversario: 0</html>");
        scoreLabel.setHorizontalAlignment(SwingConstants.LEFT);
        scoreLabel.setVerticalAlignment(SwingConstants.TOP);
        scoreLabel.setSize(220, 70); // spazio per tre linee
    }

    private void styleMenuButton(){
        menuButton.setOpaque(false);
        menuButton.setContentAreaFilled(false);
        menuButton.setBorder(BorderFactory.createLineBorder(Color.BLACK,2,true));
        menuButton.setForeground(Color.BLACK);
        menuButton.setFocusPainted(false);
        menuButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        menuButton.setFont(new Font("Serif", Font.BOLD, 20));
        menuButton.setPreferredSize(new Dimension(140,46));
    }

    @Override 
    public void doLayout(){
        Insets insets = getInsets(); // Insets represent the space between the component's border and its content
        int width = getWidth() - insets.left - insets.right;
        Dimension leftD = scoreLabel.getPreferredSize();
        Dimension rightD = eventBox.getPreferredSize();
        Dimension menuD = menuButton.getPreferredSize();

        // We want left (score) and right (event) boxes to share the same top margin for symmetry.
        int top = insets.top + TOP_OFFSET; // fixed offset from top edge
        eventBox.setBounds(insets.left + width - rightD.width, top, rightD.width, rightD.height);
        scoreLabel.setBounds(insets.left, top, leftD.width, leftD.height);

        // Center menu button vertically relative to the taller of the two side boxes, using their shared top.
        int sideMaxH = Math.max(leftD.height, rightD.height);
        int menuY = top + (sideMaxH - menuD.height)/2;
        int centerX = insets.left + (width - menuD.width)/2;
        menuButton.setBounds(centerX, menuY, menuD.width, menuD.height);
    }
    /** Pushes a new event message to the event box */
    public void pushEvent(String message) {
        eventBox.pushEvent(message);
        repaint();
    }

    /** Delegate to update scores shown in the left score panel */
    public void setScores(int human, int opponent) {
    scoreLabel.setText("<html>Punteggi:<br>Il Tuo Team: "+human+"<br>Team Avversario: "+opponent+"</html>");
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        Insets insets = getInsets();
    Dimension leftD = scoreLabel.getPreferredSize();
        Dimension rightD = eventBox.getPreferredSize();
        Dimension menuD = menuButton.getPreferredSize();
        int sideMaxH = Math.max(leftD.height, rightD.height);
        int h = TOP_OFFSET + sideMaxH; // account for vertical offset used in layout
        h = Math.max(h, menuD.height); // ensure menu fits if taller
        h += insets.top + insets.bottom; // add container insets
        return new Dimension(100, h);
    }
}
