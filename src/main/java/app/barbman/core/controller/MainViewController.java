package app.barbman.core.controller;

import app.barbman.core.model.User;
import app.barbman.core.util.SessionManager;
import app.barbman.core.util.WindowManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainViewController {
    private static final Logger logger = LogManager.getLogger(MainViewController.class);
    private static final String PREFIX = "[MAIN-VIEW]";

    @FXML private BorderPane borderPane; // Container
    @FXML private ToggleGroup menuGroup; // Button group for menu navigation
    @FXML private ToggleButton incomeButton;
    @FXML private ToggleButton expenseButton;
    @FXML private ToggleButton salariesButton;
    @FXML private ToggleButton cashButton;
    @FXML private Button logoutButton;

    private final Map<ToggleButton, String> viewMap = new HashMap<>();

    public void loadCenterView(String fxmlPath) {
        WindowManager.setEmbeddedView(borderPane, "center", fxmlPath);
    }

    @FXML
    public void initialize() {
        User activeUser = SessionManager.getActiveUser();
        if (activeUser != null && "admin".equals(activeUser.getRole())) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/barbman/core/view/sidebar/sidebar-admin.fxml"));
                Node sidebar = loader.load();

                SidebarController sidebarController = loader.getController();
                sidebarController.setMainController(this);

                borderPane.setLeft(sidebar);
                logger.info("{} Sidebar admin cargado correctamente.", PREFIX);
            } catch (IOException e) {
                logger.error("{} Error al cargar sidebar: {}", PREFIX, e.getMessage());
            }
        }

        // Vista inicial (puede ser ingresos o lo que prefieras)
        loadCenterView("/app/barbman/core/view/embed-view/services-view.fxml");

        // Logout button action
        logoutButton.setOnAction(event -> onLogout());

        // Listener for menu buttons
//        menuGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
//            ToggleButton selected = (ToggleButton) newToggle;
//            // If none selected, revert to old or default
//            if (selected == null) {
//                menuGroup.selectToggle(oldToggle != null ? oldToggle : incomeButton);
//                return;
//            }
//            // Load corresponding view according to selection
//            String viewPath = viewMap.get(selected);
//            if (viewPath != null) {
//                WindowManager.setEmbeddedView(borderPane, "center", viewPath);
//            }
//        });

//        menuGroup.selectToggle(incomeButton);
//        WindowManager.setEmbeddedView(borderPane, "center", viewMap.get(incomeButton));

    }

    @FXML
    private void onLogout() {
        logger.info("{} Logging out.", PREFIX);
        SessionManager.endSession();
        WindowManager.switchWindow((Stage) borderPane.getScene().getWindow(), "/app/barbman/core/view/login-view.fxml",
           null, "/app/barbman/core/style/login.css");
    }
}
