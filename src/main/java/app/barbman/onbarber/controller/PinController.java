package app.barbman.onbarber.controller;

import app.barbman.onbarber.models.Barbero;
import app.barbman.onbarber.service.pin.PinService;
import app.barbman.onbarber.sesion.AppSession;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

public class PinController {
    @FXML
    private Label loginLabel;
    @FXML
    private PasswordField pinField;

    public void wrongPin(){
        loginLabel.setVisible(true);
    }
    public void loginController(){
        String PIN = pinField.getText();
        Barbero sesion = PinService.getSesion(PIN);

        if (sesion != null && sesion.getPin().equals(PIN)) {
            AppSession.iniciarSesion(sesion);
            // TODO: Abrir ventana principal
        } else {
            wrongPin();
        }
    }


}
