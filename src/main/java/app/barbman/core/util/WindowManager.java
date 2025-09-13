package app.barbman.core.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class WindowManager {
    private static final Logger logger = LogManager.getLogger(WindowManager.class);
    private static boolean fontsLoaded = false;

    /**
     * Abre una nueva ventana con el FXML indicado y configura fuentes, estilos y título con versión.
     *
     * @param fxmlPath Ruta del archivo FXML
     */
    // Sobrecarga para permitir establecer una ventana padre (owner)
    public static void openWindow(String fxmlPath, String title, Stage owner){
        try {
            loadFontsOnce();

            FXMLLoader loader = new FXMLLoader(WindowManager.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            scene.getStylesheets().add(WindowManager.class.getResource("/app/barbman/core/style/main.css").toExternalForm());

            Stage stage = new Stage();
            stage.setResizable(false);
            if (title == null || title.isBlank()) {
                stage.setTitle("Barbman (" + getAppVersion() + ")");
            } else {
                stage.setTitle(title);
            }

            if (owner != null) {
                stage.initOwner(owner); // hace que sea hijo
            }

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            logger.error("Error abriendo ventana: " + fxmlPath, e);
        }
    }
    // Sobrecarga para establecer título
    public static void openWindow(String fxmlPath, String title) {
        try {
            loadFontsOnce();

            FXMLLoader loader = new FXMLLoader(WindowManager.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            scene.getStylesheets().add(WindowManager.class.getResource("/app/barbman/core/style/main.css").toExternalForm());

            Stage stage = new Stage();
            stage.setResizable(false);
            if (title == null || title.isBlank()) {
                stage.setTitle("Barbman (" + getAppVersion() + ")");
            } else {
                stage.setTitle(title);
            }
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            logger.error("Error abriendo ventana: " + fxmlPath, e);
        }
    }
    // Sobrecarga para no establecer titulo (usa default)
    public static void openWindow(String fxmlPath) {
        openWindow(fxmlPath, null);
    }


    /**
     * Cambia de ventana: abre una nueva y cierra la actual.
     *
     * @param currentStage Stage actual a cerrar
     * @param fxmlPath     FXML de la nueva ventana
     * @param title        Título de la ventana nueva (puede ser null)
     */
    public static void switchWindow(Stage currentStage, String fxmlPath, String title) {
        openWindow(fxmlPath, title);
        currentStage.close();
    }
    public static void switchWindow(Stage currentStage, String fxmlPath) {
        switchWindow(currentStage, fxmlPath, null);
    }

    /**
     * Abre una nueva ventana y devuelve el controller asociado.
     *
     * @param fxmlPath Ruta al archivo FXML
     * @param title    Título de la ventana
     * @param owner    Stage padre (puede ser null)
     * @param <T>      Tipo del controller
     * @return El controller del FXML cargado
     */
    public static <T> T openWindowWithController(String fxmlPath, String title, Stage owner) {
        try {
            loadFontsOnce();

            FXMLLoader loader = new FXMLLoader(WindowManager.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            scene.getStylesheets().add(WindowManager.class.getResource("/app/barbman/core/style/main.css").toExternalForm());

            Stage stage = new Stage();
            stage.setResizable(false);
            if (title == null || title.isBlank()) {
                stage.setTitle("Barbman (" + getAppVersion() + ")");
            } else {
                stage.setTitle(title);
            }

            if (owner != null) {
                stage.initOwner(owner); // hace que sea hijo
            }

            stage.setScene(scene);
            stage.show();

            return loader.getController();
        } catch (IOException e) {
            logger.error("Error abriendo ventana con controller: " + fxmlPath, e);
            throw new RuntimeException("No se pudo abrir ventana: " + fxmlPath, e);
        }
    }

    /**
     * Carga las fuentes personalizadas solo una vez en toda la aplicación.
     */
    private static void loadFontsOnce() {
        if (!fontsLoaded) {
            logger.info("Cargando fuentes personalizadas (solo una vez)");
            Font.loadFont(WindowManager.class.getResourceAsStream("/fonts/Inter_24pt-Bold.ttf"), 14);
            Font.loadFont(WindowManager.class.getResourceAsStream("/fonts/Inter_24pt-Italic.ttf"), 14);
            Font.loadFont(WindowManager.class.getResourceAsStream("/fonts/Inter_24pt-Light.ttf"), 14);
            Font.loadFont(WindowManager.class.getResourceAsStream("/fonts/Inter_24pt-Regular.ttf"), 14);
            fontsLoaded = true;
        }
    }

    /**
     * Obtiene la versión de la aplicación desde el archivo version.properties.
     *
     * @return La versión de la aplicación como una cadena.
     */
    public static String getAppVersion() {
        try (InputStream input = WindowManager.class.getResourceAsStream("/version.properties")) {
            Properties props = new Properties();
            props.load(input);
            return props.getProperty("version", "UNKNOWN");
        } catch (Exception e) {
            logger.warn("No se pudo obtener la versión.", e);
            return "UNKNOWN";
        }
    }

}
