package app.barbman.core.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;

/**
 * Clase central para gestión de ventanas, vistas embebidas y carga de idiomas.
 * Inyecta automáticamente el ResourceBundle actual para soporte multilenguaje.
 */
public class WindowManager {
    private static final Logger logger = LogManager.getLogger(WindowManager.class);
    private static boolean fontsLoaded = false;

    // ============================================================
    // =============== GESTIÓN DE IDIOMA ==========================
    // ============================================================

    private static Locale currentLocale = new Locale("es", "ES");
    private static ResourceBundle currentBundle =
            ResourceBundle.getBundle("app.barbman.core.lang.lang", currentLocale);

    public static void setLocale(Locale locale) {
        if (locale != null) {
            currentLocale = locale;
            currentBundle = ResourceBundle.getBundle("app.barbman.core.lang.lang", currentLocale);
            logger.info("[LANG] Idioma actualizado -> {}", currentLocale);
        }
    }

    public static Locale getCurrentLocale() {
        return currentLocale;
    }

    public static ResourceBundle getBundle() {
        // Si AppSession tiene configuración de idioma, usarla
        try {
            Locale sessionLocale = SessionManager.getCurrentLocale();
            if (sessionLocale != null && !sessionLocale.equals(currentLocale)) {
                setLocale(sessionLocale);
            }
        } catch (Exception e) {
            logger.debug("[LANG] No se encontró idioma en AppSession, se usa por defecto: {}", currentLocale);
        }
        return currentBundle;
    }

    // ============================================================
    // =============== APERTURA DE VENTANAS =======================
    // ============================================================

    public static void openWindow(String fxmlPath, String title, String extraCssPath) {
        try {
            loadFontsOnce();
            FXMLLoader loader = new FXMLLoader(WindowManager.class.getResource(fxmlPath), getBundle());
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(WindowManager.class
                    .getResource("/app/barbman/core/style/main.css").toExternalForm());

            if (extraCssPath != null && !extraCssPath.isBlank()) {
                scene.getStylesheets().add(WindowManager.class.getResource(extraCssPath).toExternalForm());
            }

            Stage stage = new Stage();
            stage.setResizable(false);
            stage.getIcons().add(new javafx.scene.image.Image(
                    WindowManager.class.getResourceAsStream("/app/barbman/core/icons/icon-for-javafx.png")
            ));
            stage.setTitle(title == null || title.isBlank()
                    ? "Barbman (" + getAppVersion() + ")"
                    : title);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            logger.error("Error abriendo ventana: " + fxmlPath, e);
        }
    }

    public static void openWindow(String fxmlPath, String title, Stage owner) {
        openWindow(fxmlPath, title, owner, null);
    }

    public static void openWindow(String fxmlPath, String title, Stage owner, String extraCssPath) {
        try {
            loadFontsOnce();

            FXMLLoader loader = new FXMLLoader(WindowManager.class.getResource(fxmlPath), getBundle());
            Parent root = loader.load();
            Scene scene = new Scene(root);

            scene.getStylesheets().add(WindowManager.class
                    .getResource("/app/barbman/core/style/main.css").toExternalForm());

            if (extraCssPath != null && !extraCssPath.isBlank()) {
                scene.getStylesheets().add(WindowManager.class.getResource(extraCssPath).toExternalForm());
            }

            Stage stage = new Stage();
            stage.setResizable(false);
            stage.getIcons().add(new javafx.scene.image.Image(
                    WindowManager.class.getResourceAsStream("/app/barbman/core/icons/icon-for-javafx.png")
            ));
            stage.setTitle(title == null || title.isBlank()
                    ? "Barbman (" + getAppVersion() + ")"
                    : title);

            if (owner != null) {
                stage.initOwner(owner);
            }

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            logger.error("Error abriendo ventana con owner: " + fxmlPath, e);
        }
    }

    public static void openWindow(String fxmlPath, String title) {
        openWindow(fxmlPath, title, (String) null);
    }

    public static void openWindow(String fxmlPath) {
        openWindow(fxmlPath, null);
    }

    // ============================================================
    // =================== SWITCH WINDOWS =========================
    // ============================================================

    public static void switchWindow(Stage currentStage, String fxmlPath, String title) {
        switchWindow(currentStage, fxmlPath, title, null);
    }

    public static void switchWindow(Stage currentStage, String fxmlPath, String title, String extraCssPath) {
        openWindow(fxmlPath, title, extraCssPath);
        currentStage.close();
    }

    public static void switchWindow(Stage currentStage, String fxmlPath) {
        switchWindow(currentStage, fxmlPath, null);
    }

