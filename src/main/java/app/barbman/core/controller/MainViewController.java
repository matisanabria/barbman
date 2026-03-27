package app.barbman.core.controller;

import app.barbman.core.model.cashbox.CashboxOpening;
import app.barbman.core.model.human.User;
import app.barbman.core.repositories.cashbox.closure.CashboxClosureRepositoryImpl;
import app.barbman.core.repositories.cashbox.movement.CashboxMovementRepositoryImpl;
import app.barbman.core.repositories.cashbox.opening.CashboxOpeningRepositoryImpl;
import app.barbman.core.service.cashbox.CashboxService;
import app.barbman.core.util.AlertUtil;
import app.barbman.core.util.SessionManager;
import app.barbman.core.util.window.EmbeddedViewLoader;
import app.barbman.core.util.window.WindowManager;
import app.barbman.core.util.window.WindowRequest;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MainViewController {

    private static final Logger logger = LogManager.getLogger(MainViewController.class);
    private static final String PREFIX = "[MAIN-VIEW]";

    @FXML private BorderPane borderPane;
    private static MainViewController instance;

    private final CashboxService cashboxService =
            new CashboxService(
                    new CashboxOpeningRepositoryImpl(),
                    new CashboxClosureRepositoryImpl(),
                    new CashboxMovementRepositoryImpl()
            );

    @FXML
    public void initialize() {
        logger.info("{} Initializing main view", PREFIX);

        instance = this;

        User user = SessionManager.getActiveUser();
        if (user == null) return;

        loadSidebarForUser();
        SessionManager.setMainBorderPane(borderPane);

        if (!cashboxService.isCashboxOpen()) {
            Platform.runLater(() -> redirectToCashboxGate(user));
            return; // if cashbox is not opened, do not load default view
        }

        // Warn if cashbox has been open since a previous week
        Platform.runLater(this::checkStaleCashbox);

        // Cashbox is opened, load default view
        EmbeddedViewLoader.load(
                borderPane,
                EmbeddedViewLoader.Position.CENTER,
                "/app/barbman/core/view/embed-view/sale-create-view.fxml",
                "/app/barbman/core/style/embed-views/sales-view.css"
        );

    }


    // ============================================================
    // ======================= SIDEBAR ============================
    // ============================================================

    private void loadSidebarForUser() {
        User user = SessionManager.getActiveUser();
        if (user == null) return;

        String sidebarPath = "/app/barbman/core/view/sidebar/sidebar-admin.fxml";

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(sidebarPath)
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
    // ===================== STALE CASHBOX ========================
    // ============================================================

    private void checkStaleCashbox() {
        try {
            CashboxOpening opening = cashboxService.getCurrentOpening();
            if (opening == null) return;

            LocalDate openedWeekStart = opening.getOpenedAt().toLocalDate().with(DayOfWeek.MONDAY);
            LocalDate currentWeekStart = LocalDate.now().with(DayOfWeek.MONDAY);

            if (openedWeekStart.isBefore(currentWeekStart)) {
                String openedDate = opening.getOpenedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                AlertUtil.showWarning(
                        "Caja abierta desde la semana pasada",
                        "La caja fue abierta el " + openedDate + " y no se cerro.\n" +
                        "Recorda cerrarla desde la seccion Caja."
                );
            }
        } catch (Exception e) {
            logger.error("{} Error checking stale cashbox", PREFIX, e);
        }
    }

    // ============================================================
    // ======================== LOGOUT ============================
    // ============================================================

    public void logout() {
        logger.info("{} Logging out", PREFIX);

        SessionManager.endSession();

        Stage currentStage = (Stage) borderPane.getScene().getWindow();

        WindowManager.switchWindow(
                currentStage,
                WindowRequest.builder()
                        .fxml("/app/barbman/core/view/login-view.fxml")
                        .css("/app/barbman/core/style/login.css")
                        .icon("/app/barbman/core/icons/icon-for-javafx.png")
                        .build()
        );
    }

    // MÉTODO ESTÁTICO para llamar desde Sidebar
    public static void logoutFromSidebar() {
        if (instance != null) {
            instance.logout();
        }
    }

    /**
     * Redirects the user to the cashbox gate view based on their role.
     */
    private void redirectToCashboxGate(User user) {

        String fxml =
                "admin".equals(user.getRole())
                        ? "/app/barbman/core/view/cashbox/cashbox-opening-view.fxml"
                        : "/app/barbman/core/view/cashbox/cashbox-locked-view.fxml";

        Stage currentStage = (Stage) borderPane.getScene().getWindow();

        WindowManager.showExclusive(
                WindowRequest.builder()
                        .fxml(fxml)
                        .resizable(false)
                        .css("/app/barbman/core/style/cashbox.css")
                        .icon("/app/barbman/core/icons/icon-for-javafx.png")
                        .build()
        );
    }
}
