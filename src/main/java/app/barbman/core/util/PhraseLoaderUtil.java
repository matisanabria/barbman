package app.barbman.core.util;

import app.barbman.core.repositories.DbBootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PhraseLoaderUtil {
    private static final Logger logger = LogManager.getLogger(PhraseLoaderUtil.class);

    private static final File PHRASES_FOLDER =
            new File(DbBootstrap.getAppFolder(), "phrases");

    private static final File PHRASES_FILE =
            new File(PHRASES_FOLDER, "login_phrases.txt");

    public static String getRandomLoginPhrase() {
        ensureFileExists();
        List<String> phrases = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(PHRASES_FILE))) {
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
            return "Ready to cut history.";
        }

        return phrases.get(new Random().nextInt(phrases.size()));
    }

    private static void ensureFileExists() {
        try {
            if (!PHRASES_FOLDER.exists()) {
                PHRASES_FOLDER.mkdirs();
            }

            if (!PHRASES_FILE.exists()) {
                Files.write(PHRASES_FILE.toPath(), getDefaultPhrases().getBytes());
                logger.info("[PHRASES] Created default login_phrases.txt at {}", PHRASES_FILE.getAbsolutePath());
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
