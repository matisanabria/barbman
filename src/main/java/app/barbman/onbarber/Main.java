package app.barbman.onbarber;

import app.barbman.onbarber.repository.DbBootstrap;
import app.barbman.onbarber.util.LoggerUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        Logger logger = LoggerUtil.getLogger(LoggerUtil.class);
        DbBootstrap.init();
        System.out.println("Ruta absoluta de log: " + new File("data/barbman.log").getAbsolutePath());

        launch();

    }
}