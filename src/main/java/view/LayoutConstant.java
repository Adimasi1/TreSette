package view;

/**
 * Centralized layout and UI constants used across view classes.
 * Constants are grouped by the view class 
 */
public final class LayoutConstant {
    private LayoutConstant() {}
    public static final String BACK_IMG = "images/cards/dorso.png";

    public static final String[] AVATARS = {
        // Use classpath-relative paths consistent with ImageResources
        "images/avatar_0.png",
        "images/avatar_1.png",
        "images/avatar_2.png",
        "images/avatar_3.png",
        "images/avatar_4.png"
    };
    
    public static final int CARD_W = 72;
    public static final int CARD_H = 110;
    public static final int CARD_GAP = 24;
    public static final int CARD_PADDING = 10;

    public static final int AVATAR_SIZE_DEFAULT = 250;  // default logo and avatar size
    public static final int AVATAR_SIZE_PROFILE = 150;  // scaled size when a profile avatar is selected
}