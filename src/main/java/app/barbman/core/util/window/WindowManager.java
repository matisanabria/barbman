package app.barbman.core.util.window;

import app.barbman.core.util.AlertUtil;
import javafx.fxml.FXMLLoader;
import javafx.scene.text.Font;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Centralized window navigation manager.
 * Handles opening, switching, modal windows and exclusive views.
 *
 * FXML controls layout.
 * CSS is optional and explicit.
 */
public class WindowManager {
    private static final Logger logger = LogManager.getLogger(WindowManager.class);
    private static boolean fontsLoaded = false;


    // ============================================================
    // ======================= PUBLIC API =========================
    // ============================================================

    /** Opens a new window without closing others. */
    public static Object open(WindowRequest request) {
        return openInternal(request, null, false, false);
    }

    /** Opens a new window and closes the current one (used for login / lock). */
    public static Object switchWindow(Stage currentStage, WindowRequest next) {
        Object controller = openInternal(next, null, false, false);
        if (currentStage != null) {
            logger.info("[WINDOW] Closing current window");
            currentStage.close();
        }
        return controller;
    }

    /** Opens a modal window (APPLICATION_MODAL). */
    public static Object openModal(WindowRequest request) {
        if (request.getOwner() == null) {
            throw new IllegalStateException("Modal window requires an owner Stage.");
        }
        return openInternal(request, null, true, false);
    }

    /** Closes ALL windows and opens only the requested one (fatal error / lock). */
    public static Object showExclusive(WindowRequest request) {
        closeAllWindows();
        return openInternal(request, null, false, true);
    }

    // ============================================================
    // ====================== CORE LOGIC ==========================
    // ============================================================

    private static Object openInternal(
            WindowRequest request,
            Stage forcedOwner,
            boolean forceModal,
            boolean exclusive
    ) {
        try {
            loadFontsOnce();
            ResourceBundle bundle = WindowManager.getBundle();

            FXMLLoader loader = new FXMLLoader(
                    WindowManager.class.getResource(request.getFxmlPath()),
                    bundle
            );

            Parent root = loader.load();
            Scene scene = new Scene(root);

            // CSS (explicit, optional)
            injectCss(scene, List.of(
                    "/app/barbman/core/style/main.css"
            ));
            injectCss(scene, request.getCssPaths());

            Stage stage = new Stage();
            stage.setScene(scene);

            // Title
            if (request.getTitle() != null && !request.getTitle().isBlank()) {
                stage.setTitle(request.getTitle());
            } else {
                stage.setTitle("Barbman");
            }

            // Owner
            if (request.getOwner() != null) {
                stage.initOwner(request.getOwner());
            } else if (forcedOwner != null) {
                stage.initOwner(forcedOwner);
            }

            // Modality
            if (request.isModal() || forceModal) {
                stage.initModality(Modality.APPLICATION_MODAL);
            }

            // Resizable
            stage.setResizable(request.isResizable());

            logger.info("[WINDOW] Opened: {}", request.getFxmlPath());
            stage.show();

            return request.shouldReturnController()
                    ? loader.getController()
                    : null;

        } catch (Exception e) {
            logger.error("[WINDOW] Failed to open window: {}", request.getFxmlPath(), e);
            AlertUtil.showError(
                    "No se pudo abrir la ventana:\n" + request.getFxmlPath(),
                    "An unexpected error occurred while opening the cashbox.");
            return null;
        }
    }

    // ============================================================
    // ======================= CSS LOGIC ==========================
    // ============================================================

    private static void injectCss(Scene scene, List<String> cssPaths) {
        if (cssPaths == null || cssPaths.isEmpty()) return;

        for (String css : cssPaths) {
            URL cssUrl = WindowManager.class.getResource(css);
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
                logger.info("[CSS] Loaded: {}", css);
            } else {
                logger.warn("[CSS] NOT FOUND: {}", css);
                AlertUtil.showWarning(
                        "No se pudo cargar el archivo CSS:\n" + css, "No se encontró el recurso.");
            }
        }
    }

    // ============================================================
    // ======================= UTILITIES ==========================
    // ============================================================

    private static void closeAllWindows() {
        List<Window> windows = new ArrayList<>(Window.getWindows());
        for (Window w : windows) {
            if (w instanceof Stage stage) {
                logger.info("[WINDOW] Closing window");
                stage.close();
            }
        }
    }

    /** Always provides the active ResourceBundle. */
    public static ResourceBundle getBundle() {
        return WindowLanguageHolder.getBundle();
    }

    // ======================= FONT LOGIC ==========================
    private static void loadFontsOnce() {
        if (fontsLoaded) return;

        logger.info("[FONT] Loading application fonts");

        loadFont("/fonts/Inter_24pt-Regular.ttf");
        loadFont("/fonts/Inter_24pt-Bold.ttf");
        loadFont("/fonts/Inter_24pt-Light.ttf");
        loadFont("/fonts/Inter_24pt-Italic.ttf");

        fontsLoaded = true;
    }

    private static void loadFont(String path) {
        try {
            var stream = WindowManager.class.getResourceAsStream(path);
            if (stream == null) {
                logger.warn("[FONT] Resource not found: {}", path);
                return;
            }

            Font font = Font.loadFont(stream, 14);

            if (font != null) {
                logger.info("[FONT] Loaded: {}", path);
            } else {
                // Esto es NORMAL en el primer load de JavaFX
                logger.debug("[FONT] Deferred load (JavaFX timing): {}", path);
            }

        } catch (Exception e) {
            logger.error("[FONT] Error loading font: {}", path, e);
        }
    }


}
