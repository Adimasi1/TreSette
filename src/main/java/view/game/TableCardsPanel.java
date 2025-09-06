package view.game;

import controller.ViewEvent.*;
import controller.ViewEvent;
import model.events.DealSnapshot;

import javax.swing.*;
import java.awt.*;
import java.util.List;
 

/**
 * Central area that displays the cards currently played on the table
 * The component lays out up to four card components in a centered
 * horizontal flow.
 */
public class TableCardsPanel extends JPanel {
    private static final int TABLE_CARD_W = view.LayoutConstant.CARD_W;
    private static final int TABLE_CARD_H = view.LayoutConstant.CARD_H;
    private static final int TABLE_GAP = view.LayoutConstant.CARD_GAP; // wide spacing for readability
    private static final int TABLE_PADDING = view.LayoutConstant.CARD_PADDING;

    /**
     * Create the table cards panel and configure its layout and preferred size.
     */
    public TableCardsPanel(){
        setOpaque(false);
        setLayout(new FlowLayout(FlowLayout.CENTER, TABLE_GAP, 0));
        setBorder(null);
        int baseWidth = TABLE_CARD_W * 4 + TABLE_GAP * 3 + TABLE_PADDING * 2 + 80; 
        setPreferredSize(new Dimension(baseWidth, TABLE_CARD_H + TABLE_PADDING * 2));
        setMinimumSize(getPreferredSize());
    }

    /**
     * Handle view events relevant to the table state. The method filters
     * incoming view event instances and updates the displayed cards
     * when the game state changes.
     */
    public void onEvent(ViewEvent event){
        if(!( event instanceof DealStarted || 
              event instanceof TrickStarted || 
              event instanceof CardPlayed || 
              event instanceof TrickEnded || 
              event instanceof DealEnded)) 
              return;
        DealSnapshot snap = SnapshotUtil.extract(event);
        if(snap == null) return;
        updateFromSnapshot(snap);
    }

    private void updateFromSnapshot(DealSnapshot snap){
        List<String> table = snap.getTableCards();
        SwingUtilities.invokeLater(() -> {
            removeAll();
            for(String code : table){
                CardComponent cardComponent = new CardComponent(code);
                add(cardComponent);
            }
            // If deal ended (table expected empty) cards are already cleared.
            revalidate();
            repaint();
        });
    }

}
