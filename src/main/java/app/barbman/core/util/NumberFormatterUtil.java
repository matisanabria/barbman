package app.barbman.core.util;

import javafx.scene.control.TextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * Utilidades para formateo y parseo de números con separadores de miles.
 * Usa '.' como separador de miles y ',' como decimal (configurable).
 * Ejemplos:
 *   Formateo: 30000 -> "30.000"
 *   Parseo: "30.000" -> 30000
 *
 * También permite aplicar formato automático a un TextField.
 */
public class NumberFormatterUtil {

    private static final Logger logger = LogManager.getLogger(NumberFormatterUtil.class);

    // Configuración del formateador
    private static final DecimalFormatSymbols symbols;
    private static final DecimalFormat formatter;

    static {
        symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');   // separador de miles
        //symbols.setDecimalSeparator(',');    si en el futuro se usan decimales
        formatter = new DecimalFormat("#,###", symbols);
        formatter.setGroupingUsed(true);
        formatter.setMaximumFractionDigits(0); // sin decimales
    }

    /**
     * Formatea un número double a cadena con separador de miles.
     * Ej: 30000.5 -> "30.001"
     */
    public static String format(double value) {
        return formatter.format(Math.round(value));
    }

    /**
     * Aplica formato automático a un TextField para que
     * siempre muestre separadores de miles al escribir.
     */
    public static void applyToTextField(TextField textField) {
        textField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.isBlank()) {
                return;
            }
            // Deja solo los dígitos
            String digits = newValue.replaceAll("[^\\d]", "");
            if (digits.isEmpty()) {
                textField.setText("");
                return;
            }
            try {
                long valor = Long.parseLong(digits);
                String formateado = format(valor);
                textField.setText(formateado);
                textField.positionCaret(formateado.length()); // cursor al final
            } catch (NumberFormatException e) {
                logger.warn("[NumberFormatterUtil] Valor no numérico: {}", newValue);
            }
        });
    }
}
