package view.game;

import controller.GameController;
import controller.ViewEvent.*;
import controller.ViewEvent;
import model.events.DealSnapshot;
import model.sign.SignType;
import javax.swing.*;
import java.awt.*;

import java.util.function.Consumer;
import utils.AudioManager;

/**
 * Bottom bar that shows the human player's hand and (in multiplayer) the sign button.
 * Responsible only for local hand interaction (reorder + play) and opening the sign menu.
 */
public class HumanHandPanel extends JPanel {

    private final GameController controller;
    private final HumanCardsPanel cardsPanel;
    private final JLayeredPane layered = new JLayeredPane();
    private final JButton signButton = new JButton("Segni");
    private SignMenuPanel signMenu;

    // Local layout constants
    private static final int H_MARGIN = 16;
    private static final int V_MARGIN = 12;
    private static final int HAND_GAP = 8; // this gap remains different from CARD_GAP intentionally
    private static final int SIGN_BUTTON_WIDTH = 90;
    private static final int SIGN_BUTTON_HEIGHT = 36;
    private static final Color SIGN_ENABLED_COLOR = new Color(34, 139, 34);
    private static final Color SIGN_DISABLED_COLOR = new Color(150, 150, 150);
    private static final int DEFAULT_PREF_WIDTH = 800;

    private final Consumer<String> messageConsumer;
    private boolean canSignContext;

    public HumanHandPanel(GameController controller, Consumer<String> messageConsumer) {
        this.controller = controller;
        if (messageConsumer == null) {
            this.messageConsumer = new Consumer<String>() {
                @Override 
                public void accept(String s) { /* no-op */ }
            };
        } else this.messageConsumer = messageConsumer;

        setOpaque(false);
        setLayout(new BorderLayout());

        layered.setOpaque(false);
        add(layered, BorderLayout.CENTER);
        cardsPanel = new HumanCardsPanel(controller);
        layered.add(cardsPanel, JLayeredPane.DEFAULT_LAYER);

        // Sign menu button
        initSignButton();
        cardsPanel.refreshHand(); // initial empty layout
    }

    /** Configure and add the sign button to the layered pane. */
    private void initSignButton() {
        signButton.setFont(new Font("Serif", Font.BOLD, 16));
        signButton.setBackground(SIGN_ENABLED_COLOR);
        signButton.setForeground(Color.BLACK);
        signButton.setOpaque(true);
        signButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2, true));
        signButton.setFocusPainted(false);
        signButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signButton.setToolTipText("Apri il menu per inviare un segno");
        signButton.addActionListener(new java.awt.event.ActionListener(){
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e){ 
                toggleSignMenu(); 
            }
        });
        layered.add(signButton, JLayeredPane.PALETTE_LAYER);
    }

    /** Methods to position UI elements */
    private void positionSignButton(){ 
        int bw = SIGN_BUTTON_WIDTH;
        int bh = SIGN_BUTTON_HEIGHT; 
        signButton.setBounds(10, layered.getHeight()-bh-10, bw, bh);
    }
    
    private void positionSignMenu(){ 
        if(signMenu == null) return; 
        int height = signMenu.getPreferredSize().height; 
        signMenu.setBounds(10, layered.getHeight()-height-10, signMenu.getPreferredSize().width, height); 
    }

    @Override 
    public void doLayout(){ 
        super.doLayout(); // it places all components 
        cardsPanel.setBounds(0, 0, layered.getWidth(), layered.getHeight()); // the cards panel occupies the full layered area
        cardsPanel.layoutCards();
        positionSignButton(); 
        positionSignMenu(); 
    }

    @Override 
    public Dimension getPreferredSize(){
        int count = cardsPanel.getComponentCount();
        int gapCount = Math.max(0, count - 1);
        int width = 2 * H_MARGIN + count * view.LayoutConstant.CARD_W + gapCount * HAND_GAP;
        int height = view.LayoutConstant.CARD_H + V_MARGIN * 2;
        return new Dimension(Math.max(DEFAULT_PREF_WIDTH, width), height);
    }

    /** Methods to handle game events */
    public void onEvent(ViewEvent event){
        if(!(event instanceof DealStarted ||
             event instanceof CardPlayed || 
             event instanceof TrickStarted || 
             event instanceof DealEnded || 
             event instanceof TrickEnded)) 
             return;

        DealSnapshot snap = SnapshotUtil.extract(event);
        if(snap != null){
            cardsPanel.setHand(snap.getHumanHand());
            cardsPanel.refreshHand();
            updateSignAvailability(snap);
            cardsPanel.clearPlayPending();
        }
    }

    private void updateSignAvailability(DealSnapshot snap){
    // Modalità unica 2vs2: pulsante segni sempre disponibile (visibilità gestita da condizioni successive)
    signButton.setVisible(true);
        // Conditions: human player's turn (P1) and the model rules allow signing.
        boolean isHumanTurn = "P1".equals(snap.getCurrentPlayerId()); 
        boolean rulesAllow = snap.canCurrentPlayerSign();
        boolean canSign = isHumanTurn && rulesAllow; 
        canSignContext = canSign;
        // Keep the button enabled so text color stays consistent
        if (canSign) {
            signButton.setBackground(SIGN_ENABLED_COLOR);
            signButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            signButton.setBackground(SIGN_DISABLED_COLOR);
        }
    }

    /** Toggle the sign menu visibility */
    private void toggleSignMenu(){
        if(!canSignContext){
            messageConsumer.accept("Non è il momento per un segno"); 
            return; 
        }
        if(signMenu != null){ 
            closeSignMenu(); 
            return; 
        }
        showSignMenu();
    }

    // Create and show the sign menu
    private void showSignMenu() {
        signMenu = createSignMenu();
        layered.add(signMenu, JLayeredPane.MODAL_LAYER);
        positionSignMenu();
        layered.revalidate(); 
        layered.repaint();
    }

    // Factory for the SignMenuPanel with binding to handlers
    private SignMenuPanel createSignMenu() {
        return new SignMenuPanel(new SignMenuPanel.Actions() {
            @Override 
            public void onSend(SignType type) { handleSignSend(type); }
            @Override 
            public void onClose() { toggleSignMenu(); }
        });
    }

    // Handle sending a sign: play sound, send to model, update UI/state
    private void handleSignSend(SignType type) {
        switch (type) {
            case BUSSO -> AudioManager.playKnock();
            case VOLO -> AudioManager.playFlying();
            case LISCIO -> { /* no special sound */ }
            default -> { }
        } 
        controller.makeSign("P1", type); 
        if (signMenu != null) { closeSignMenu(); }
        canSignContext = false;
        // keep button enabled so text is visible, but show disabled appearance
        signButton.setBackground(SIGN_DISABLED_COLOR);
        signButton.setForeground(Color.BLACK);
        signButton.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        layered.revalidate(); 
        layered.repaint();
    }

    private void closeSignMenu(){
        if(signMenu != null){
            layered.remove(signMenu);
            signMenu = null;
            layered.repaint();
        }
    }
}
