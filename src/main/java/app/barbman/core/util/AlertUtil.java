package app.barbman.core.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;

import java.util.Optional;

/**
 * Utility class for showing different types of alerts with consistent styling.
 */
public class AlertUtil {

    private static final String ALERT_CSS = "/app/barbman/core/style/alerts.css";

    // ============================================================
    // ERROR
    // ============================================================

    /**
     * Shows an error alert with given title and message.
     */
    public static void showError(String title, String message) {
        show(title, message, Alert.AlertType.ERROR);
    }

    // ============================================================
    // INFO
    // ============================================================

    /**
     * Shows an information alert with given title and message.
     */
    public static void showInfo(String title, String message) {
        show(title, message, Alert.AlertType.INFORMATION);
    }

    // ============================================================
    // WARNING
    // ============================================================

    /**
     * Shows a warning alert with given title and message.
     */
    public static void showWarning(String title, String message) {
        show(title, message, Alert.AlertType.WARNING);
    }

    // ============================================================
    // CONFIRMATION
    // ============================================================

    /**
     * Shows a confirmation dialog with OK and Cancel buttons.
     * Returns true if user clicks OK, false otherwise.
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        applyStyles(alert);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Shows a confirmation dialog with custom button texts.
     * Returns true if user clicks the confirm button, false otherwise.
     */
    public static boolean showConfirmation(String title, String message, String confirmText, String cancelText) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Custom buttons
        ButtonType confirmButton = new ButtonType(confirmText);
        ButtonType cancelButton = new ButtonType(cancelText);
        alert.getButtonTypes().setAll(confirmButton, cancelButton);

        applyStyles(alert);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == confirmButton;
    }

    // ============================================================
    // SHOW AND WAIT (Generic)
    // ============================================================

    /**
     * Shows an alert and waits for user response.
     * Returns the ButtonType that was clicked.
     */
    public static Optional<ButtonType> showAndWait(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        applyStyles(alert);

        return alert.showAndWait();
    }

    /**
     * Shows an alert with custom buttons and waits for user response.
     * Returns the ButtonType that was clicked.
     */
    public static Optional<ButtonType> showAndWait(String title, String message, Alert.AlertType type, ButtonType... buttons) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getButtonTypes().setAll(buttons);

        applyStyles(alert);

        return alert.showAndWait();
    }

    // ============================================================
    // CORE LOGIC
    // ============================================================

    /**
     * Shows an alert of given type with given title and message.
     */
    private static void show(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        applyStyles(alert);

        alert.showAndWait();
    }

    /**
     * Applies consistent styling to alerts.
     */
    private static void applyStyles(Alert alert) {
        try {
            DialogPane dialogPane = alert.getDialogPane();

            // Apply CSS if available
            String cssPath = AlertUtil.class.getResource(ALERT_CSS).toExternalForm();
            if (cssPath != null) {
                dialogPane.getStylesheets().add(cssPath);
            }

            dialogPane.getStyleClass().add("alert-dialog");

        } catch (Exception e) {
            // CSS not found or error loading - continue without styling
        }
    }
}