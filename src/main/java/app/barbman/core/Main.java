package app.barbman.core;

import app.barbman.core.infrastructure.HibernateUtil;
import app.barbman.core.repositories.DbBootstrap;
import app.barbman.core.util.window.WindowManager;
import app.barbman.core.util.window.WindowRequest;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;


public class Main extends Application {
    private static final Logger logger = LogManager.getLogger(Main.class);

    @Override
    public void start(Stage stage) throws IOException {
        WindowManager.open(
                WindowRequest.builder()
                        .fxml("/app/barbman/core/view/login-view.fxml")
                        .css("/app/barbman/core/style/login.css")
                        .icon("/app/barbman/core/icons/icon-for-javafx.png")
                        .build()
        );
        Platform.runLater(() -> {
        });
    }

    @Override
    public void stop() {
        HibernateUtil.shutdown();
        logger.info("[BARBMAN] App closed.");
        LogManager.shutdown();
    }

    public static void main(String[] args) {

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Logger logger = LogManager.getLogger(Main.class);

            // Check if it's the known TextField formatting bug
            if (isTextFieldFormattingBug(throwable)) {
                logger.warn("[JAVAFX-EXCEPTION] Known TextField bug caught (rapid delete): {}",
                        throwable.getMessage());
                logger.debug("[JAVAFX-EXCEPTION] Stack trace:", throwable);
                // Don't crash - just log and ignore
                return;
            }

            // For other exceptions, log as error and show fatal error screen
            logger.error("[FATAL] Uncaught exception in thread {}", thread.getName(), throwable);

            Platform.runLater(() -> {
                // Pass exception to the controller
                app.barbman.core.controller.FatalErrorController.setLastException(throwable);

                WindowManager.showExclusive(
                        WindowRequest.builder()
                                .fxml("/app/barbman/core/view/fatal-error-view.fxml")
                                .css("/app/barbman/core/style/fatal-error.css")
                                .title("Error crítico")
                                .icon("/app/barbman/core/icons/icon-for-javafx.png")
                                .build()
                );
            });
        });

        // Inicializa la base de datos
        DbBootstrap.init();

        // Mensaje de inicio en el log
        String separador = "=".repeat(60);
        String horaInicio = java.time.LocalDateTime.now().toString();

        logger.info("""
            \n{}
            [BARBMAN] Starting application
            Hour: {}
            System OS: {} {}
            User: {}
            Java ver.: {}
            {}
            """,
                separador,
                horaInicio,
                System.getProperty("os.name"),
                System.getProperty("os.version"),
                System.getProperty("user.name"),
                System.getProperty("java.version"),
                separador
        );

        launch();
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
}