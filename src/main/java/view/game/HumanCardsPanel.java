package view.game;

import controller.GameController;
import utils.AudioManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * This Panel is responsible for displaying and interacting with the human player's hand.
 */
public class HumanCardsPanel extends JPanel {
    // local layout constants
    private static final int HAND_CARD_W = view.LayoutConstant.CARD_W;
    private static final int HAND_CARD_H = view.LayoutConstant.CARD_H;
    private static final int H_MARGIN = 16;
    private static final int V_MARGIN = 12;
    private static final int HAND_GAP = 8;

    private final GameController controller;
    private List<String> lastHumanHand = List.of();
    private int dragOriginIndex = -1;
    private int currentDragTargetIndex = -1;
    private int dragStartScreenX = 0;
    private boolean dragMoved = false;
    private boolean playPending = false; // to avoid multiple plays
    /** Constructor of HumanCardsPanel,
     *  @param controller the game controller to interact with
     *  It's one of the few classes in the GUI to interact directluy
     *  with the controller.
     */
    public HumanCardsPanel(GameController controller) {
        this.controller = controller;
        setOpaque(false);
        setLayout(null);
    }
    /** Sets the hand of the player
     * @param hand the list of card codes representing the player's hand
     */
    public void setHand(List<String> hand){
        if(hand == null) this.lastHumanHand = List.of();
        else this.lastHumanHand = List.copyOf(hand);
    }

    /** Reset del flag di pending  when HumanHandPanel receive a new SnapShot*/
    public void clearPlayPending(){
        playPending = false;
    }
    /** Refreshes the hand display
     * Call this after setHand() to update the display.
     */
    public void refreshHand(){
        removeAll(); // remove all existing card views
        for (int i = 0; i < lastHumanHand.size(); i++) {
            add(HumanCardComponent(lastHumanHand.get(i), i)); // add each card to the Panel
        }
        layoutCards();
        revalidate();
        repaint();
    }
    /** Positions the card components within the panel */
    public void layoutCards(){
        int count = getComponentCount();
        if (count == 0) return;
        int cardW = HAND_CARD_W;
        int cardH = HAND_CARD_H;
        int gap = HAND_GAP;
        int totalW = count * cardW + Math.max(0, count - 1) * gap;
        int startX = (getWidth() - totalW) / 2;
        int y = Math.max(0, getHeight() - cardH - V_MARGIN);
        int x = startX;
        for (int i = 0; i < count; i++) {
            java.awt.Component component = getComponent(i);
            component.setBounds(x, y, cardW, cardH);
            x += cardW + gap;
        }
    }
    /** This class has a CardComponent for each human card and 
     *  handles its interaction with the mouse:
     *  <ul>
     *  <li> drag and drop for reordering cards
     *  <li> click to play a card (release without moving the mouse)
     *  <ul>
     */
    private CardComponent HumanCardComponent(String cardCode, int index){
        CardComponent cardView = new CardComponent(cardCode);

        cardView.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override 
            public void mousePressed(java.awt.event.MouseEvent event) {
                dragOriginIndex = index;
                currentDragTargetIndex = index;
                cardView.setHighlighted(true);
                dragStartScreenX = event.getXOnScreen();
                dragMoved = false;
            }

            @Override 
            public void mouseReleased(java.awt.event.MouseEvent event) {
                if (dragOriginIndex >= 0 && currentDragTargetIndex >= 0) {
                    int size = lastHumanHand.size();
                    int toSlot = Math.min(Math.max(currentDragTargetIndex,0), size);
                    int from = dragOriginIndex;
                    boolean noReorder = (toSlot == from || (toSlot == size && from == size -1));
                    if (!dragMoved || noReorder) { //no movement or same position 
                        if (event.getClickCount() == 1 && !playPending) {
                            boolean accepted = controller.playCard("P1", cardCode);
                            if (accepted) {
                                playPending = true; // blocca ulteriori play fino al prossimo snapshot
                            }
                        }
                    } else if (from >= 0 && from < size) {
                        lastHumanHand = controller.moveHumanCard(from, toSlot);
                        refreshHand();
                        AudioManager.playSwapping();
                    }
                }
                cardView.setHighlighted(false);
                dragOriginIndex = -1;
                currentDragTargetIndex = -1;
                dragMoved = false;
            }
        });

        cardView.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override public void mouseDragged(java.awt.event.MouseEvent event) {
                boolean moreThan4Pixels = Math.abs(event.getXOnScreen() - dragStartScreenX) > 4;
                if(!dragMoved && moreThan4Pixels) {
                    dragMoved = true; // if we moved more than 4 pixels, consider it a drag
                }
                updateDragTarget(event);
            }
        });

        return cardView;
    }

    private void updateDragTarget(MouseEvent event){
        if(dragOriginIndex < 0) return; // no drag
        Point p = SwingUtilities.convertPoint(event.getComponent(), event.getPoint(), this);
        int target = computeTargetIndex(p.x);
        if(target != currentDragTargetIndex && target >= 0){
            currentDragTargetIndex = target;
        }
    }

    private int computeTargetIndex(int mouseX){
        int count = getComponentCount();
        if(count == 0) return -1;
        for(int i = 0; i < count; i++){
            Rectangle bound = getComponent(i).getBounds();
            int mid = bound.x + bound.width / 2;
            if(mouseX < mid) return i;
        }
        return count;
    }

    @Override
    public void doLayout(){
        super.doLayout();
        layoutCards();
    }

    @Override
    public Dimension getPreferredSize(){
        int count = lastHumanHand == null ? 0 : lastHumanHand.size();
        int gapCount = Math.max(0, count - 1);
        int width = 2 * H_MARGIN + count * HAND_CARD_W + gapCount * HAND_GAP;
        int height = HAND_CARD_H + V_MARGIN * 2;
        return new Dimension(width, height);
    }
}
