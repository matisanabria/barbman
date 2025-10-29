package app.barbman.core.util;

public class TextFormatterUtil {
    /**
     * Capitalizes the first letter of the given text.
     * If the text is null or blank, returns an empty string.
     *
     * @param text The input text to capitalize.
     * @return The text with the first letter capitalized.
     */
    public static String capitalizeFirstLetter(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        text = text.trim();
        if (text.length() == 1) {
            return text.toUpperCase();
        }

        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}
