package view.game;

import model.events.DealSnapshot;
import view.common.ImageResources;
import view.LayoutConstant;
import javax.swing.*;
import java.awt.*;

/**
 * Visual representation of the cards a player has won.
 * Draws a small stack of facedown card backs. Supports horizontal
 * rendering (rotated 90°) for top and bottom alignment. 
 */
class WonPileComponent extends JComponent {
    private static final int CARD_W = 72;
    private static final int CARD_H = 110;
    private static final int OFFSET = 3;
    private static final int MAX_VISIBLE = 5;
    private static final int PAD = 8;   
    private static final int EXTRA = 2; 
    private int wonCount = 0;
    private final String playerId;
    private final Image backImg;
    private boolean horizontal = false; 
    private static Image ROTATED_BACK; 

    /**
     * Create a WonPile for the provided player identifier.
     *
     */
    public WonPileComponent(String playerId){
        this.playerId = playerId;
        setOpaque(false);
        backImg = ImageResources.load(LayoutConstant.BACK_IMG);
        if(backImg!=null && ROTATED_BACK==null){
            ROTATED_BACK = createRotated(backImg);
        }
        setToolTipText("Carte vinte");
    }
    /**
     * Returns the player id associated with this pile.
     */
    public String getPlayerId(){ return playerId; }
    
    /**
     * Enable or disable horizontal (rotated) rendering.
     * When the orientation changes the component is revalidated and
     * repainted so layout and display update accordingly.
     */
    public void setHorizontal(boolean h){ 
        if(this.horizontal != h){ 
            this.horizontal = h; 
            revalidate(); 
            repaint(); 
        } 
    }

    /**
     * Compute preferred size based on visible stacks and orientation.
     */
    @Override 
    public Dimension getPreferredSize(){
        // Number of stacks we would draw (each stack represents ~2 won cards); don't force at least one.
        int stacks = Math.min(MAX_VISIBLE, (wonCount + 1) / 2);
        // Keep a stable minimum footprint so layout doesn't collapse when 0 (use 1 for sizing only).
        int displayForSize = Math.max(1, stacks);
        if(horizontal){
            return new Dimension(CARD_H + OFFSET * (displayForSize - 1) + PAD * 2 + EXTRA,
                                 CARD_W + PAD * 2 + EXTRA);
        } else {
            return new Dimension(CARD_W + OFFSET * (displayForSize - 1) + PAD * 2 + EXTRA,
                                 CARD_H + PAD * 2 + EXTRA);
        }
    }

    /**
     * Update won count from the given snapshot. 
     * If the count changed thecomponent is revalidated and repainted.
     */
    public void updateFromSnapshot(DealSnapshot snap){
        if(snap == null) return;
        Integer c = snap.getWonCards().get(playerId);
        if (c != null && c != wonCount) {
            wonCount = c;
            setToolTipText("Carte vinte: " + wonCount);
            revalidate();
            repaint();
        }
    }
    /**
     * Paint the stacks of facedown cards. Up 
     */
    @Override 
    protected void paintComponent(Graphics g){
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        int stacks = Math.min(MAX_VISIBLE, (wonCount + 1) / 2);
        if(stacks <= 0){
            // Nothing won yet: draw nothing (keeps area blank but reserved by preferredSize).
            g2.dispose();
            return;
        }
        for(int i=0;i<stacks;i++){
            int x = PAD + i*OFFSET;
            int y = PAD + i*OFFSET;
            if(backImg!=null){
                if(!horizontal){
                    g2.drawImage(backImg, x, y, CARD_W, CARD_H, this);
                } else {
                    // use cached rotated image
                    Image rot = ROTATED_BACK != null ? ROTATED_BACK : backImg;
                    g2.drawImage(rot, x, y, CARD_H, CARD_W, this);
                }
            } else {
                g2.setColor(new Color(40, 40, 40, 160));
                if (!horizontal) {
                    g2.fillRoundRect(x, y, CARD_W, CARD_H, 12, 12);
                    g2.setColor(Color.WHITE);
                    g2.drawRoundRect(x + 1, y + 1, CARD_W - 2, CARD_H - 2, 12, 12);
                } else {
                    g2.fillRoundRect(x, y, CARD_H, CARD_W, 12, 12);
                    g2.setColor(Color.WHITE);
                    g2.drawRoundRect(x + 1, y + 1, CARD_H - 2, CARD_W - 2, 12, 12);
                }
            }
        }
        g2.dispose();
    }

    /**
     * Create and return a buffered image containing the source rotated -90°.
     */
    private static Image createRotated(Image image){
        if (image == null) return null;
        final int width = CARD_W;
        final int height = CARD_H;
        java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(height, width,
                                                            java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bi.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.translate(height / 2.0, width / 2.0);
            g2.rotate(-Math.PI / 2);
            g2.translate(-width / 2.0, -height / 2.0);
            g2.drawImage(image, 0, 0, width, height, null);
            return bi;
        } finally {
            g2.dispose();
        }
    }

}