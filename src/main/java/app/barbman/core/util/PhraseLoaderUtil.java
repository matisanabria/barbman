package app.barbman.core.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PhraseLoaderUtil {
    private static final Logger logger = LogManager.getLogger(PhraseLoaderUtil.class);
    private static final String FOLDER_PATH =
            System.getProperty("user.home") + File.separator + "Documents" + File.separator + "Barbman" + File.separator + "data";
    private static final String FILE_PATH = FOLDER_PATH + File.separator + "login_phrases.txt";

    /**
     * Returns a random phrase from the login phrases file.
     * If the file doesn't exist, it will be created with default phrases.
     */
    public static String getRandomLoginPhrase() {
        ensureFileExists();
        List<String> phrases = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#") && !line.startsWith("[")) {
                    phrases.add(line);
                }
            }
        } catch (IOException e) {
            logger.error("[PHRASES] Error reading login_phrases.txt: {}", e.getMessage());
        }

        if (phrases.isEmpty()) {
            logger.warn("[PHRASES] No phrases found, using fallback.");
            return "Listo para cortar historia.";
        }

        // Pick a random one
        Random random = new Random();
        return phrases.get(random.nextInt(phrases.size()));
    }

    /**
     * Ensures the phrases file exists, creates it if missing.
     */
    private static void ensureFileExists() {
        try {
            Path folder = Paths.get(FOLDER_PATH);
            if (!Files.exists(folder)) {
                Files.createDirectories(folder);
            }

            Path file = Paths.get(FILE_PATH);
            if (!Files.exists(file)) {
                Files.write(file, getDefaultPhrases().getBytes());
                logger.info("[PHRASES] Created default login_phrases.txt at {}", FILE_PATH);
            }
        } catch (IOException e) {
            logger.error("[PHRASES] Error creating default file: {}", e.getMessage());
        }
    }

    private static String getDefaultPhrases() {
        return """
                # ===== BARBMAN LOGIN PHRASES =====
                [INFO]
                Testing phrases only — production file replaces this on release build.
                """;
    }
}
