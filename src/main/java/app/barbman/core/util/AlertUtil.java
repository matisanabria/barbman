package app.barbman.core.util;

import javafx.scene.control.Alert;

/**
 * Utility class for showing different types of alerts.
 */
public class AlertUtil {

    /**
     * Shows an error alert with given message.
     * @param msg Message to show
     */
    public static void showError(String msg) { show(msg, Alert.AlertType.ERROR); }

    /**
     * Shows an information alert with given message.
     * @param msg Message to show
     */
    public static void showInfo(String msg){ show(msg, Alert.AlertType.INFORMATION); }

    /**
     * Shows an warning alert with given message.
     * @param msg Message to show
     */
    public static void showWarning(String msg) { show(msg, Alert.AlertType.WARNING); }

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
