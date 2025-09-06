package view.common; // package for UI classes

import utils.AudioManager; // project audio manager
import javax.swing.*; // Swing UI toolkit
import java.awt.*; // AWT for graphics and events
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/** Startup splash: shows logo, waits, fades out, then calls callback */
public class SplashOverlay extends JComponent {
    private final Image logo; 
    private float alpha = 1f; // logo opacity
    private final Runnable onFinished; // action to run when splash finishes (provided by the main)
    private final Timer lifeTimer; // timer for the fixed display time
    /** the splash overlay */
    public SplashOverlay(Runnable onFinished) {
        this.onFinished = onFinished;
        setOpaque(false);
        // Load white logo, fallback to colored logo; fully materialized
        Image img = ImageResources.load(ImageResources.WHITE_LOGO);
        if (img == null) img = ImageResources.load(ImageResources.LOGO);
        this.logo = img;
        AudioManager.playIntro();
        // show splash for 3000 ms, then remove splash and call callback
        lifeTimer = new Timer(3000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (onFinished != null)
                    onFinished.run();
                Container parent = getParent();
                if (parent != null) {
                    parent.remove(SplashOverlay.this);
                    parent.revalidate();
                    parent.repaint();
                }
            }
        });
        lifeTimer.setRepeats(false);
        lifeTimer.start();
    }

    /**
     * This override is made to paint the logo in the center of the splash screen
     * with a black background.
     */
    @Override 
    protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create(); // create a copy of the graphics
        int w = getWidth(); 
        int h = getHeight(); 
        g2.setColor(Color.BLACK);
        g2.fillRect(0,0,w,h);
        if (logo != null) {
            int imgW = logo.getWidth(this);
            int imgH = logo.getHeight(this);
            if (imgW > 0 && imgH > 0) {
                // the logo was too big, so it reduces the size to 65%
                int lw = (int) ( imgW * 0.65 ); 
                int lh = (int) ( imgH * 0.65 ); 
                int x = (w-lw) / 2; // place it in the center
                int y = (h-lh) / 2;
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                g2.drawImage(logo, x, y, lw, lh, this); 
            }
        }
        g2.dispose(); 
    }
}