package app.barbman.core.util;

import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Global exception handler for JavaFX Application Thread.
 * Catches uncaught exceptions to prevent app crashes.
 */
public class JavaFXExceptionHandler {

    private static final Logger logger = LogManager.getLogger(JavaFXExceptionHandler.class);

    /**
     * Install the global exception handler.
     * Call this once during application startup.
     */
    public static void install() {

        // Set default uncaught exception handler for JavaFX thread
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {

            // Check if it's the known TextField formatting bug
            if (isTextFieldFormattingBug(throwable)) {
                logger.warn("[JAVAFX-EXCEPTION] Known TextField bug caught (rapid delete): {}",
                        throwable.getMessage());
                logger.debug("[JAVAFX-EXCEPTION] Stack trace:", throwable);
                // Don't crash - just log and ignore
                return;
            }

            // For other exceptions, log as error and show fatal error screen
            logger.error("[JAVAFX-EXCEPTION] Uncaught exception in thread {}: {}",
                    thread.getName(), throwable.getMessage(), throwable);

            // Show fatal error dialog on JavaFX thread
            Platform.runLater(() -> {
                try {
                    // Your existing fatal error handler
                    showFatalErrorDialog(throwable);
                } catch (Exception e) {
                    logger.error("[JAVAFX-EXCEPTION] Failed to show error dialog", e);
                }
            });
        });

        logger.info("[JAVAFX-EXCEPTION] Global exception handler installed");
    }

    /**
     * Check if exception is the known TextField formatting bug.
     */
    private static boolean isTextFieldFormattingBug(Throwable throwable) {
        if (!(throwable instanceof IllegalArgumentException)) {
            return false;
        }

        String message = throwable.getMessage();
        if (message == null) {
            return false;
        }

        // Check for the specific error message
        if (!message.contains("The start must be <= the end")) {
            return false;
        }

        // Check if it's coming from TextField operations
        StackTraceElement[] stack = throwable.getStackTrace();
        if (stack.length == 0) {
            return false;
        }

        // Look for TextField-related classes in the stack
        for (StackTraceElement element : stack) {
            String className = element.getClassName();
            if (className.contains("TextInputControl") ||
                    className.contains("TextField") ||
                    className.contains("TextFieldSkin")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Show fatal error dialog using the same approach as Main.
     */
    private static void showFatalErrorDialog(Throwable throwable) {
        // Pass exception to the controller
        app.barbman.core.controller.FatalErrorController.setLastException(throwable);

        // Show the fatal error view
        app.barbman.core.util.window.WindowManager.showExclusive(
                app.barbman.core.util.window.WindowRequest.builder()
                        .fxml("/app/barbman/core/view/fatal-error-view.fxml")
                        .css("/app/barbman/core/style/fatal-error.css")
                        .title("Error crítico")
                        .icon("/app/barbman/core/icons/icon-for-javafx.png")
                        .build()
        );
    }
}