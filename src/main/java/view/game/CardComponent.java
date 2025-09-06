package view.game;

import view.common.ImageResources;
import javax.swing.*;
import java.awt.*;

/** Simple graphic representation of a card (front) */
public class CardComponent extends JComponent {
    private final String code; // machine code, e.g. ASSO_SPADE
    private Image img;
    private boolean highlighted;

    /**
     * Creates a graphical view for a card. This is a pure rendering component.
     */
    public CardComponent(String code){
        this.code = code;
        setToolTipText(code);
        setPreferredSize(new Dimension(view.LayoutConstant.CARD_W, view.LayoutConstant.CARD_H));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loadImage();
    }

    /**
     * Sets the highlighted state of the card.
     */
    public void setHighlighted(boolean h){
        if (this.highlighted != h) {
            this.highlighted = h;
            repaint();
        }
    }
    /**
     * Paints the card, highlighted if requested.
     */
    @Override 
    protected void paintComponent(Graphics g){

        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth();
        int h = getHeight();
        // Glow and highlight background
        if (highlighted) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255, 255, 150, 140));
            g2.fillRoundRect(-2, -2, w + 4, h + 4, 16, 16);
        }

        if (img != null) {
            g2.drawImage(img, 0, 0, w, h, this);
        } else {
            g2.setColor(new Color(255, 255, 255, 200));
            g2.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);
            g2.setColor(Color.BLACK);
            g2.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            String shortTxt = code.replace("_", "\n");
            int y = 18;
            for (String line : shortTxt.split("\n")) {
                g2.drawString(line, 8, y);
                y += 14;
            }
        }
        if (highlighted) {
            g2.setColor(new Color(255, 215, 0, 200));
            Stroke old = g2.getStroke();
            g2.setStroke(new BasicStroke(3f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, 14, 14);
            g2.setStroke(old);
        }
        g2.dispose();
    }

    public String getCode(){ return code; }
    private void loadImage(){
    /**
     * Loads the image for the card matching the code.
     * Throws RuntimeException if the image is not found.
     */
        if (!code.contains("_")) return; // not right format
        String filename = code + ".png";
        String path = "images/cards/" + filename;
        Image candidate = ImageResources.load(path);
        if (candidate != null)
            img = candidate;
        else
            throw new RuntimeException("[CardView] Missing image for " + code + " tried=" + path);
    }

}
