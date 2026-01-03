package app.barbman.core.controller;

import app.barbman.core.util.window.EmbeddedViewLoader;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SidebarController {

    private static final Logger logger = LogManager.getLogger(SidebarController.class);
    private static final String PREFIX = "[SIDEBAR]";

    private BorderPane targetPane;

    @FXML private ToggleButton btnIncome;
    @FXML private ToggleButton btnExpenses;
    @FXML private ToggleButton btnSalaries;
    @FXML private ToggleButton btnCash;
    @FXML private ToggleButton btnSettings;

    private final ToggleGroup group = new ToggleGroup();

    // ============================================================
    // ======================= BINDING ============================
    // ============================================================

    public void bind(BorderPane borderPane) {
        this.targetPane = borderPane;
    }

    // ============================================================
    // ====================== INITIALIZE ==========================
    // ============================================================

    @FXML
    private void initialize() {
        logger.info("{} Initializing sidebar", PREFIX);

        register(btnIncome,
                "/app/barbman/core/view/embed-view/services-create-view.fxml");

        register(btnExpenses,
                "/app/barbman/core/view/embed-view/expenses-view.fxml");

        register(btnSalaries,
                "/app/barbman/core/view/embed-view/sueldos-view.fxml");

        register(btnCash,
                "/app/barbman/core/view/embed-view/caja-view.fxml");

        register(btnSettings,
                "/app/barbman/core/view/embed-view/settings-view.fxml");

        if (btnIncome != null) {
            btnIncome.setSelected(true);

            // ✅ Cargar vista inicial manualmente
            load("/app/barbman/core/view/embed-view/sale-create-view.fxml");
        }
    }

    // ============================================================
    // ======================= INTERNAL ===========================
    // ============================================================

    private void register(ToggleButton button, String viewPath) {
        if (button == null) return;

        button.setToggleGroup(group);
        button.setOnAction(e -> load(viewPath));
    }

    private void load(String fxmlPath) {
        if (targetPane == null) {
            logger.warn("{} No target pane bound", PREFIX);
            return;
        }

        EmbeddedViewLoader.load(
                targetPane,
                EmbeddedViewLoader.Position.CENTER,
                fxmlPath
        );

        logger.info("{} Loaded view: {}", PREFIX, fxmlPath);
    }
}
