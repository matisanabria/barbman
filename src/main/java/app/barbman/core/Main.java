package app.barbman.core;

import app.barbman.core.repositories.DbBootstrap;
import app.barbman.core.util.WindowManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.Notifications;

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
        // Mostrar notificación beta
        Notifications.create()
                .title("Barbman BETA")
                .text("¡Gracias por probar Barbman!\n" +
                        "Estás usando una versión Beta \uD83D\uDE80\n" +
                        "Avisanos si encontrás errores \uD83D\uDE4C")
                .position(Pos.BOTTOM_RIGHT)   // esquina inferior derecha
                .hideAfter(Duration.seconds(10)) // se oculta en 10s
                .showInformation(); // icono azul (info)
    }
    @Override
    public void stop() {
        // Esto lo hago para evitar errores, no es estrictamente necesario
        logger.info("[BARBMAN] Aplicación cerrada.");
        LogManager.shutdown();
    }
    public static void main(String[] args) {
        // Inicializa la base de datos
        DbBootstrap.init();

        // Mensaje de inicio en el log
        String separador = "=".repeat(60);
        String horaInicio = java.time.LocalDateTime.now().toString();

        logger.info("""
            \n{}
            [BARBMAN] Iniciando aplicación
            Hora de inicio: {}
            Sistema: {} {}
            Usuario: {}
            Java: {}
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
