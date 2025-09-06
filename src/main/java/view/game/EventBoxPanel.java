package view.game;

import javax.swing.*;
import java.awt.*;

/** Box for recent events (shows latest only) transparent */
public class EventBoxPanel extends JPanel {
    private final JTextArea area = new JTextArea();
    private String last;
    /** Constructs the event box panel */
    public EventBoxPanel(){
        setOpaque(false);
        setLayout(new BorderLayout());
        area.setEditable(false);
        area.setOpaque(false);
        area.setForeground(new Color(25,25,25));
        area.setFont(new Font("Serif", Font.BOLD, 20)); // match menu style
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(8,10,8,10));
        area.setFocusable(false);
        add(area, BorderLayout.CENTER);
        setPreferredSize(new Dimension(260,70));
    }
    /** Pushes a new event message to the panel */
    public void pushEvent(String text){
        if(text == null || text.isBlank()) return;
    if(text.equals(last)) return; // not likely, but avoid unnecessary updates
        last = text;
        area.setText(text);
    }

    @Override 
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
    }
}
