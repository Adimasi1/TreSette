package view.game;

import view.common.ImageResources;
import model.sign.SignType;
import javax.swing.*;
import java.awt.*;

/**
 * SignMenuPanel
 *
 * A compact wooden-styled popup used to send a sign action (e.g. Busso,
 * Volo, Liscio) from the human player. The panel uses absolute positioning
 * so it can be placed by its parent without affecting surrounding layout.
 */
public class SignMenuPanel extends JPanel {
    public interface Actions { void onSend(SignType type); void onClose(); }

    // ---- Local Constants ----
    private static final int PADDING = 10;
    private static final int BUTTON_W = 100;
    private static final int BUTTON_H = 30;
    private static final int BUTTON_GAP = 8;
    private static final int TOP_OFFSET = 18;
    private static final int CLOSE_SIZE = 20;
    private static final Font SIGN_FONT = new Font("Serif", Font.BOLD, 15);
    private static final Font CLOSE_FONT = new Font("SansSerif", Font.BOLD, 14);
    private static final Color CLOSE_COLOR = new Color(200,0,0);
    private static final SignType[] SIGN_ORDER = { SignType.BUSSO, SignType.VOLO, SignType.LISCIO };

    private final Actions actions;
    /** Constructor for the SignMenuPanel
     * @param actions the actions to perform on sign buttons
     */
    public SignMenuPanel(Actions actions) {
        this.actions = actions;
        setOpaque(false);
        setLayout(null); 
        buildSignButtons();
        buildCloseButton();
    }

    // ---- Building Components ----
    private void buildSignButtons() {
        int y = PADDING + TOP_OFFSET;
        for (int i = 0; i < SIGN_ORDER.length; i++) {
            final SignType type = SIGN_ORDER[i];
            JButton signButton = new JButton(capitalize(type.name()));
            styleGreen(signButton);
            signButton.setFont(SIGN_FONT);
            int x = PADDING + i * (BUTTON_W + BUTTON_GAP);
            signButton.setBounds(x, y, BUTTON_W, BUTTON_H);
            signButton.setFocusable(false);
            signButton.addActionListener(new java.awt.event.ActionListener(){
                @Override 
                public void actionPerformed(java.awt.event.ActionEvent event) {
                    if (actions != null) actions.onSend(type);
                }
            });
            add(signButton);
        }
    }

    private void buildCloseButton() {
        JButton closeButton = new JButton("X");
        closeButton.setFont(CLOSE_FONT);
        closeButton.setMargin(new Insets(0,0,0,0));
        closeButton.setContentAreaFilled(false);
        closeButton.setOpaque(false);
        closeButton.setBorder(null);
        closeButton.setForeground(CLOSE_COLOR);
        closeButton.setFocusPainted(false);
        int w = computeTotalWidth();
        closeButton.setBounds(w - CLOSE_SIZE - 2, 2, CLOSE_SIZE, CLOSE_SIZE);
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeButton.setToolTipText("Chiudi");
        closeButton.addActionListener(new java.awt.event.ActionListener(){
            @Override 
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (actions != null) actions.onClose();
            }
        });
        add(closeButton);
    }

    // ---- Compute Dimensions ----
    private int computeTotalWidth() {
        return PADDING * 2 + SIGN_ORDER.length * BUTTON_W + (SIGN_ORDER.length - 1) * BUTTON_GAP;
    }
    private int computeTotalHeight() {
        int y = PADDING + TOP_OFFSET;
        return y + BUTTON_H + PADDING - 2;
    }
    @Override 
    public Dimension 
    getPreferredSize() { return new Dimension(computeTotalWidth(), computeTotalHeight()); }

    /** Apply green button styling used across the game UI. */
    private static void styleGreen(JButton button){
        button.setBackground(new Color(34,139,34));
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK,2,true));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    /** Capitalize the given word (first letter upper-case, rest lower-case). */
    private static String capitalize(String text){ 
        text = text.toLowerCase(); 
        return Character.toUpperCase(text.charAt(0)) + text.substring(1); 
    }
    /**
     * Paints a rounded wooden background (fallback to a brown rectangle)
     * and a thin black border to match the game's theme.
     */
    @Override 
    protected void paintComponent(Graphics g){
        Graphics2D graphics = (Graphics2D) g.create();
        Image wood = ImageResources.load(ImageResources.WOOD);
        int width = getWidth();
        int height = getHeight();
        int cornerArc = 18; 
        if(wood != null){
            graphics.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, width, height, cornerArc, cornerArc));
            graphics.drawImage(wood, 0, 0, width, height, this);
        } else {
            graphics.setColor(new Color(133, 94, 66));
            graphics.fillRoundRect(0, 0, width, height, cornerArc, cornerArc);
        }
        graphics.setColor(Color.BLACK);
        graphics.drawRoundRect(0, 0, width - 1, height - 1, cornerArc, cornerArc);
        graphics.dispose();
        super.paintComponent(g);
    }
}
