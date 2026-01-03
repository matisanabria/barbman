package app.barbman.core;

import app.barbman.core.repositories.DbBootstrap;
import app.barbman.core.util.ErrorWindowUtil;
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
                        .build()
        );
        Platform.runLater(() -> {
        });
    }
    @Override
    public void stop() {
        // Esto lo hago para evitar errores, no es estrictamente necesario
        logger.info("[BARBMAN] App closed.");
        LogManager.shutdown();
    }
    public static void main(String[] args) {

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Logger logger = LogManager.getLogger(Main.class);
            logger.error("[FATAL] Uncaught exception in thread {}", thread.getName(), throwable);

            Platform.runLater(() -> {
                WindowManager.showExclusive(
                        WindowRequest.builder()
                                .fxml("/app/barbman/core/view/error-view.fxml")
                                .title("Error crítico")
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
}
