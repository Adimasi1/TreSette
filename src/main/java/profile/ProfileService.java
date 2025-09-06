package profile;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

/**
 * Service that manages user profiles in memory and save themm using {@link ProfileRepository}.
 * The service keeps a small index keyed by nickname and delegates persistence operations 
 * to the repository.
 */
public final class ProfileService {
    private final ProfileRepository repository;
    private final Map<String, UserProfile> profilesByNickname  = new HashMap<>();

    public ProfileService(Path dir) {
        this.repository = new ProfileRepository(dir);
        repository.loadAll().forEach(profile -> profilesByNickname.put(profile.getNickname(), profile));
    }

    /**
     * @return mutable list of profiles ordered by nickname
     */
    public List<UserProfile> list() {
        List<UserProfile> list = new ArrayList<>(profilesByNickname.values());
        list.sort(Comparator.comparing(UserProfile::getNickname));
        return list;
    }
    /**
     * Create a new user profile with empty statistics.
     * If a profile with the same nickname already exists it will be
     * overwritten it in the memory.
     *
     * @param nickname unique nickname for the profile
     * @param avatarPath optional avatar path (may be null)
     * @return the created {@link UserProfile} instance
     */
    public UserProfile create(String nickname, String avatarPath) {
        
        if (list().stream().anyMatch(p -> p.getNickname().equals(nickname))) {
                            return null;
                        }
        UserProfile profile = new UserProfile(nickname, avatarPath, 0, 0, 0);
        profilesByNickname.put(nickname, profile);
        repository.save(profile);
        return profile;
    }
    /**
     * Record the result of a played game for the profile identified by
     * {@code nickname}. The method updates games played and won counters,
     * saves the new profile.
     * @param nickname profile identifier
     * @param won true if the player won the game
     * @return Optional containing the updated profile, or empty if the
     * profile was not found
     */
    public Optional<UserProfile> recordGameResult(String nickname, boolean won) {
        UserProfile existing = profilesByNickname.get(nickname);
        if (existing == null) return Optional.empty();
        int newPlayed = existing.getGamesPlayed() + 1;
        int newWon = existing.getGamesWon();
        int newLost = existing.getGameLost();
        if(won) newWon += 1;
        else newLost += 1;
        UserProfile updated = existing.updateStats(newPlayed, newWon, newLost); // new istance of the player profile
        profilesByNickname.put(nickname, updated);
        repository.save(updated);
        return Optional.of(updated);
    }

    /**
     * Delete the profile identified by {@code nickname} from 
     * from memory and storage.
     *
     * @param nickname profile identifier to remove
     */
    public void delete(String nickname) {
        profilesByNickname.remove(nickname);
        repository.delete(nickname);
    }
}
