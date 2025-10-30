package app.barbman.core.controller;

import app.barbman.core.model.User;
import app.barbman.core.util.SessionManager;
import app.barbman.core.util.WindowManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    @FXML
    public void initialize() {
        User activeUser = SessionManager.getActiveUser();
        if (activeUser != null && "admin".equals(activeUser.getRole())) {
            WindowManager.setEmbeddedView(borderPane, "left", "/app/barbman/core/view/sidebar/sidebar-admin.fxml");
        }
        // Map buttons to their respective views
        viewMap.put(incomeButton, "/app/barbman/core/view/embed-view/services-view.fxml");
        viewMap.put(expenseButton, "/app/barbman/core/view/embed-view/expenses-view.fxml");
        viewMap.put(salariesButton, "/app/barbman/core/view/embed-view/sueldos-view.fxml");
        viewMap.put(cashButton, "/app/barbman/core/view/embed-view/caja-view.fxml");

        // Prevents deselection of toggle buttons
        ToggleButton[] botones = new ToggleButton[]{incomeButton, expenseButton, salariesButton, cashButton /*, btnResumen*/};
        for (ToggleButton btn : botones) {
            if (btn != null) {
                btn.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, event -> {
                    if (btn.isSelected()) {
                        event.consume();
                    }
                });
            }
        }

        // Logout button action
        logoutButton.setOnAction(event -> onLogout());

        // Listener for menu buttons
        menuGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            ToggleButton selected = (ToggleButton) newToggle;
            // If none selected, revert to old or default
            if (selected == null) {
                menuGroup.selectToggle(oldToggle != null ? oldToggle : incomeButton);
                return;
            }
            // Load corresponding view according to selection
            String viewPath = viewMap.get(selected);
            if (viewPath != null) {
                WindowManager.setEmbeddedView(borderPane, "center", viewPath);
            }
        });

        menuGroup.selectToggle(incomeButton);
        WindowManager.setEmbeddedView(borderPane, "center", viewMap.get(incomeButton));
    }

    @FXML
    private void onLogout() {
        logger.info("{} Logging out.", PREFIX);
        SessionManager.endSession();
        WindowManager.switchWindow(
                (Stage) borderPane.getScene().getWindow(),
                "/app/barbman/core/view/login-view.fxml"
        );
    }
}
