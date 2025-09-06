package view.game;

import controller.ViewEvent;
import model.events.DealSnapshot;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * GameBoardView is the central visual representation of a match.
 * It arranges table cards, opponent hand boxes, name tags and won piles
 * using a layered layout. 
 * The view also formats short textual messages for the top bar through a
 * {@code Consumer<String>}.
 */
class GameBoardView extends JLayeredPane {
    // Local layout constants
    private static final int TABLE_Y_OFFSET = 90;              // push table cards a bit lower vertically
    private static final int SIDE_Y_OFFSET = 40;               // offset to lower side players and their piles
    private static final int PLAYER_MARGIN = 20;               // margin from frame edges for player boxes
    private static final int HUMAN_WON_PILE_BOTTOM_MARGIN = 12;// distance from bottom for human won pile
    private static final int SIDE_WON_PILE_GAP = 6;            // gap between side player box and its won pile
    private static final int TOP_WON_PILE_GAP = 5;             // vertical gap between top player box and its won pile
    private static final int NAME_LABEL_EXTRA_GAP = 4;         // extra vertical gap under top player name label
    private static final int NAME_LABEL_VERTICAL_GAP = -2;     // gap above won pile for name labels
    private static final Integer DEFAULT_PLAYER_LAYER = JLayeredPane.DEFAULT_LAYER;
    private static final Integer WON_PILE_LAYER = 2;
    private static final Integer NAME_LABEL_LAYER = Integer.valueOf(10);
    private static final Integer DEAL_INDEX_LAYER = Integer.valueOf(20);

    private final List<String> playerIds; 
    private final Map<String, String> playerNames; // <player id, player name>
    private final Map<String, WonPileComponent> wonPiles = new HashMap<>();
    private final Map<String, JLabel> nameLabels = new HashMap<>();
    private TableCardsPanel tableBox;
    private BotHandComponent leftBox;
    private BotHandComponent rightBox;
    private BotHandComponent topBox;
    private final Consumer<String> eventMessageConsumer; // pushes text to top bar
    private final EventMessageFormatter messageFormatter; // message creation
    private final JLabel dealIndexLabel = new JLabel();

    /**
     * Construct a GameBoardView for the provided players.
     *
     * @param playerNames ordered mapping (player id : display name) used to layout seats
     * @param eventMessageConsumer consumer that receives short event messages for the top bar
     */
    GameBoardView(Map<String, String> playerNames, Consumer<String> eventMessageConsumer){
        this.playerIds = List.copyOf(playerNames.keySet()); // the ordered is guaranteed by the LinkedHashMap
        this.playerNames = Map.copyOf(playerNames);
        this.eventMessageConsumer = eventMessageConsumer; 
        this.messageFormatter = new EventMessageFormatter(this.playerIds, this.playerNames);
        setOpaque(false);
        // initiate components
        tableBox = new TableCardsPanel();
        add(tableBox, DEFAULT_PLAYER_LAYER);
        
        setupPlayerBoxes();
        setupWonPiles();
        setupNameLabels();
        setupDealIndexLabel();
    }

    /**
     * Override of Swing doLayout: once the container has a non‑zero size we
     * position all child components. 
     */
    @Override
    public void doLayout() {
        super.doLayout();
        relayout();
    }

    /* ---------------- setup sub components ---------------- */
    private void setupPlayerBoxes(){
        List<String> botPlayers = playerIds.stream()
                                    .filter(id -> !id.equals("P1"))
                                    .toList();
        // 2v2 seating (counterclockwise)
        if (botPlayers.size() >= 1) {
            rightBox = new BotHandComponent(botPlayers.get(0), BotHandComponent.Orientation.RIGHT);
            add(rightBox, DEFAULT_PLAYER_LAYER);
        }
        if (botPlayers.size() >= 2) {
            topBox = new BotHandComponent(botPlayers.get(1), BotHandComponent.Orientation.TOP);
            add(topBox, DEFAULT_PLAYER_LAYER);
        }
        if (botPlayers.size() >= 3) {
            leftBox = new BotHandComponent(botPlayers.get(2), BotHandComponent.Orientation.LEFT);
            add(leftBox, DEFAULT_PLAYER_LAYER);
        }
    }

