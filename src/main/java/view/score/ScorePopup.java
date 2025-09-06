package view.score;

import javax.swing.*;
import java.awt.*;
import view.common.ImageResources;
import java.util.Map;
import java.util.List;

/**
 * Popup used to present end-of-deal and end-of-game summaries.
 * This component is intentionally minimal: it lays out a small set of
 * JLabels and a single action button to confirm and proceed. The component
 * exposes two public methods showDeal(...) and showGame(...),
 * which feed the content and make the popup visible. The provided
 * {@code Runnable} callbacks are executed when the user confirms the popup.
 *
 */
final class ScorePopup extends JPanel {

    private static final int POPUP_WIDTH = 360;
    private static final int POPUP_HEIGHT = 200;
    private final JPanel contentPanel = new JPanel();
    private final JButton actionButton = new JButton();
    private Runnable actionCallback; // callback esterna

    public ScorePopup(){
        setOpaque(false);
        setLayout(null); // fill parent; content centrato manualmente
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        actionButton.addActionListener(new java.awt.event.ActionListener(){
            @Override public void actionPerformed(java.awt.event.ActionEvent event){ 
                runAction(); 
            }
        });
        actionButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(contentPanel);
        setVisible(false);
    }

    private void runAction(){
        if(actionCallback != null) {
            actionCallback.run();
        }
    }

    /**
     * Populate and display the popup for the end of a deal.
     *
     * @param dealPoints mapping of team identifier to points scored
     * @param winnerIds list with the identifiers of the winners
     * @param isTie true if the deal resulted in a tie
     * @param displayNamesMap mapping of identifiers to display names
     * @param onNext callback executed when the user confirms the popup ("Avanti")
     */
    public void showDeal(Map<String,Integer> dealPoints, List<String> winnerIds, boolean isTie,
                  Map<String,String> displayNamesMap, Runnable onNext){
        build("Fine Mano", dealPoints, winnerIds, isTie, displayNamesMap, "Avanti", onNext);
    }
    /**
     * Populate and display the popup for the end of the entire game.
     *
     * @param finalScores mapping of team id to cumulative score for the match
     * @param winnerIds list with the identifiers of the final winners
     * @param isTie true if the game ended in a tie
     * @param displayNamesMap mapping of identifiers to display names
     * @param onMenu callback executed when the user confirms the popup ("Menu Principale")
     */
    public void showGame(Map<String,Integer> finalScores, List<String> winnerIds, boolean isTie,
                  Map<String,String> displayNamesMap, Runnable onMenu){
        String title;
        if(isTie) title = "Pareggio!";
        else title = "Fine Partita";
        build(title, finalScores, winnerIds, isTie, displayNamesMap, "Menu Principale", onMenu);
    }

    private void build(String title, Map<String,Integer> scoreMap, List<String> winnerIds, boolean isTie,
                       Map<String,String> displayNamesMap, String buttonText, Runnable actionToExecute){
        contentPanel.removeAll();
        this.actionCallback = actionToExecute;
        // title
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(Color.BLACK);
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        // Scores
        if(scoreMap == null) scoreMap = Map.of();
        addScoreLine(rename("Team1", displayNamesMap), scoreMap.get("Team1"));
        addScoreLine(rename("Team2", displayNamesMap), scoreMap.get("Team2"));
  
        if(winnerIds != null && !winnerIds.isEmpty()){
            contentPanel.add(Box.createVerticalStrut(6));
            String winnerLabelText;
            if(isTie){
                winnerLabelText = "Vincitori: Pareggio";
            } else {
                String winnerName = rename(winnerIds.get(0), displayNamesMap);
                winnerLabelText = "Vince: " + winnerName;
            }
            JLabel winnerLabel = new JLabel(winnerLabelText);
            winnerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            winnerLabel.setForeground(Color.BLACK);
            contentPanel.add(winnerLabel);
        }
        contentPanel.add(Box.createVerticalStrut(12));
        actionButton.setText(buttonText);
        contentPanel.add(actionButton);
        contentPanel.add(Box.createVerticalStrut(6));
        revalidate();
        repaint();
        setVisibility(true);
    }

    private void addScoreLine(String label, Integer value){
        JLabel scoreLabel = new JLabel(label+": "+value+" punti");
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        scoreLabel.setForeground(Color.BLACK); 
        contentPanel.add(scoreLabel);
    }

    private String rename(String identifier, Map<String,String> displayNamesMap){
        if(identifier == null) return "";
        if("Team1".equalsIgnoreCase(identifier)) return "Il Tuo Team";
        if("Team2".equalsIgnoreCase(identifier)) return "Team Avversario";
        return identifier;
    }

    protected void setVisibility(boolean v){
        setVisible(v);
    }

    @Override 
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        if(!isVisible()) return;
        Graphics2D g2 = (Graphics2D) g.create();
        int x = (getWidth()-POPUP_WIDTH)/2;
        int y = (getHeight()-POPUP_HEIGHT)/2;
        Image wood = ImageResources.load(ImageResources.WOOD);
        if(wood!=null){
            g2.drawImage(wood, x, y, POPUP_WIDTH, POPUP_HEIGHT, this);
        } else {
            g2.setColor(new Color(139,69,19));
            g2.fillRect(x,y,POPUP_WIDTH,POPUP_HEIGHT);
        }
        g2.setColor(Color.BLACK);
        g2.drawRect(x,y,POPUP_WIDTH,POPUP_HEIGHT);
        contentPanel.setBounds(x, y, POPUP_WIDTH, POPUP_HEIGHT);
        g2.dispose();
    }
}
