package app.barbman.core.util.window;

import app.barbman.core.util.AlertUtil;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;

public class EmbeddedViewLoader {
    private static final Logger logger = LogManager.getLogger(EmbeddedViewLoader.class);

    private static final String BASE_CSS =
            "/app/barbman/core/style/views/view-base.css";

    private EmbeddedViewLoader() {}

    // ============================================================
    // ======================= PUBLIC API =========================
    // ============================================================

    public static void load(
            BorderPane container,
            Position position,
            String fxmlPath
    ) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    EmbeddedViewLoader.class.getResource(fxmlPath),
                    WindowManager.getBundle()
            );

            Parent view = loader.load();

            injectCssIfExists(view, fxmlPath);
            place(container, position, view);

            logger.info("[EMBED] Loaded {} into {}", fxmlPath, position);

        } catch (Exception e) {
            logger.error("[EMBED] Failed to load view: {}", fxmlPath, e);
            // SIN ALERTS
        }
    }

    // ============================================================
    // ======================= INTERNAL ===========================
    // ============================================================

    private static void place(BorderPane pane, Position pos, Parent view) {
        switch (pos) {
            case CENTER -> pane.setCenter(view);
            case LEFT -> pane.setLeft(view);
            case RIGHT -> pane.setRight(view);
            case TOP -> pane.setTop(view);
            case BOTTOM -> pane.setBottom(view);
        }
    }

    private static void injectBaseCss(Parent view) {
        URL baseCss = EmbeddedViewLoader.class.getResource(BASE_CSS);

        if (baseCss != null) {
            view.getStylesheets().add(baseCss.toExternalForm());
            logger.info("[CSS] Base loaded");
        } else {
            logger.error("[CSS] Base CSS NOT FOUND: {}", BASE_CSS);
        }
    }

    private static void injectCssIfExists(Parent view, String fxmlPath) {
        String cssPath = guessCssPath(fxmlPath);
        URL cssUrl = EmbeddedViewLoader.class.getResource(cssPath);

        if (cssUrl != null) {
            view.getStylesheets().add(cssUrl.toExternalForm());
            logger.info("[EMBED-CSS] Loaded: {}", cssPath);
        } else {
            logger.debug("[EMBED-CSS] CSS not found (ignored): {}", cssPath);
        }
    }

    private static String guessCssPath(String fxmlPath) {
        String file = fxmlPath.substring(fxmlPath.lastIndexOf('/') + 1);
        String name = file.replace(".fxml", "");
        return "/app/barbman/core/style/views/" + name + ".css";
    }

    // ============================================================
    // ======================= ENUM ===============================
    // ============================================================

    public enum Position {
        CENTER, LEFT, RIGHT, TOP, BOTTOM
    }
}