    private void setupWonPiles(){
        for(String playerId : playerIds){
            WonPileComponent wonPileComponent = new WonPileComponent(playerId);
            if(playerId.equals("P1"))
                wonPileComponent.setHorizontal(true);
            wonPiles.put(playerId, wonPileComponent);
            add(wonPileComponent, WON_PILE_LAYER);
        }
    }
    private void setupNameLabels(){
        for(String playerId : playerIds){
            String name = playerNames.getOrDefault(playerId, playerId); // Get player name or use playerId
            JLabel label = new NameTag(name);
            nameLabels.put(playerId, label);
            add(label, NAME_LABEL_LAYER);
        }
    }
    // deck removed

    private void setupDealIndexLabel(){
        dealIndexLabel.setOpaque(false);
        dealIndexLabel.setForeground(new Color(0, 0, 0, 255));
        dealIndexLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        dealIndexLabel.setText("");
        add(dealIndexLabel, DEAL_INDEX_LAYER); 
    }

    /** Public Methods to interact with the game board
     * Forward a view event to child components and emit a short
     * user message to the event message consumer.
     *
     * @param event the view event to handle (may be null)
     */
    public void onEvent(ViewEvent event){
        if(tableBox != null) tableBox.onEvent(event);
        if(leftBox != null) leftBox.onEvent(event);
        if(rightBox != null) rightBox.onEvent(event);
        if(topBox != null) topBox.onEvent(event);
        String message = messageFormatter.format(event);
        if(message != null) eventMessageConsumer.accept(message);
    }
    
    /**
     * Update the entire visual state from a {@link DealSnapshot}.
     * Child components (won piles, table contents) are refreshed and a layout
     * pass is scheduled.
     *
     * @param snap the snapshot describing the current deal; if null the method is a no-op
     */
    public void updateSnapshot(DealSnapshot snap){
        if(snap == null) return;
        for(WonPileComponent pile : wonPiles.values())
            pile.updateFromSnapshot(snap);
        dealIndexLabel.setText("Mano n° " + (snap.getDealIndex() + 1));
        revalidate();
        repaint();
    }
    /** Relayout and positioning methods */
    /* Relayout all components */
    private void relayout(){
        positionTableBox();
        positionPlayerBoxes();
        positionWonPiles();
        positionNameLabels();
        positionDealIndexLabel();
        repaint();
    }

    private void positionTableBox(){  
        if(tableBox==null) return;
        Dimension tableSize = tableBox.getPreferredSize();
        int x = (getWidth() - tableSize.width) / 2;
        int y = (getHeight() - tableSize.height) / 2 + TABLE_Y_OFFSET;
        tableBox.setBounds(Math.max(0, x), Math.max(0, y), tableSize.width, tableSize.height);
    }
    
    private void positionPlayerBoxes(){
        int totalWidth = getWidth(); 
        int totalHeight = getHeight();
        if(leftBox != null) {
            Dimension leftSize = leftBox.getPreferredSize();
            // (margin from left, center vertically, preferred size)
            leftBox.setBounds(PLAYER_MARGIN, (totalHeight - leftSize.height) / 2 + 
                              SIDE_Y_OFFSET, leftSize.width, leftSize.height);
        }
        if(rightBox != null) {
            Dimension rightSize = rightBox.getPreferredSize();
            // (margin from right, center vertically, preferred size)
            rightBox.setBounds(totalWidth - rightSize.width - PLAYER_MARGIN, 
                              (totalHeight - rightSize.height) / 2 + SIDE_Y_OFFSET, 
                              rightSize.width, rightSize.height);
        }
        if(topBox != null) {
            Dimension topSize = topBox.getPreferredSize();
            // (center horizontally, margin from top, preferred size)
            topBox.setBounds((totalWidth - topSize.width) / 2, PLAYER_MARGIN,
                             topSize.width, topSize.height);
        }
    }

