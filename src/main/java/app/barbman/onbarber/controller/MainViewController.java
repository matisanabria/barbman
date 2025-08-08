package app.barbman.onbarber.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class MainViewController {
    @FXML
    private BorderPane borderPane;

    @FXML
    public void initialize() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/barbman/onbarber/view/servicios-view.fxml"));
            Parent serviciosView = loader.load();
            borderPane.setCenter(serviciosView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
