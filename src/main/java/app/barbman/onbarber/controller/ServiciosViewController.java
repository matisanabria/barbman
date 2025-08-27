package app.barbman.onbarber.controller;

import app.barbman.onbarber.appsession.AppSession;
import app.barbman.onbarber.model.Barbero;
import app.barbman.onbarber.model.ServicioRealizado;
import app.barbman.onbarber.repositories.barbero.BarberoRepository;
import app.barbman.onbarber.repositories.barbero.BarberoRepositoryImpl;
import app.barbman.onbarber.repositories.serviciorealizado.ServicioRealizadoRepository;
import app.barbman.onbarber.repositories.serviciorealizado.ServicioRealizadoRepositoryImpl;
import app.barbman.onbarber.service.servicios.ServicioRealizadoService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.Collections;
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
    private TableColumn<ServicioRealizado, String> colBarbero;
    @FXML
    private TableColumn<ServicioRealizado, Integer> colTipoServicio;
    @FXML
    private TableColumn<ServicioRealizado, Integer> colPrecio;
    @FXML
    private TableColumn<ServicioRealizado, java.util.Date> colFecha;
    @FXML
    private TableColumn<ServicioRealizado, String> colObservaciones;

    // Fields para agregar servicios
    @FXML
    private ChoiceBox<Barbero> barberoField;
    @FXML
    private javafx.scene.control.TextField tipoServicioField;
    @FXML
    private javafx.scene.control.TextField precioField;
    @FXML
    private javafx.scene.control.TextField observacionesField;
    @FXML
    private ChoiceBox<String> formaPagoBox;
    @FXML
    private javafx.scene.control.Button guardarButton;

    ServicioRealizadoRepository repo = new ServicioRealizadoRepositoryImpl();
    ServicioRealizadoService sr = new ServicioRealizadoService(repo);
    private final BarberoRepository barberoRepo = new BarberoRepositoryImpl();

    /**
     * Metodo de inicialización del controlador.
     * Se llama automáticamente al cargar el FXML.
     * Configura las columnas de la tabla y carga los servicios realizados.
     *
     * @param location  ubicación para resolver rutas relativas de recursos
     * @param resources recursos utilizados para localizar objetos raíz
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Para que las columnas queden fijas
        serviciosTable.getColumns().forEach(col -> col.setReorderable(false));
        serviciosTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        // Configuración de las columnas de la tabla con las propiedades del modelo ServicioRealizad
        colBarbero.setCellValueFactory(cellData -> {
            int barberoId = cellData.getValue().getBarberoId();
            String nombre = barberoRepo.getNombreById(barberoId); // implementa este metodo según tu lógica
            return new SimpleStringProperty(nombre);
        });
        colTipoServicio.setCellValueFactory(new PropertyValueFactory<>("tipoServicio"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colObservaciones.setCellValueFactory(new PropertyValueFactory<>("observaciones"));

        mostrarServicios(); // Carga y muestra los servicios realizados en la tabla
        // Llenar los choicebox con datos de la base
        cargarBarberos();

        guardarButton.setOnAction(e -> guardarServicio());
    }

    /**
     * Recupera los servicios realizados desde el repositorio y los muestra en la tabla.
     * Si la lista está vacía, la tabla queda vacía.
     */
    void mostrarServicios() {
        ServicioRealizadoRepositoryImpl repo = new ServicioRealizadoRepositoryImpl();
        List<ServicioRealizado> servicios = repo.findAll();
        Collections.reverse(servicios);
        serviciosTable.setItems(FXCollections.observableArrayList(servicios));
    }

    /**
     * Guarda un nuevo servicio realizado utilizando los datos ingresados en los campos de texto.
     * Realiza validaciones para asegurar que los datos sean correctos antes de guardar.
     * Muestra alertas en caso de errores de validación.
     */
    private void guardarServicio() {
        Barbero barbero = barberoField.getValue();
        String tipoServicio = tipoServicioField.getText();
        String precioStr = precioField.getText();
        String observaciones = observacionesField.getText();

        // Validación de campos vacíos
        if (barbero == null) {
            mostrarAlerta("Debe seleccionar un barbero.");
            return;
        }
        if (tipoServicio == null || tipoServicio.trim().isEmpty()) {
            mostrarAlerta("El campo 'Tipo de servicio' es obligatorio.");
            return;
        }
        if (precioStr == null || precioStr.trim().isEmpty()) {
            mostrarAlerta("El campo 'Precio' es obligatorio.");
            return;
        }

        // Validación de tipoServicio como número
        int tipoServicioInt;
        try {
            tipoServicioInt = Integer.parseInt(tipoServicio.trim());
        } catch (NumberFormatException e) {
            mostrarAlerta("El campo 'Tipo de servicio' debe ser un número.");
            return;
        }

        // Validación de precio como número y no negativo
        double precio;
        try {
            precio = Double.parseDouble(precioStr.trim());
        } catch (NumberFormatException e) {
            mostrarAlerta("El campo 'Precio' debe ser un número.");
            return;
        }
        if (precio < 0) {
            mostrarAlerta("El precio no puede ser negativo.");
            return;
        }

        sr.addServicioRealizado(
                barbero.getId(),            // barberoId
                tipoServicioInt,             // tipoServicio
                precio,          // precio
                "efectivo",    // formaPago
                observaciones // observaciones
        );

        // Recarga la tabla para mostrar los nuevos datos
        mostrarServicios();
    }

    /**
     * Muestra una alerta con el mensaje proporcionado.
     *
     * @param mensaje El mensaje a mostrar en la alerta.
     */
    private void mostrarAlerta(String mensaje) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Validación");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void cargarBarberos() {
        List<Barbero> barberos = barberoRepo.loadBarberos();
        barberoField.setItems(FXCollections.observableArrayList(barberos));
        barberoField.setConverter(new StringConverter<Barbero>() {
            @Override
            public String toString(Barbero b) {
                return (b == null) ? "" : b.getNombre();
            }
            @Override
            public Barbero fromString(String s) { return null; }
        });

        // Selecciona automáticamente el barbero activo si está en la lista
        Barbero activo = AppSession.getBarberoActivo();
        if (activo != null && barberos.contains(activo)) {
            barberoField.setValue(activo);
        }
    }
}
