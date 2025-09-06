package view.common;

import javax.swing.*;
import java.awt.*;

/** Helpers to create and update avatar labels */
public final class AvatarComponents {
    private static volatile int avatarSize = 250;

    private AvatarComponents() { }

    /** Sets the shared avatar size used when a null size is provided. 
     */
    public static void setAvatarSize(int size) {
        if (size <= 0) {
            avatarSize = 250;
        } else {
            avatarSize = size;
        }
    }

    /**
     * Creates a centered JLabel for the avatar located at avatarPath.
     * If avatarPath is invalid or null, the LOGO is used.
     * If size is null, the shared avatarSize is used.
     * @param avatarPath path to the avatar image
     * @param size optional size for the avatar
     */
    public static JLabel createAvatar(String avatarPath, Integer size) {
        int finalSize;
        if (size == null || size <= 0) {
            finalSize = avatarSize;
        } else {
            finalSize = size;
        }
        Image image = loadFromPathOrDefault(avatarPath);
        return buildLabelFromImage(image, finalSize);
    }

    /**
     * Updates the given target label to display the avatar at avatarPath.
     * If avatarPath is invalid or null, the LOGO is used.
     * If size is null, the shared avatarSize is used.
     * @param target the JLabel to update
     * @param avatarPath path to the avatar image
     * @param size optional size for the avatar
     */
    public static void updateAvatar(JLabel target, String avatarPath, Integer size) {
        if (target == null) return;
        int finalSize;
        if (size == null || size <= 0) {
            finalSize = avatarSize;
        } else {
            finalSize = size;
        }
        Image image = loadFromPathOrDefault(avatarPath);
        Icon icon;
        if (image != null) {
            icon = new ImageIcon(image.getScaledInstance(finalSize, finalSize, Image.SCALE_SMOOTH));
        } else {
            icon = null;
        }
        target.setIcon(icon);
        if (icon == null) {
            target.setText("");
        } else {
            target.setText(null);
        }
        target.setAlignmentX(Component.CENTER_ALIGNMENT);
        Dimension square = new Dimension(finalSize, finalSize);
        target.setPreferredSize(square);
        target.setMinimumSize(square);
        target.setMaximumSize(square);
    }

    // --- private helpers ---
    private static Image buildScaled(Image source, int size) {
        if (source == null) return null;
        return source.getScaledInstance(size, size, Image.SCALE_SMOOTH);
    }

    private static JLabel buildLabelFromImage(Image image, int size) {
        JLabel label;
        if (image != null) {
            Image scaled = buildScaled(image, size);
            label = new JLabel(new ImageIcon(scaled));
        } else {
            label = new JLabel("");
        }
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
	    Dimension square = new Dimension(size, size);
	    label.setPreferredSize(square);
	    label.setMinimumSize(square);
	    label.setMaximumSize(square);
        return label;
    }

    private static Image loadFromPathOrDefault(String path) {
        Image img;
        if (path == null || path.isBlank()) {
            img = null;
        } else {
            img = ImageResources.load(path);
        }
        if (img == null) {
            img = ImageResources.load(ImageResources.LOGO);
        }
        return img;
    }
    // Getter for avatar size
    public static int getAvatarSize() { return avatarSize; }

}
