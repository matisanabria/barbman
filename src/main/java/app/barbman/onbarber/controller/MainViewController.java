package app.barbman.onbarber.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

/**
 * Controlador principal de la aplicación.
 * Gestiona la navegación entre vistas mediante botones de menú.
 */
public class MainViewController {
    @FXML
    private BorderPane borderPane; // Contenedor principal de la vista
    @FXML
    private ToggleButton btnInicio; // Botón de "Inicio"
    @FXML
    private ToggleButton btnServicios;
    @FXML
    private ToggleGroup menuGroup; // Grupo de botones de menú

    /**
     * Inicializa el controlador.
     * Selecciona el botón de inicio al arrancar y configura la navegación entre vistas.
     */
    @FXML
    public void initialize() {
        // Selecciona el botón de inicio por defecto
        menuGroup.selectToggle(btnInicio);

        // Listener para cambios de selección en el grupo de botones
        menuGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == btnServicios) {
                setView("/app/barbman/onbarber/view/servicios-view.fxml");
            }
            // Si ningún botón está seleccionado (por doble clic) vuelve a seleccionar el último
            else if (newToggle == null) {
                menuGroup.selectToggle(oldToggle);
            } else {
                // Deja el centro del BorderPane vacío
                borderPane.setCenter(null);
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
