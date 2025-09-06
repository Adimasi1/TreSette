package profile;

import java.io.Serializable;
import java.util.Objects;

/**
/**
 * Immutable user profile used by the application to store basic information
 * and game statistics.
 * The class is {@link java.io.Serializable} to allow file persistence
 * through the {@code ProfileRepository}. Instances are immutable: any
 * update to statistics produces a new instance of UserProfile via {@link #updateStats(int, int, int)}.
 */
public final class UserProfile implements Serializable {
     private static final long serialVersionUID = 1L;
     private final String nickname;    // acts as unique key now
     private final String avatarPath;  // optional
     private final int gamesPlayed;
     private final int gamesWon;
     private final int gameLost;
    /**
     * Create a new user profile.
     *
     * @param nickname unique identifier (not null)
     * @param avatarPath path to avatar image (may be null)
     * @param gamesPlayed number of games played
     * @param gamesWon number of games won
     */
    public UserProfile(String nickname, String avatarPath, int gamesPlayed, int gamesWon, int gameLost) {
        this.nickname = Objects.requireNonNull(nickname);
        this.avatarPath = avatarPath;
        this.gamesPlayed = gamesPlayed;
        this.gamesWon = gamesWon;
        this.gameLost = gameLost;
    }

    /**
     * Return a new {@code UserProfile} instance with updated statistics.
     * The method returns a new snapshot containing the provided values.
     *
     * @param gamesPlayed new games-played value
     * @param gamesWon new games-won value
     * @return new {@code UserProfile} instance with updated stats
     */
    public UserProfile updateStats(int gamesPlayed, int gamesWon, int gameLost) {
        return new UserProfile(nickname, avatarPath, gamesPlayed, gamesWon, gameLost);
    }

    // --------------- Getters ---------------
    public String getNickname() { return nickname; }
    public String getAvatarPath() { return avatarPath; }
    public int getGamesPlayed() { return gamesPlayed; }
    public int getGamesWon() { return gamesWon; }
    public int getGameLost() { return gameLost; }
    public double getWinRate() {
        if (gamesPlayed == 0) {
            return 0d;
        } else {
            double winRate = gamesWon / (double) gamesPlayed;
            return winRate;
        }
    }
}