    // ============================================================
    // =================== OPEN WITH CONTROLLER ===================
    // ============================================================

    public static <T> T openWindowWithController(String fxmlPath, String title, Stage owner) {
        try {
            loadFontsOnce();
            FXMLLoader loader = new FXMLLoader(WindowManager.class.getResource(fxmlPath), getBundle());
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(WindowManager.class
                    .getResource("/app/barbman/core/style/main.css").toExternalForm());

            Stage stage = new Stage();
            stage.setResizable(false);
            stage.setTitle(title == null || title.isBlank()
                    ? "Barbman (" + getAppVersion() + ")"
                    : title);
            if (owner != null) {
                stage.initOwner(owner);
            }
            stage.setScene(scene);
            stage.show();

            return loader.getController();
        } catch (IOException e) {
            logger.error("Error abriendo ventana con controller: {}", fxmlPath, e);
            throw new RuntimeException("No se pudo abrir ventana: " + fxmlPath, e);
        }
    }

    // ============================================================
    // ======================= MODALES ============================
    // ============================================================

    public static <T> void openModal(String fxmlPath, Consumer<T> controllerInitializer) {
        try {
            loadFontsOnce();
            FXMLLoader loader = new FXMLLoader(WindowManager.class.getResource(fxmlPath), getBundle());
            Parent root = loader.load();
            Scene scene = new Scene(root);

            scene.getStylesheets().add(WindowManager.class
                    .getResource("/app/barbman/core/style/main.css").toExternalForm());

            Stage stage = new Stage();
            stage.setResizable(false);
            stage.setTitle("Barbman (" + getAppVersion() + ")");
            stage.getIcons().add(new javafx.scene.image.Image(
                    WindowManager.class.getResourceAsStream("/app/barbman/core/icons/icon-for-javafx.png")
            ));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setScene(scene);

            T controller = loader.getController();
            if (controllerInitializer != null) {
                controllerInitializer.accept(controller);
            }

            stage.showAndWait();
        } catch (IOException e) {
            logger.error("Error abriendo ventana modal: {}", fxmlPath, e);
        }
    }

    // ============================================================
    // =================== EMBEDDED VIEW (CSS + LANG) ==============
    // ============================================================

    public static void setEmbeddedView(BorderPane borderPane, String position, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(WindowManager.class.getResource(fxmlPath), getBundle());
            Parent view = loader.load();

            // CSS global
            borderPane.getStylesheets().add(
                    WindowManager.class.getResource("/app/barbman/core/style/main.css").toExternalForm()
            );


            // CSS específico
            String cssName = extractCssNameFromFxml(fxmlPath);
            String customCssPath = "/app/barbman/core/style/views/" + cssName + ".css";
            var cssUrl = WindowManager.class.getResource(customCssPath);
            if (cssUrl != null) {
                view.getStylesheets().add(cssUrl.toExternalForm());
                logger.info("[CSS-INJECT] CSS personalizado cargado: {}", customCssPath);
            } else {
                logger.debug("[CSS-INJECT] No se encontró CSS personalizado para {}", fxmlPath);
            }

            // Insertar vista en posición
            switch (position.toLowerCase()) {
                case "center" -> borderPane.setCenter(view);
                case "left" -> borderPane.setLeft(view);
                case "right" -> borderPane.setRight(view);
                case "top" -> borderPane.setTop(view);
                case "bottom" -> borderPane.setBottom(view);
                default -> borderPane.setCenter(view);
            }

            logger.info("[LANG-INJECT] ResourceBundle inyectado en {}", fxmlPath);

        } catch (IOException e) {
            logger.error("Error loading view: {}", fxmlPath, e);
        }
    }

    // ============================================================
    // ======================= UTILIDADES =========================
    // ============================================================

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

    private static String extractCssNameFromFxml(String fxmlPath) {
        String fileName = fxmlPath.substring(fxmlPath.lastIndexOf('/') + 1);
        return fileName.replace(".fxml", "");
    }

    /**
     * Closes all open windows except the specified one.
     * @param allowed
     */
    public static void closeAllWindowsExcept(Stage allowed) {
        List<Window> windows = new ArrayList<>(Window.getWindows());

        for (Window w : windows) {
            if (w instanceof Stage stage) {
                if (stage != allowed) {
                    stage.close();
                }
            }
        }
    }

    /**
     * Closes all open windows.
     */
    public static void closeAllWindows() {
        // make a SAFE copy
        List<Window> windows = new ArrayList<>(Window.getWindows());

        for (Window w : windows) {
            if (w instanceof Stage stage) {
                stage.close();
            }
        }
    }

}
