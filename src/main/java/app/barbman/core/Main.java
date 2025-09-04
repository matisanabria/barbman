package app.barbman.core;

import app.barbman.core.repositories.DbBootstrap;
import app.barbman.core.util.WindowManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;


public class Main extends Application {
    private static final Logger logger = LogManager.getLogger(Main.class);

    @Override
    public void start(Stage stage) throws IOException {
        // Manejo global de errores no capturados
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            logger.error("Error no capturado en hilo " + thread.getName(), throwable);

            // Mostrar alerta al usuario
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error inesperado");
                alert.setHeaderText("Ha ocurrido un problema en la aplicación.");
                alert.setContentText(throwable.getMessage());
                alert.showAndWait();
            });
        });
        WindowManager.openWindow("/app/barbman/core/view/login-view.fxml");
    }

    public static void main(String[] args) {
        DbBootstrap.init();
        logger.info("Iniciando aplicación.");
        launch();

    }
}
