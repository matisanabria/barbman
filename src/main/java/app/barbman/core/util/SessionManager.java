package app.barbman.core.util;

import app.barbman.core.model.User;

public class SessionManager {
    private static User activeUser;

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
}
