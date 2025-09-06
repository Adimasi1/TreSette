package profile;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Files repository for {@link UserProfile} objects.
 * Each profile is stored as a single binary file inside the configured
 * directory. This repository exposes operations to load, save and delete
 * profiles. Serialization and deserialization is handled using
 * {@code ObjectOutputStream} and {@code ObjectInputStream}.
 */
public final class ProfileRepository {
    private final Path dir;

    /**
     * Create a repository rooted at the given directory.
     * If the directory does not exist it will be created on first save.
     *
     * @param dir path of the directory that will contain profile files
     */
    public ProfileRepository(Path dir) { 
        this.dir = dir; 
    }

    /**
     * Load all profiles present in the repository directory.
     * Unreadable or invalid files are silently skipped to avoid the
     * application startup failure.
     *
     * @return an immutable list of loaded profiles
     */
    public List<UserProfile> loadAll() {
        List<UserProfile> result = new ArrayList<>();
        if (!Files.isDirectory(dir)) return result;
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, "*.dat")) {
            for (Path path : ds) {
                UserProfile up = load(path);
                if (up != null) result.add(up);
            }
        } catch (IOException exception) {
            // keep it simple: if we can't read the directory return what we got
        }
        return result;
    }

    /**
     * Attempt to deserialize a single profile file.
     *
     * @param file path of the file to read
     * @return the {@link UserProfile} or {@code null} if the file is
     *         unreadable or contains an invalid profile
     */
    private UserProfile load(Path file) {
        try (ObjectInputStream input = new ObjectInputStream(Files.newInputStream(file))) {
            return (UserProfile) input.readObject();
        } catch (IOException | ClassNotFoundException | ClassCastException ignored) {
            return null;
        }
    }

    /**
     * Save (or overwrite) the given profile to disk.
     * The directory is created if missing
     * @param profile profile to persist
     */
    public void save(UserProfile profile) {
        try {
            Files.createDirectories(dir);
        } catch (IOException ignored) {}
        Path file = dir.resolve(safeFileName(profile.getNickname())+".dat");
        try (ObjectOutputStream objectOutput = 
                new ObjectOutputStream(Files.newOutputStream(file))) {
                    objectOutput.writeObject(profile);
        } catch (IOException ex) {
            // directly failed the persistence operation
            // by throwing an unchecked exception
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * Delete the file that corresponds to the given nickname.
     * This method only removes the canonical file
     */
    public void delete(String nickname) {
        Path file = dir.resolve(safeFileName(nickname) + ".dat");
        try {
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
            // ignore failures for simplicity
        }
    }

    private String safeFileName(String nickname) {
        return nickname.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}
