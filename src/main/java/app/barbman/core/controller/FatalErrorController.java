package app.barbman.core.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class FatalErrorController {

    @FXML
    private Label errorMessage;

    public void setMessage(String msg) {
        errorMessage.setText(msg);
    }
}
