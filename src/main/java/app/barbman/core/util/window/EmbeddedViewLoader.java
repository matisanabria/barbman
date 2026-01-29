package app.barbman.core.util.window;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.List;

/**
 * Utility class responsible for loading and embedding FXML views
 * into a BorderPane at a given position.
 *
 * This loader is intentionally "dumb":
 * - It does NOT assume base styles
 * - It does NOT guess CSS paths
 * - It does NOT apply implicit conventions
 *
 * All styling decisions must be explicit and intentional.
 */
public final class EmbeddedViewLoader {

    private static final Logger logger = LogManager.getLogger(EmbeddedViewLoader.class);

    private EmbeddedViewLoader() {
        // Utility class
    }

    // ============================================================
    // ======================= PUBLIC API =========================
    // ============================================================

    /**
     * Loads an FXML view and embeds it into the given BorderPane
     * at the specified position.
     *
     * Optionally applies one or more CSS files to the embedded view.
     *
     * @param container the BorderPane where the view will be embedded
     * @param position  the target position inside the BorderPane
     * @param fxmlPath  absolute classpath to the FXML file
     * @param cssPaths  optional list of absolute classpath CSS files
     */
    public static void load(
            BorderPane container,
            Position position,
            String fxmlPath,
            String... cssPaths
    ) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    EmbeddedViewLoader.class.getResource(fxmlPath),
                    WindowManager.getBundle()
            );

            Parent view = loader.load();

            applyCss(view, cssPaths);
            place(container, position, view);

            logger.info(
                    "[EMBED] Loaded view '{}' into position {}",
                    fxmlPath,
                    position
            );

        } catch (Exception e) {
            logger.error(
                    "[EMBED] Failed to load embedded view: {}",
                    fxmlPath,
                    e
            );
            // No alerts here by design
        }
    }

    // ============================================================
    // ======================= INTERNAL ===========================
    // ============================================================

    /**
     * Places the given view into the BorderPane at the requested position.
     *
     * This method replaces any existing node in that position.
     *
     * @param pane     target BorderPane
     * @param position desired placement position
     * @param view     loaded FXML root node
     */
    private static void place(
            BorderPane pane,
            Position position,
            Parent view
    ) {
        // This is necessary to ensure only one view exists per region
        // otherwise old embedded views may remain attached to the scene graph
        switch (position) {
            case CENTER -> pane.setCenter(view);
            case LEFT -> pane.setLeft(view);
            case RIGHT -> pane.setRight(view);
            case TOP -> pane.setTop(view);
            case BOTTOM -> pane.setBottom(view);
        }
    }

    /**
     * Applies the given CSS files to the embedded view.
     *
     * CSS paths must be absolute classpath locations.
     * Missing CSS files are ignored but logged.
     *
     * @param view     the embedded root node
     * @param cssPaths optional list of CSS paths
     */
    private static void applyCss(
            Parent view,
            String... cssPaths
    ) {
        if (cssPaths == null || cssPaths.length == 0) {
            // No styles requested, this is valid
            logger.debug("[EMBED-CSS] No CSS injected");
            return;
        }

        for (String cssPath : cssPaths) {
            URL cssUrl = EmbeddedViewLoader.class.getResource(cssPath);

            if (cssUrl != null) {
                view.getStylesheets().add(cssUrl.toExternalForm());
                logger.info("[EMBED-CSS] Applied CSS: {}", cssPath);
            } else {
                // We do not fail hard on missing CSS
                // because embedded views must remain functional without styles
                logger.warn(
                        "[EMBED-CSS] CSS not found, ignored: {}",
                        cssPath
                );
            }
        }
    }

    /**
     * Valid embedding positions inside a BorderPane.
     */
    public enum Position {
        CENTER,
        LEFT,
        RIGHT,
        TOP,
        BOTTOM
    }
}
