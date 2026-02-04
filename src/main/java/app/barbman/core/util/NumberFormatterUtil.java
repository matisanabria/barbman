package app.barbman.core.util;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
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
    public static TextFormatter<String> applyToTextField(TextField field) {

        TextFormatter<String> formatter = new TextFormatter<>(change -> {

            try {
                String newText = change.getControlNewText();

                if (newText.isBlank()) {
                    return change;
                }

                String digits = newText.replaceAll("\\D", "");
                if (digits.isEmpty()) {
                    change.setText("");
                    change.setRange(0, change.getControlText().length());
                    return change;
                }

                long value = Long.parseLong(digits);
                String formatted = format(value);

                change.setText(formatted);
                change.setRange(0, change.getControlText().length());

                // Asegurar que el caret no quede fuera de rango
                int caretPos = Math.min(formatted.length(), formatted.length());
                change.setCaretPosition(caretPos);
                change.setAnchor(caretPos);

                return change;

            } catch (NumberFormatException e) {
                logger.debug("[NUMBER-FORMATTER] NumberFormatException caught, ignoring: {}", e.getMessage());
                return null;
            } catch (IllegalArgumentException e) {
                // Este es el error "The start must be <= the end"
                logger.warn("[NUMBER-FORMATTER] IllegalArgumentException caught (rapid delete?): {}", e.getMessage());
                // Simplemente rechazar el cambio y mantener el estado anterior
                return null;
            } catch (Exception e) {
                // Cualquier otro error inesperado
                logger.error("[NUMBER-FORMATTER] Unexpected error in text formatter: {}", e.getMessage());
                return null;
            }
        });

        field.setTextFormatter(formatter);
        return formatter;
    }


}