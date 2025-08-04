package app.barbman.onbarber.controller;

import app.barbman.onbarber.model.Barbero;
import app.barbman.onbarber.service.pin.PinService;
import app.barbman.onbarber.session.AppSession;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.Node;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PinController {
     private static final Logger logger = LogManager.getLogger(PinController.class);

    @FXML
    private Label loginLabel;
    @FXML
    private PasswordField pinField;



    public void wrongPin() {
        logger.info("PIN inválido");
        loginLabel.setVisible(true);
        if (!pinField.getStyleClass().contains("error")) {
            pinField.getStyleClass().add("error");
        }
        shake(pinField);
    }

    public void loginController() {
        logger.info("PIN válido. Iniciando sesión.");
        pinField.getStyleClass().remove("error");
        loginLabel.setVisible(false);


        String PIN = pinField.getText();
        Barbero sesion = PinService.getSesion(PIN);

        if (sesion != null && sesion.getPin().equals(PIN)) {
            AppSession.iniciarSesion(sesion);
            // TODO: Abrir ventana principal
        } else {
            wrongPin();
        }
    }

    private void shake(Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), node);
        tt.setFromX(0);
        tt.setByX(10);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.play();
    }


}
