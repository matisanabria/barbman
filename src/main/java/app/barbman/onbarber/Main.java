package app.barbman.onbarber;

import app.barbman.onbarber.model.ServicioRealizado;
import app.barbman.onbarber.repositories.DbBootstrap;
import app.barbman.onbarber.repositories.servicio.ServicioRealizadoRepository;
import app.barbman.onbarber.repositories.servicio.ServicioRealizadoRepositoryImpl;
import app.barbman.onbarber.service.servicios.ServicioRealizadoService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.io.IOException;


public class Main extends Application {
    private static final Logger logger = LogManager.getLogger(Main.class);

    @Override
    public void start(Stage stage) throws IOException {
        logger.info("Iniciando FXML");
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("view/login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        // scene.getStylesheets().add(getClass().getResource("style/main.css").toExternalForm());
        stage.setTitle("Barbman (Snapshot OB 1.0)");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        DbBootstrap.init();
        logger.info("Iniciando aplicación.");
        ServicioRealizadoRepository repo = new ServicioRealizadoRepositoryImpl();
        ServicioRealizadoService sr = new ServicioRealizadoService(repo);
        logger.info("Creando nuevo servicio en database");
        ServicioRealizado nuevoServicio = sr.addServicioRealizado(
                1,             // barberoId
                2,             // tipoServicio
                40000,          // precio
                "efectivo",    // formaPago
                "Cliente pidió degradado en los laterales" // observaciones
        );
        logger.info("Servicio registrado: ");
        logger.info(nuevoServicio.toString());

        launch();

    }
}
