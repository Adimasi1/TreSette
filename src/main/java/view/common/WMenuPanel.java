package view.common;

import javax.swing.*;
import java.awt.*;

/**
 * Shared base panel for menu-like screens.
 * Provides common helpers for title and subtitle, wood-styled boxes, and button styling.
 */
public class WMenuPanel extends JPanel {
    public WMenuPanel() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    /** Adds a big centered title label to this panel
     * @param titleText the text to display as the title
    */
    public void addTitleSection(String titleText) {
        JLabel title = new JLabel(titleText);
        title.setFont(new Font("Serif", Font.BOLD, 46));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(Color.BLACK);
        add(title);
    }

    /** Adds a centered subtitle label to this panel
     * @param subtitleText the text to display as the subtitle
    */
    public void addSubtitleSection(String subtitleText) {
        JLabel subtitle = new JLabel(subtitleText);
        subtitle.setFont(new Font("Serif", Font.PLAIN, 26));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setForeground(Color.BLACK);
        add(subtitle);
    }

    /** Creates a wood-styled container with vertical BoxLayout for menu content
     * @param preferredSize the preferred size of the box
    */
    public JPanel createWoodBox(Dimension preferredSize) {
        JPanel box = new JPanel() {
            private final Image woodImage = ImageResources.load(ImageResources.WOOD);
            @Override 
            protected void paintComponent(Graphics graphics) {
                if (woodImage != null) {
                    graphics.drawImage(woodImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    graphics.setColor(new Color(133, 94, 66));
                    graphics.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                }
                super.paintComponent(graphics);
            }
        };
        box.setOpaque(false);
        if (preferredSize != null) {
            box.setPreferredSize(preferredSize);
            box.setMaximumSize(preferredSize);
        }
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        return box;
    }

    /** Applies standard styling to a menu button. 
     * @param button the JButton to style
    */
    public void styleMenuButton(JButton button) {
        Dimension buttonSize = new Dimension(260, 54);
        button.setBackground(new Color(34, 139, 34));
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 4, true));
        button.setFocusPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setPreferredSize(buttonSize);
        button.setMaximumSize(buttonSize);
        button.setMinimumSize(buttonSize);
        button.setFont(new Font("Serif", Font.BOLD, 22));
    }
}
