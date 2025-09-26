package app.barbman.core.controller;

import app.barbman.core.Main;
import app.barbman.core.util.AppSession;
import app.barbman.core.util.WindowManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Controlador principal de la aplicación.
 * Gestiona la navegación entre vistas mediante botones de menú.
 */
public class MainController {
    private static final Logger logger = LogManager.getLogger(Main.class);
    @FXML
    private BorderPane borderPane; // Contenedor principal de la vista
    @FXML
    private ToggleButton btnInicio; // Botón de "Inicio"
    @FXML
    private ToggleButton btnServicios;
    @FXML
    private ToggleButton btnEgresos;
    @FXML
    private ToggleButton btnSueldos;
    @FXML
    private ToggleButton btnCaja;
    @FXML
    private ToggleButton btnResumen;
    @FXML
    private ToggleGroup menuGroup; // Grupo de botones de menú

    @FXML
    private void onCerrarSesion() {
        logger.info("[MAIN-VIEW] Cerrando sesión y volviendo a la pantalla de login.");
        AppSession.cerrarSesion();
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
        // Selecciona el botón de inicio por defecto
        menuGroup.selectToggle(btnServicios);
        setView("/app/barbman/core/view/embed-view/servicios-view.fxml");

        // Listener para cambios de selección en el grupo de botones
        menuGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == btnServicios) {
                setView("/app/barbman/core/view/embed-view/servicios-view.fxml");
            } else if (newToggle==btnEgresos) {
                setView("/app/barbman/core/view/embed-view/egresos-view.fxml");
            } else if (newToggle==btnSueldos) {
                setView("/app/barbman/core/view/embed-view/sueldos-view.fxml");
            } else if (newToggle==btnCaja) {
                setView("/app/barbman/core/view/embed-view/caja-view.fxml");
            } else if (newToggle==btnResumen){
                setView("/app/barbman/core/view/embed-view/caja-resumen-view.fxml");
            }
            else if (newToggle == null) {
                menuGroup.selectToggle(oldToggle != null ? oldToggle : btnServicios);
            } else {
                borderPane.setCenter(null);
            }
            // Previene la deselección por clic en el botón ya seleccionado
            ToggleButton[] botones = new ToggleButton[]{btnServicios, btnEgresos, btnSueldos, btnCaja /*, btnResumen*/};
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

        // Establece la vista inicial si el botón de inicio está seleccionado
//        if (menuGroup.getSelectedToggle() == btnInicio) {
//            setView("/app/barbman/onbarber/view/servicios-view.fxml");
//        }
    }

    /**
     * Carga un archivo FXML y lo establece en el centro del BorderPane.
     *
     * @param fxmlPath ruta del archivo FXML a cargar
     */
    private void setView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            borderPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
