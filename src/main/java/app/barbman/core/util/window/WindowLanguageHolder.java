package app.barbman.core.util.window;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;
import java.util.ResourceBundle;

public class WindowLanguageHolder {
    private static final Logger logger = LogManager.getLogger(WindowLanguageHolder.class);

    private static Locale currentLocale = new Locale("es", "ES");
    private static ResourceBundle bundle = loadBundle(currentLocale);

    private WindowLanguageHolder() {}

    // ============================================================
    // ======================= PUBLIC API =========================
    // ============================================================

    public static ResourceBundle getBundle() {
        return bundle;
    }

    public static Locale getLocale() {
        return currentLocale;
    }

    public static void setLocale(Locale locale) {
        if (locale == null || locale.equals(currentLocale)) return;

        currentLocale = locale;
        bundle = loadBundle(locale);

        logger.info("[LANG] Locale changed -> {}", locale);
    }

    // ============================================================
    // ======================= INTERNAL ===========================
    // ============================================================

    private static ResourceBundle loadBundle(Locale locale) {
        try {
            return ResourceBundle.getBundle(
                    "app.barbman.core.lang.lang",
                    locale
            );
        } catch (Exception e) {
            logger.error("[LANG] Failed to load bundle for locale {}", locale, e);
            return ResourceBundle.getBundle(
                    "app.barbman.core.lang.lang",
                    new Locale("es", "ES")
            );
        }
    }

}
