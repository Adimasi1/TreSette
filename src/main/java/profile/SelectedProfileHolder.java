package profile;

/**
 * Static holder for the currently selected Profile within the UI
 * It provides access to the current profile and allows to change it
 * with a setter.
 */
public final class SelectedProfileHolder {
    private static UserProfile current;

    private SelectedProfileHolder() {}

    public static UserProfile get() { return current; }
    public static void set(UserProfile player) { current = player; }
    public static boolean isSet() { return current != null; }
}
