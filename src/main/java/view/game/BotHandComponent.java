package view.game;

import controller.ViewEvent;
import model.events.DealSnapshot;
import view.common.ImageResources;

import javax.swing.JComponent;
import java.awt.*;
import static view.LayoutConstant.BACK_IMG;

/**
 * Shows hidden (back) cards for an opponent in one of three orientations.
 * Only displays count (no real ranks). Rotates image for LEFT/RIGHT columns.
 */
public class BotHandComponent extends JComponent {
    // Local bot-hand layout constants
    private static final int OVERLAP = 18;
    private static final int BOT_CARD_W = view.LayoutConstant.CARD_W;
    private static final int BOT_CARD_H = view.LayoutConstant.CARD_H;
    public enum Orientation { TOP, LEFT, RIGHT }
    private final Image backImage;

    private final String playerId;
    private final Orientation orientation;
    private int cardCount = 0;

    public BotHandComponent(String playerId, Orientation orientation){
        this.playerId = playerId;
        this.orientation = orientation;
        setOpaque(false);
        this.backImage = ImageResources.load(BACK_IMG);
    }

    /** Returns the id of the player this view represents. */
    public String getPlayerId() {
        return playerId;
    }

    /** Consumes a view event and updates the displayed card count if it changed. */
    public void onEvent(ViewEvent event){
        if(event instanceof ViewEvent.SignMade) {
            
        }
        DealSnapshot snap = SnapshotUtil.extract(event);
        if (snap == null) return;
        Integer size = snap.getHandSizes().get(playerId);
        if (size == null) return;
        if (cardCount != size) {
            cardCount = size;
            revalidate();
            repaint();
        }
    }

    @Override 
    /** Preferred size depends on orientation and current card count (with overlap). */
    public Dimension getPreferredSize(){
        int count = Math.max(cardCount, 1);
        if(orientation==Orientation.TOP){
            int width = BOT_CARD_W + (count-1)*OVERLAP + 20;
            return new Dimension(width, BOT_CARD_H + 20);
        } else {
            int height = BOT_CARD_W + (count-1)*OVERLAP + 20;
            return new Dimension(BOT_CARD_H + 20, height);
        }
    }

    @Override 
    /** Paints overlapped card backs in the proper orientation */
    protected void paintComponent(Graphics g){
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (cardCount <= 0) {
            g2.dispose();
            return;
        }
        if(orientation == Orientation.TOP){
            int startX = 10;
            int y = (getHeight()-BOT_CARD_H)/2;
            for(int i=0;i<cardCount;i++){
                drawBack(g2, startX + i*OVERLAP, y, false);
            }
        } else {
            int startY = 10;
            int x = (getWidth()-BOT_CARD_H)/2;
            for(int i=0;i<cardCount;i++){
                drawBack(g2, x, startY + i*OVERLAP, true);
            }
        }
        g2.dispose();
    }

    /* Draws a single card back with image or fallback. */
    private void drawBack(Graphics2D g2, int x, int y, boolean rotate){
    if(backImage==null){ // fallback rectangle
            g2.setColor(new Color(30,30,30,160));
            if(!rotate){
                g2.fillRoundRect(x,y,BOT_CARD_W,BOT_CARD_H,12,12);
                g2.setColor(Color.WHITE); g2.drawRoundRect(x+1,y+1,BOT_CARD_W-2,BOT_CARD_H-2,12,12);
            } else {
        // rotated fallback shape
                g2.fillRoundRect(x,y,BOT_CARD_H,BOT_CARD_W,12,12);
                g2.setColor(Color.WHITE); g2.drawRoundRect(x+1,y+1,BOT_CARD_H-2,BOT_CARD_W-2,12,12);
            }
            return;
        }
        if(!rotate){
            g2.drawImage(backImage, x, y, BOT_CARD_W, BOT_CARD_H, this);
        } else {
        // rotate around card center
            int cx = x + BOT_CARD_H/2; // width dopo rotazione
            int cy = y + BOT_CARD_W/2;
            g2.rotate(Math.toRadians(90), cx, cy);
        // after rotation draw original image centered
            int drawX = cx - BOT_CARD_W/2;
            int drawY = cy - BOT_CARD_H/2;
            g2.drawImage(backImage, drawX, drawY, BOT_CARD_W, BOT_CARD_H, this);
            g2.rotate(Math.toRadians(-90), cx, cy);
        }
    }
}
