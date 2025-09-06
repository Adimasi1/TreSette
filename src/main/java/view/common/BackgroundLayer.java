package view.common;

import javax.swing.*;
import java.awt.*;
// BackgroundLayer doesn't use LayoutConstant now

/** This class represents a background layer with an image or optional dark overlay.
 *  The background is used to display a table image.
 */
public class BackgroundLayer extends JPanel {
    private float darkenAlpha = 0.32f; 
    private Image background;
    /** Default constructor 
     *  Initializes the background layer with the default table image
    */
    public BackgroundLayer() { 
        this(ImageResources.TABLE); 
    }
    /** Overloaded constructor to allow custom image paths */
    public BackgroundLayer(String path) {
        setLayout(new BorderLayout()); 
        setOpaque(false);
        load(path);
    }

    /** Paints the background image and applies a darkening overlay if needed */
    @Override 
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics); 
        if (background != null) {
            // create a copy of the Graphics object
            Graphics2D g2 = (Graphics2D) graphics.create();
            // draw the background image at its size
            g2.drawImage(background, 0, 0, this);
            // if a darkening factor is set, paint a translucent black rectangle over the image
        if (darkenAlpha > 0f) {
            // convert alpha to a value between 0 and 255
            int alpha = (int) (darkenAlpha * 255);
            g2.setColor(new Color(0, 0, 0, alpha));
            g2.fillRect(0, 0, getWidth(), getHeight()); // darker overlay
            }
            // dispose the copy to free resources and avoid side-effects
        g2.dispose();
        }
    }

    /** Adds a child component centered, keeping background behind. 
     *  It is used to display the main game interface
    */
    public void setCentral(Component c) {
        removeAll(); // remove previous childs from the container
        JPanel wrapper = new JPanel(new GridBagLayout()); // wrapper to center the component
        wrapper.setOpaque(false);
        wrapper.add(c);
        add(wrapper, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    /** Adds a child component */
    public void setFull(Component c) {
        removeAll();
        add(c, BorderLayout.CENTER);
        revalidate(); 
        repaint();
    }

    private void load(String path) {
        background = ImageResources.load(path);
    }
}
