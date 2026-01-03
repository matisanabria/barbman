package app.barbman.core.controller;

import app.barbman.core.model.human.User;
import app.barbman.core.util.SessionManager;
import app.barbman.core.util.window.EmbeddedViewLoader;
import app.barbman.core.util.window.WindowManager;
import app.barbman.core.util.window.WindowRequest;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainViewController {

    private static final Logger logger = LogManager.getLogger(MainViewController.class);
    private static final String PREFIX = "[MAIN-VIEW]";

    @FXML private BorderPane borderPane;
    @FXML private Button logoutButton;

    @FXML
    public void initialize() {
        logger.info("{} Initializing main view", PREFIX);

        loadSidebarForUser();
        SessionManager.setMainBorderPane(borderPane);

        // Vista inicial
        EmbeddedViewLoader.load(
                borderPane,
                EmbeddedViewLoader.Position.CENTER,
                "/app/barbman/core/view/embed-view/sale-create-view.fxml"
        );

        logoutButton.setOnAction(e -> logout());
    }

    // ============================================================
    // ======================= SIDEBAR ============================
    // ============================================================

    private void loadSidebarForUser() {
        User user = SessionManager.getActiveUser();
        if (user == null) return;

        String sidebarPath =
                "admin".equals(user.getRole())
                        ? "/app/barbman/core/view/sidebar/sidebar-admin.fxml"
                        : "/app/barbman/core/view/sidebar/sidebar-user.fxml";

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(sidebarPath),
                    WindowManager.getBundle()
            );

            Node sidebar = loader.load();
            SidebarController controller = loader.getController();
            controller.bind(borderPane);

            borderPane.setLeft(sidebar);
            logger.info("{} Sidebar loaded: {}", PREFIX, sidebarPath);

        } catch (Exception e) {
            logger.error("{} Failed to load sidebar", PREFIX, e);
        }
    }

    // ============================================================
    // ======================== LOGOUT ============================
    // ============================================================

    private void logout() {
        logger.info("{} Logging out", PREFIX);

        SessionManager.endSession();

        Stage currentStage = (Stage) borderPane.getScene().getWindow();

        WindowManager.switchWindow(
                currentStage,
                WindowRequest.builder()
                        .fxml("/app/barbman/core/view/login-view.fxml")
                        .css("/app/barbman/core/style/login.css")
                        .build()
        );
    }
}
