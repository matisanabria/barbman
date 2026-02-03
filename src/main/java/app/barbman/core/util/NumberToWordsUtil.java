package app.barbman.core.util;

import com.ibm.icu.text.RuleBasedNumberFormat;
import java.util.Locale;

public class NumberToWordsUtil {

    private static final RuleBasedNumberFormat FORMATTER =
            new RuleBasedNumberFormat(
                    new Locale("es", "PY"),
                    RuleBasedNumberFormat.SPELLOUT
            );

    public static String convert(double amount) {
        long intAmount = (long) amount;

        if (intAmount == 0) {
            return "CERO";
        }

        String words = FORMATTER.format(intAmount);
        return words.toUpperCase();
    }

    private NumberToWordsUtil() {
        // Utility class
    }
}