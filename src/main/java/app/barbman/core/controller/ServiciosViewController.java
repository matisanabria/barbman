package app.barbman.core.controller;

import app.barbman.core.util.AppSession;
import app.barbman.core.model.Barbero;
import app.barbman.core.model.ServicioDefinido;
import app.barbman.core.model.ServicioRealizado;
import app.barbman.core.repositories.barbero.BarberoRepository;
import app.barbman.core.repositories.barbero.BarberoRepositoryImpl;
import app.barbman.core.repositories.serviciodefinido.ServicioDefinidoRepository;
import app.barbman.core.repositories.serviciodefinido.ServicioDefinidoRepositoryImpl;
import app.barbman.core.repositories.serviciorealizado.ServicioRealizadoRepository;
import app.barbman.core.repositories.serviciorealizado.ServicioRealizadoRepositoryImpl;
import app.barbman.core.service.servicios.ServicioRealizadoService;
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
    private TableColumn<ServicioRealizado, String> colTipoServicio;
    @FXML
    private TableColumn<ServicioRealizado, Integer> colPrecio;
    @FXML
    private TableColumn<ServicioRealizado, java.util.Date> colFecha;
    @FXML
    private TableColumn<ServicioRealizado, String> colObservaciones;

    // Fields para agregar servicios
    @FXML
    private ChoiceBox<Barbero> barberoChoiceBox;
    @FXML
    private ChoiceBox<ServicioDefinido> tipoServicioBox;
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
    private final ServicioDefinidoRepository servicioDefinidoRepo = new ServicioDefinidoRepositoryImpl();

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
            Barbero b = barberoRepo.findById(barberoId);
            String nombre = (b != null) ? b.getNombre() : "Desconocido";
            return new SimpleStringProperty(nombre);
        });
        colTipoServicio.setCellValueFactory(cellData -> {
            int servicioId = cellData.getValue().getTipoServicio();
            ServicioDefinido servicio = servicioDefinidoRepo.findById(servicioId); // Carga servicio definido según ID
            String nombre = (servicio != null) ? servicio.getNombre() : "Desconocido";
            return new SimpleStringProperty(nombre);
        });
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colObservaciones.setCellValueFactory(new PropertyValueFactory<>("observaciones"));

        mostrarServicios();// Carga y muestra los servicios realizados en la tabla
        cargarBarberos();
        cargarServiciosDefinidos();
        // Autocompleta el precio al seleccionar un tipo de servicio
        tipoServicioBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                precioField.setText(String.valueOf(newVal.getPrecioBase()));
            }
        });

        // Opciones de forma de pago
        formaPagoBox.setItems(FXCollections.observableArrayList(
                "efectivo", "transferencia", "pos"
        ));
        formaPagoBox.setValue("efectivo"); // opción por defecto

        guardarButton.setOnAction(e -> guardarServicio());
    }

    /**
     * Recupera los servicios realizados desde el repositorio y los muestra en la tabla.
     * Si la lista está vacía, la tabla queda vacía.
     */
    void mostrarServicios() {
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
        Barbero barbero = barberoChoiceBox.getValue();
        ServicioDefinido servicioDefinido = tipoServicioBox.getValue();
        String precioTexto = precioField.getText().trim();
        String observaciones = observacionesField.getText();
        String formaPago = formaPagoBox.getValue();

        if (barbero == null) {
            mostrarAlerta("Debe seleccionar un barbero.");
            return;
        }

        if (servicioDefinido == null) {
            mostrarAlerta("Debe seleccionar un tipo de servicio.");
            return;
        }

        if (precioTexto.isEmpty()) {
            mostrarAlerta("Debe ingresar un precio.");
            return;
        }

        if (formaPago == null || formaPago.isBlank()) {
            mostrarAlerta("Debe seleccionar una forma de pago.");
            return;
        }

        try {
            double precio = Double.parseDouble(precioTexto);

            sr.addServicioRealizado(
                    (barbero != null) ? barbero.getId() : null,
                    (servicioDefinido != null) ? servicioDefinido.getId() : null,
                    precio,
                    formaPago,
                    observaciones
            );

            mostrarServicios();
        } catch (NumberFormatException e) {
            mostrarAlerta("El campo 'Precio' debe ser un número válido.");
        } catch (IllegalArgumentException e) {
            mostrarAlerta(e.getMessage());
        }
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

    /** Carga los barberos desde el repositorio y los asigna al ChoiceBox.
     * Si hay un barbero activo en la sesión, lo selecciona automáticamente.
     */
    private void cargarBarberos() {
        List<Barbero> barberos = barberoRepo.findAll();
        barberoChoiceBox.setItems(FXCollections.observableArrayList(barberos));
        barberoChoiceBox.setConverter(new StringConverter<Barbero>() {
            @Override
            public String toString(Barbero b) {
                return (b == null) ? "" : b.getNombre();
            }

            @Override
            public Barbero fromString(String s) {
                return null;
            }
        });

        // Selecciona automáticamente el barbero activo si está en la lista
        Barbero activo = AppSession.getBarberoActivo();
        if (activo != null && barberos.contains(activo)) {
            barberoChoiceBox.setValue(activo);
        }
    }

    /** Carga los servicios definidos desde el repositorio y los asigna al ChoiceBox.
     * Configura un conversor para mostrar el nombre del servicio en lugar del objeto completo.
     */
    private void cargarServiciosDefinidos() {
        List<ServicioDefinido> servicios = servicioDefinidoRepo.findAll();
        tipoServicioBox.setItems(FXCollections.observableArrayList(servicios));
        tipoServicioBox.setConverter(new StringConverter<ServicioDefinido>() {
            @Override
            public String toString(ServicioDefinido servicio) {
                return (servicio == null) ? "" : servicio.getNombre();
            }
            @Override
            public ServicioDefinido fromString(String nombre) { return null; }
        });
    }
}
