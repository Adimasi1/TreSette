package view.common;

import javax.imageio.ImageIO;
import java.awt.*;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Central image loader with small cache and path normalization */
public final class ImageResources {
    private static final Map<String, Image> imagesCache = new ConcurrentHashMap<>();
    // images's paths
    public static final String TABLE = "images/tavolo.jpg";
    public static final String WOOD = "images/legno.png";
    public static final String LOGO = "images/Tre_Sette.png";
    public static final String WHITE_LOGO = "images/Tre_Sette_white.png";
    /** private constructor to prevent instantiation */
    private ImageResources() {}
    /** Loads an image from the resources 
     * @param path path to the image resource
    */
    public static Image load(String path) {
        if (path == null || path.isBlank()) return null;
        return imagesCache.computeIfAbsent(path, ImageResources::loadImageFromPath);
    }

    private static Image loadImageFromPath(String path) {
        // Normalize path for classpath lookup
        String normPath = path.trim().replace('\\', '/');
        // drop any leading slashes
        while (normPath.startsWith("/")) normPath = normPath.substring(1);
        // accept prefix like "resources/images/..." 
        if (normPath.startsWith("resources/")) {
            normPath = normPath.substring("resources/".length()); // cut from resources/ index
        }

        // Try classpath: first normalized path like images/...
        Image img = loadFromClasspath(normPath);
        if (img != null) return img;

        System.err.println("Resource not found on classpath: original='" + path + "' normalized='" + normPath + "'");
        return null;
    }

    private static Image loadFromClasspath(String path) {
        try {
            // get the classloader of ImageResources, to load from classpath
            URL url = ImageResources.class.getClassLoader().getResource(path);
            if (url == null) return null;
            return ImageIO.read(url); // decode the image in Image
        } catch (Exception e) {
            return null;
        }
    }
}