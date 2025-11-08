package app.barbman.core.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SidebarController {

    private static final Logger logger = LogManager.getLogger(SidebarController.class);
    private static final String PREFIX = "[SIDEBAR]";

    private MainViewController mainController;

    @FXML private ToggleButton btnIncome;
    @FXML private ToggleButton btnExpenses;
    @FXML private ToggleButton btnSalaries;
    @FXML private ToggleButton btnCash;
    @FXML private ToggleButton btnSettings;

    public void setMainController(MainViewController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void initialize() {
        logger.info("{} Sidebar initialized.", PREFIX);

        // Checks if null 'cause this controller might be used for other sidebars without all buttons
        if (btnIncome != null)
            btnIncome.setOnAction(e -> loadView("/app/barbman/core/view/embed-view/services-view.fxml", "Ingresos"));

        if (btnExpenses != null)
            btnExpenses.setOnAction(e -> loadView("/app/barbman/core/view/embed-view/expenses-view.fxml", "Egresos"));

        if (btnSalaries != null)
            btnSalaries.setOnAction(e -> loadView("/app/barbman/core/view/embed-view/sueldos-view.fxml", "Sueldos"));

        if (btnCash != null)
            btnCash.setOnAction(e -> loadView("/app/barbman/core/view/embed-view/caja-view.fxml", "Caja"));

        if (btnSettings != null)
            btnSettings.setOnAction(e -> loadView("/app/barbman/core/view/embed-view/settings-view.fxml", "Configuración"));
    }

    private void loadView(String path, String name) {
        if (mainController != null) {
            mainController.loadCenterView(path);
            logger.info("{} Vista '{}' cargada -> {}", PREFIX, name, path);
        } else {
            logger.warn("{} Main controller no asignado aún, no se pudo cambiar la vista.", PREFIX);
        }
    }
}
