package view.profileUI;

import javax.swing.*;
import java.awt.*;

/**
 * This class provides shared fonts and small helpers used by the
 * profile components of the view (cards, panels, overlays). It keeps visual
 * style in one place to make the UI easier to maintain and to ensure
 * consistent appearance across the profile views.
 */
final class ProfileUIStyle {
    private ProfileUIStyle() {}

    // Fonts centralizzati
    public static final Font FONT_TITLE = new Font("Serif", Font.BOLD, 40);
    public static final Font FONT_SUBTITLE = new Font("Serif", Font.PLAIN, 26);
    public static final Font FONT_NAME = new Font("Serif", Font.BOLD, 20);
    public static final Font FONT_STAT = new Font("Serif", Font.PLAIN, 14);
    public static final Font FONT_BTN_LARGE = new Font("Serif", Font.BOLD, 16);
    public static final Font FONT_BTN_SMALL = new Font("Serif", Font.BOLD, 12);

    public static JButton createButton(String text, Color background, Dimension size, Font font, Color fg){
        JButton b = new JButton(text);
        if(background != null){
            b.setBackground(background);
        }
        b.setForeground(fg == null ? Color.WHITE : fg);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createLineBorder(Color.BLACK,2,true));
        if(size != null){
            b.setPreferredSize(size);
            b.setMaximumSize(size);
        }
        if(font != null){
            b.setFont(font);
        }
        return b;
    }

    public static JLabel createLabel(String text, Font font, Color fg){
        JLabel l = new JLabel(text);
        if(font != null) l.setFont(font);
        if(fg != null) l.setForeground(fg);
        return l;
    }
}