    private void positionWonPiles(){
        int width = getWidth();
        int height = getHeight();
        WonPileComponent human = wonPiles.get("P1");
        // P1 (human) should be always present (if not, something went wrong)
        human.setHorizontal(true);
        Dimension humanSize = human.getPreferredSize();
        int x = (width - humanSize.width) / 2;
        int y = height - humanSize.height - HUMAN_WON_PILE_BOTTOM_MARGIN;
        human.setBounds(x, y, humanSize.width, humanSize.height);

        if (topBox != null) {
            WonPileComponent wonPile = wonPiles.get(topBox.getPlayerId());
            if (wonPile != null) {
                wonPile.setHorizontal(true);
                Dimension topPileSize = wonPile.getPreferredSize();
                Rectangle topBoxBounds = topBox.getBounds();
                int topX = topBoxBounds.x + (topBoxBounds.width - topPileSize.width) / 2;
                int labelH = 0;
                JLabel label = nameLabels.get(topBox.getPlayerId());
                if (label != null)
                    labelH = label.getPreferredSize().height + NAME_LABEL_EXTRA_GAP;
                int topY = topBoxBounds.y + topBoxBounds.height + TOP_WON_PILE_GAP + labelH;
                wonPile.setBounds(topX, topY, topPileSize.width, topPileSize.height);
            }
        }

        if (leftBox != null) {
            WonPileComponent leftWonPile = wonPiles.get(leftBox.getPlayerId());
            if (leftWonPile != null) {
                leftWonPile.setHorizontal(false);
                Dimension leftPileSize = leftWonPile.getPreferredSize();
                Rectangle leftBoxBounds = leftBox.getBounds();
                int leftX = leftBoxBounds.x + leftBoxBounds.width + SIDE_WON_PILE_GAP;
                int leftY = leftBoxBounds.y + (leftBoxBounds.height - leftPileSize.height) / 2; 
                leftWonPile.setBounds(leftX, leftY, leftPileSize.width, leftPileSize.height);
            }
        }

        if (rightBox != null) {
            WonPileComponent rightWonPile = wonPiles.get(rightBox.getPlayerId());
            if (rightWonPile != null) {
                rightWonPile.setHorizontal(false);
                Dimension rightPileSize = rightWonPile.getPreferredSize();
                Rectangle rightBoxBounds = rightBox.getBounds();
                int rightX = rightBoxBounds.x - rightPileSize.width - SIDE_WON_PILE_GAP;
                int rightY = rightBoxBounds.y + (rightBoxBounds.height - rightPileSize.height) / 2;
                rightWonPile.setBounds(rightX, rightY, rightPileSize.width, rightPileSize.height);
            }
        }
    }

    private void positionNameLabels(){
        // For each label place it above its won pile
        for (Map.Entry<String, JLabel> entry : nameLabels.entrySet()){
            JLabel label = entry.getValue();
            WonPileComponent wp = wonPiles.get(entry.getKey());
            if (label == null || wp == null) continue;
            Dimension labelSize = label.getPreferredSize();
            Rectangle wonPileRectangle = wp.getBounds();
            int x = wonPileRectangle.x + (wonPileRectangle.width - labelSize.width) / 2; // center horizontally
            int y = wonPileRectangle.y - labelSize.height - NAME_LABEL_VERTICAL_GAP; // place above the won pile
            if (y < 0) y = 0;
            label.setBounds(x, y, labelSize.width, labelSize.height);
        }
    }

    private void positionDealIndexLabel(){
        String txt = dealIndexLabel.getText();
        if (txt == null) txt = "";
        Dimension indexSize = dealIndexLabel.getPreferredSize();
        int marginX = 8;
        int marginY = 0; 
        int x = getWidth() - indexSize.width - marginX;
        int y = getHeight() - indexSize.height - marginY; // anchor bottom-right, no gap
        dealIndexLabel.setBounds(x, y, indexSize.width, indexSize.height);
    }


    /* Utilities */
    /**
     * Attempt to extract a {@link DealSnapshot} from a view event.
     * This helper forwards to {@code SnapshotUtil} and returns null when
     * the event does not contain a snapshot.
     *
     * @param event the incoming view event
     * @return a DealSnapshot instance when available, otherwise null
     */
    public DealSnapshot extractSnapshot(ViewEvent event) { return SnapshotUtil.extract(event); }

    /** Class to represent a small label to display players names */
    private static class NameTag extends JLabel {
        NameTag(String text) {
            super(text);
            setFont(new Font("SansSerif", Font.BOLD, 14));
            setForeground(Color.WHITE);
            setOpaque(false);
        }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        g2.setColor(new Color(0, 0, 0, 140)); // semi-transparent black for the rounded rectangle
        g2.fillRoundRect(0, 0, w, h, 14, 14); // round rectangle
        g2.setColor(new Color(255, 255, 255, 160)); // white for border
        g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14); // round rectangle border
        FontMetrics fontMetrics = g2.getFontMetrics();
        int textX = (w - fontMetrics.stringWidth(getText())) / 2;
        int textY = (h - fontMetrics.getHeight()) / 2 + fontMetrics.getAscent();
        g2.setColor(getForeground());
        g2.drawString(getText(), textX, textY);
        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize() { 
        FontMetrics fontMetrics = getFontMetrics(getFont());
        Dimension dim = new Dimension(fontMetrics.stringWidth(getText()) + 16, fontMetrics.getHeight() + 6); // considering extra space (16 and 6)
        return dim;
        }
    }
}
