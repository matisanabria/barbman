package app.barbman.onbarber;

import app.barbman.onbarber.repository.DbBootstrap;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.io.File;
import java.io.IOException;


public class Main extends Application {
    private static final Logger logger = LogManager.getLogger(Main.class);

    @Override
    public void start(Stage stage) throws IOException {
        logger.info("Iniciando FXML");
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("view/main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().add(getClass().getResource("style/main.css").toExternalForm());
        stage.setTitle("Barbman (Snapshot OB 1.0)");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        DbBootstrap.init();
        logger.info("Iniciando aplicación.");

        launch();

    }
}
