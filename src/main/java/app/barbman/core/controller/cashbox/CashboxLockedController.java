package app.barbman.core.controller.cashbox;

import app.barbman.core.util.SessionManager;
import app.barbman.core.util.window.WindowManager;
import app.barbman.core.util.window.WindowRequest;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class CashboxLockedController {

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private void onLogout() {
        // cerrar sesión
        SessionManager.endSession();

        // obtener el stage actual
        Stage currentStage = (Stage) anchorPane.getScene().getWindow();

        // volver al login
        WindowManager.switchWindow(
                currentStage,
                WindowRequest.builder()
                        .fxml("/app/barbman/core/view/login-view.fxml")
                        .css("/app/barbman/core/style/login.css")
                        .build()
        );
    }
}
