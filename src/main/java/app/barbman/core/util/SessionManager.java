package app.barbman.core.util;

import app.barbman.core.dto.services.SaleCartDTO;
import app.barbman.core.model.User;
import javafx.scene.layout.BorderPane;

import java.util.Locale;

public class SessionManager {
    /** Active user in session */
    private static User activeUser;

    /** UI root*/
    private static BorderPane mainBorderPane;

    /** Temporal storage for the ServiceDTO being created/edited */
    private static SaleCartDTO currentServiceDTO;

    /**
     * USER SESSION METHODS
     */
    public static void startSession(User user) {
        SessionManager.activeUser = user;
    }

    public static User getActiveUser() {
        return activeUser;
    }

    public static void endSession() {
        SessionManager.activeUser = null;
    }

    public static boolean isSessionActive() {
        return activeUser != null;
    }

    /**
     * LOCALE METHODS
     */
    public static Locale getCurrentLocale() {
        // TODO: implementar selección de idioma real más adelante
        return null;
    }

    /**
     * UI METHODS
     * Sets the main BorderPane of the application
     * @param pane
     */
    public static void setMainBorderPane(BorderPane pane) {
        mainBorderPane = pane;
    }

    public static BorderPane getMainBorderPane() {
        return mainBorderPane;
    }

    /**
     * SERVICE DTO METHODS
     * Methods to manage the current ServiceDTO in session
     */
    public static void setCurrentCartDTO(SaleCartDTO dto) {
        currentServiceDTO = dto;
    }

    public static SaleCartDTO getCurrentCartDTO() {
        return currentServiceDTO;
    }

    public static void clearCurrentCartDTO() {
        currentServiceDTO = null;
    }
}
