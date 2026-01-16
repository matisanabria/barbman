package app.barbman.core.util;

import javafx.scene.control.Alert;

/**
 * Utility class for showing different types of alerts.
 */
public class AlertUtil {

    /**
     * Shows an error alert with given message.
     *
     * @param msg Message to show
     * @param s
     */
    public static void showError(String msg, String s) { show(msg, Alert.AlertType.ERROR); }

    /**
     * Shows an information alert with given message.
     *
     * @param msg Message to show
     * @param s
     */
    public static void showInfo(String msg, String s){ show(msg, Alert.AlertType.INFORMATION); }

    /**
     * Shows an warning alert with given message.
     *
     * @param msg     Message to show
     * @param message
     */
    public static void showWarning(String msg, String message) { show(msg, Alert.AlertType.WARNING); }

    /**
     * Shows an alert of given type with given message.
     */
    private static void show(String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Mensaje");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

}
