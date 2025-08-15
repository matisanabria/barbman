package app.barbman.onbarber.controller;

import app.barbman.onbarber.model.ServicioRealizado;
import app.barbman.onbarber.repositories.servicio.ServicioRealizadoRepositoryImpl;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador para la vista de servicios realizados.
 * Gestiona la tabla que muestra los servicios realizados por los barberos.
 */
public class ServiciosViewController implements Initializable {
    // Tabla de servicios realizados
    @FXML
    private TableView<ServicioRealizado> serviciosTable;

    // Columnas de la tabla
    @FXML
    private TableColumn<ServicioRealizado, Integer> colId;
    @FXML
    private TableColumn<ServicioRealizado, Integer> colBarberoId;
    @FXML
    private TableColumn<ServicioRealizado, Integer> colTipoServicio;
    @FXML
    private TableColumn<ServicioRealizado, Integer> colPrecio;
    @FXML
    private TableColumn<ServicioRealizado, java.util.Date> colFecha;

    /**
     * Método de inicialización del controlador.
     * Se llama automáticamente al cargar el FXML.
     * Configura las columnas de la tabla y carga los servicios realizados.
     *
     * @param location  ubicación para resolver rutas relativas de recursos
     * @param resources recursos utilizados para localizar objetos raíz
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configuración de las columnas de la tabla con las propiedades del modelo ServicioRealizado
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colBarberoId.setCellValueFactory(new PropertyValueFactory<>("barberoId"));
        colTipoServicio.setCellValueFactory(new PropertyValueFactory<>("tipoServicio"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));

        mostrarServicios(); // Carga y muestra los servicios realizados en la tabla
    }

    /**
     * Recupera los servicios realizados desde el repositorio y los muestra en la tabla.
     * Si la lista está vacía, la tabla queda vacía.
     */
    void mostrarServicios() {
        ServicioRealizadoRepositoryImpl repo = new ServicioRealizadoRepositoryImpl();
        List<ServicioRealizado> servicios = repo.findAll();
        // Si la lista es válida, la muestra en la tabla
        if (servicios != null) {
            serviciosTable.setItems(FXCollections.observableArrayList(servicios));
        }
    }
}
