package app.barbman.core.controller;

import app.barbman.core.Main;
import app.barbman.core.model.User;
import app.barbman.core.util.SessionManager;
import app.barbman.core.util.WindowManager;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainController {
    private static final Logger logger = LogManager.getLogger(Main.class);
    @FXML
    private BorderPane borderPane; // Contenedor principal de la vista
    @FXML
    private ToggleButton btnIncome;
    @FXML
    private ToggleButton btnExpenses;
    @FXML
    private ToggleButton btnSalaries;
    @FXML
    private ToggleButton btnCash;
    @FXML
    private ToggleGroup menuGroup; // Grupo de botones de menú

    // FIXME: Arreglar todo
    @FXML
    private void onLogout() {
        logger.info("[MAIN-VIEW] Logging out.");
        SessionManager.endSession();
        WindowManager.switchWindow(
                (Stage) borderPane.getScene().getWindow(),
                "/app/barbman/core/view/login-view.fxml"
        );
    }
    /**
     * Inicializa el controlador.
     * Selecciona el botón de inicio al arrancar y configura la navegación entre vistas.
     */
    @FXML
    public void initialize() {
        User activeUser = SessionManager.getActiveUser();
        if (SessionManager.getActiveUser() != null && "admin".equals(SessionManager.getActiveUser().getRole())) {
            WindowManager.setEmbeddedView(borderPane,"left", "/app/barbman/core/view/sidebar/sidebar-admin.fxml");
        }

        // Selecciona el botón de inicio por defecto
        menuGroup.selectToggle(btnIncome);
        WindowManager.setEmbeddedView(borderPane, "center", "/app/barbman/core/view/embed-view/services-view.fxml");

        // Listener for menu buttons
        menuGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == btnIncome) {
                WindowManager.setEmbeddedView(borderPane, "center", "/app/barbman/core/view/embed-view/services-view.fxml");
            } else if (newToggle == btnExpenses) {
                WindowManager.setEmbeddedView(borderPane, "center", "/app/barbman/core/view/embed-view/egresos-view.fxml");
            } else if (newToggle == btnSalaries) {
                WindowManager.setEmbeddedView(borderPane, "center", "/app/barbman/core/view/embed-view/sueldos-view.fxml");
            } else if (newToggle == btnCash) {
                WindowManager.setEmbeddedView(borderPane, "center", "/app/barbman/core/view/embed-view/caja-view.fxml");
            } else if (newToggle == null) {
                menuGroup.selectToggle(oldToggle != null ? oldToggle : btnIncome);
            } else {
                borderPane.setCenter(null);
            }
            // Previene la deselección por clic en el botón ya seleccionado
            ToggleButton[] botones = new ToggleButton[]{btnIncome, btnExpenses, btnSalaries, btnCash /*, btnResumen*/};
            for (ToggleButton btn : botones) {
                if (btn != null) { // Evita el error si el botón es null
                    btn.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, event -> {
                        if (btn.isSelected()) {
                            event.consume();
                        }
                    });
                }
            }
        });
    }
}
