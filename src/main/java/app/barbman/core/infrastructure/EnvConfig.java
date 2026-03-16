package app.barbman.core.infrastructure;

import app.barbman.core.repositories.DbBootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class EnvConfig {

    private static final Logger logger = LogManager.getLogger(EnvConfig.class);
    private static final Properties props = new Properties();

    public static void init() {
        Path envPath = DbBootstrap.getAppFolder().toPath().resolve(".env");
        load(envPath);
    }

    private static void load(Path envFile) {
        if (!Files.exists(envFile)) {
            logger.warn("[ENV] .env file not found at {}", envFile.toAbsolutePath());
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(envFile)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                int eq = line.indexOf('=');
                if (eq <= 0) continue;

                String key = line.substring(0, eq).trim();
                String value = line.substring(eq + 1).trim();
                // Remove surrounding quotes if present
                if (value.length() >= 2
                        && ((value.startsWith("\"") && value.endsWith("\""))
                        || (value.startsWith("'") && value.endsWith("'")))) {
                    value = value.substring(1, value.length() - 1);
                }
                props.setProperty(key, value);
            }
            logger.info("[ENV] Loaded {} properties from .env", props.size());
        } catch (IOException e) {
            logger.error("[ENV] Failed to read .env: {}", e.getMessage());
        }
    }

    public static String get(String key) {
        return props.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    public static boolean isConfigured() {
        String url = get("ONBARBER_API_URL");
        String token = get("ONBARBER_API_TOKEN");
        return url != null && !url.isEmpty()
                && token != null && !token.isEmpty()
                && !"your-token-here".equals(token);
    }
}
