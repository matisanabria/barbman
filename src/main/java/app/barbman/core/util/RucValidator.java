package app.barbman.core.util;

public class RucValidator {

    /**
     * Validates a Paraguayan RUC.
     * @param ruc string like "1234567-8" or "12345678"
     */
    public static boolean isValidParaguayRuc(String ruc) {
        if (ruc == null || ruc.isBlank()) return false;

        // Remove spaces
        ruc = ruc.trim();

        // Remove optional hyphen
        String numeric = ruc.replace("-", "");

        // Must be all digits
        if (!numeric.matches("\\d+")) return false;

        if (numeric.length() < 2) return false;

        String base = numeric.substring(0, numeric.length() - 1);
        int dvGiven = Character.getNumericValue(numeric.charAt(numeric.length() - 1));

        int dvCalculated = calculateDV(base);
        return dvGiven == dvCalculated;
    }

    /**
     * Calculates Paraguayan RUC DV using modulo 11.
     */
    private static int calculateDV(String base) {
        int[] weights = {2, 3, 4, 5, 6, 7};
        int weightIndex = 0;
        int sum = 0;

        // process digits from right to left
        for (int i = base.length() - 1; i >= 0; i--) {
            int digit = base.charAt(i) - '0';
            sum += digit * weights[weightIndex];
            weightIndex = (weightIndex + 1) % weights.length;
        }

        int mod = sum % 11;
        int dv = 11 - mod;

        if (dv == 11) return 1;
        if (dv == 10) return 0;

        return dv;
    }
}