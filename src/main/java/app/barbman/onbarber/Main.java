package app.barbman.onbarber;

import app.barbman.onbarber.repositories.DbBootstrap;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class Main extends Application {
    private static final Logger logger = LogManager.getLogger(Main.class);

    @Override
    public void start(Stage stage) throws IOException {
        // Cargar las fuentes personalizadas
        logger.info("Cargando fuentes");
        Font.loadFont(getClass().getResourceAsStream("/fonts/Inter_24pt-Bold.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/fonts/Inter_24pt-Italic.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/fonts/Inter_24pt-Light.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/fonts/Inter_24pt-Regular.ttf"), 14);

        // Obtener la versión de la aplicación
        String version = getAppVersion();

        // Cargar la interfaz desde el archivo FXML
        logger.info("Iniciando FXML");
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("view/login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().add(getClass().getResource("style/main.css").toExternalForm());
        stage.setTitle("Barbman (" + version + ")");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        DbBootstrap.init();
        logger.info("Iniciando aplicación.");
        launch();

    }

    /**
     * Obtiene la versión de la aplicación desde el archivo version.properties.
     *
     * @return La versión de la aplicación como una cadena.
     */
    public static String getAppVersion() {
        try (InputStream input = Main.class.getResourceAsStream("/version.properties")) {
            Properties props = new Properties();
            props.load(input);
            return props.getProperty("version", "UNKNOWN");
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }
}
