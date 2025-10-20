package app.barbman.core.controller;

import app.barbman.core.model.User;
import app.barbman.core.util.SessionManager;
import app.barbman.core.model.ServicioDefinido;
import app.barbman.core.model.ServicioRealizado;
import app.barbman.core.repositories.barbero.BarberoRepository;
import app.barbman.core.repositories.barbero.BarberoRepositoryImpl;
import app.barbman.core.repositories.serviciodefinido.ServicioDefinidoRepository;
import app.barbman.core.repositories.serviciodefinido.ServicioDefinidoRepositoryImpl;
import app.barbman.core.repositories.serviciorealizado.ServicioRealizadoRepository;
import app.barbman.core.repositories.serviciorealizado.ServicioRealizadoRepositoryImpl;
import app.barbman.core.service.servicios.ServicioRealizadoService;
import app.barbman.core.util.NumberFormatUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador para la vista de servicios realizados.
 * Gestiona la tabla que muestra los servicios realizados por los barberos.
 */
public class ServiciosController implements Initializable {

    private static final Logger logger = LogManager.getLogger(ServiciosController.class);
    // Tabla de servicios realizados
    @FXML
    private TableView<ServicioRealizado> serviciosTable;

    // Columnas de la tabla
    @FXML
    private TableColumn<ServicioRealizado, String> colBarbero;
    @FXML
    private TableColumn<ServicioRealizado, String> colTipoServicio;
    @FXML
    private TableColumn<ServicioRealizado, String> colPrecio;
    @FXML
    private TableColumn<ServicioRealizado, java.util.Date> colFecha;
    @FXML
    private TableColumn<ServicioRealizado, String> colObservaciones;

    // Fields para agregar servicios
    @FXML
    private ChoiceBox<User> barberoChoiceBox;
    @FXML
    private ChoiceBox<ServicioDefinido> tipoServicioBox;
    @FXML
    private javafx.scene.control.TextField precioField;
    @FXML
    private TableColumn<ServicioRealizado, String> colFormaPago;
    @FXML
    private javafx.scene.control.TextField observacionesField;
    @FXML
    private ChoiceBox<String> formaPagoBox;
    @FXML
    private javafx.scene.control.Button guardarButton;

    private final ServicioRealizadoRepository srRepository = new ServicioRealizadoRepositoryImpl();
    private final ServicioRealizadoService srService = new ServicioRealizadoService(srRepository);
    private final BarberoRepository barberoRepository = new BarberoRepositoryImpl();
    private final ServicioDefinidoRepository sdRepository = new ServicioDefinidoRepositoryImpl();

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
        logger.info("[SERV-VIEW] Inicializando vista de servicios...");

        // Para que las columnas queden fijas
        serviciosTable.getColumns().forEach(col -> col.setReorderable(false));
        serviciosTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Configuración de las columnas de la tabla con las propiedades del modelo ServicioRealizad
        colBarbero.setCellValueFactory(cellData -> {
            int barberoId = cellData.getValue().getBarberoId();
            User b = barberoRepository.findById(barberoId);
            String nombre = (b != null) ? b.getName() : "Desconocido";
            return new SimpleStringProperty(nombre);
        });
        colTipoServicio.setCellValueFactory(cellData -> {
            int servicioId = cellData.getValue().getTipoServicio();
            ServicioDefinido servicio = sdRepository.findById(servicioId); // Carga servicio definido según ID
            String nombre = (servicio != null) ? servicio.getNombre() : "Desconocido";
            return new SimpleStringProperty(nombre);
        });
        colPrecio.setCellValueFactory(cellData -> {
            double precio = cellData.getValue().getPrecio();
            return new SimpleStringProperty(NumberFormatUtil.format(precio) + " Gs");
        });
        colFormaPago.setCellValueFactory(cellData -> {
            String formaPago = cellData.getValue().getFormaPago();
            return new SimpleStringProperty(formaPago != null ? formaPago : "");
        });
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colObservaciones.setCellValueFactory(new PropertyValueFactory<>("observaciones"));

        mostrarServicios();// Carga y muestra los servicios realizados en la tabla
        cargarBarberos();
        cargarServiciosDefinidos();
        // Autocompleta el precio al seleccionar un tipo de servicio
        tipoServicioBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                long precioBase = (long) newVal.getPrecioBase(); // convierte 40000.0 a 40000
                String formateado = NumberFormatUtil.format(precioBase);
                precioField.setText(formateado); // se pone formateado
                precioField.positionCaret(formateado.length());
            }
        });

        // Opciones de forma de pago
        formaPagoBox.setItems(FXCollections.observableArrayList(
                "efectivo", "transferencia", "pos"
        ));
        formaPagoBox.setValue("efectivo"); // opción por defecto

        guardarButton.setOnAction(e -> guardarServicio());

        // Doble clic para borrar un servicio realizado
        serviciosTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && !serviciosTable.getSelectionModel().isEmpty()) {
                ServicioRealizado seleccionado = serviciosTable.getSelectionModel().getSelectedItem();
                if (seleccionado != null) {
                    confirmarYBorrar(seleccionado);
                }
            }
        });

        NumberFormatUtil.applyToTextField(precioField);

        logger.info("[SERV-VIEW] Vista de servicios inicializada correctamente.");
    }

    /**
     * Recupera los servicios realizados desde el repositorio y los muestra en la tabla.
     * Si la lista está vacía, la tabla queda vacía.
     */
    void mostrarServicios() {
        logger.info("[SERV-VIEW] Cargando lista de servicios realizados...");
        List<ServicioRealizado> servicios = srRepository.findAll();
        Collections.reverse(servicios);
        serviciosTable.setItems(FXCollections.observableArrayList(servicios));
        logger.info("[SERV-VIEW] {} servicios cargados en la tabla.", servicios.size());
    }

    /**
     * Guarda un nuevo servicio realizado utilizando los datos ingresados en los campos de texto.
     * Realiza validaciones para asegurar que los datos sean correctos antes de guardar.
     * Muestra alertas en caso de errores de validación.
     */
    private void guardarServicio() {
        User user = barberoChoiceBox.getValue();
        ServicioDefinido servicioDefinido = tipoServicioBox.getValue();
        String precioStr = precioField.getText().replace(".", "").trim();
        String observaciones = observacionesField.getText();
        String formaPago = formaPagoBox.getValue();

        if (user == null) {
            mostrarAlerta("Debe seleccionar un user.");
            logger.warn("[SERV-VIEW] Validación fallida: user no seleccionado.");
            return;
        }

        if (servicioDefinido == null) {
            mostrarAlerta("Debe seleccionar un tipo de servicio.");
            logger.warn("[SERV-VIEW] Validación fallida: servicio no seleccionado.");
            return;
        }

        if (precioStr.isEmpty()) {
            mostrarAlerta("Debe ingresar un precio.");
            logger.warn("[SERV-VIEW] Validación fallida: precio vacío.");
            return;
        }

        if (formaPago == null || formaPago.isBlank()) {
            mostrarAlerta("Debe seleccionar una forma de pago.");
            logger.warn("[SERV-VIEW] Validación fallida: forma de pago vacía.");
            return;
        }

        try {
            double precio = Double.parseDouble(precioStr);

            srService.addServicioRealizado(
                    (user != null) ? user.getId() : null,
                    (servicioDefinido != null) ? servicioDefinido.getId() : null,
                    precio,
                    formaPago,
                    observaciones
            );
            logger.info("[SERV-VIEW] Servicio registrado -> User: {}, Servicio: {}, Precio: {}, FormaPago: {}",
                    user.getName(),
                    (servicioDefinido != null ? servicioDefinido.getNombre() : "N/A"),
                    precio,
                    formaPago
            );
            mostrarServicios();
        } catch (NumberFormatException e) {
            mostrarAlerta("El campo 'Precio' debe ser un número válido.");
            logger.error("[SERV-VIEW] Error al parsear precio: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            mostrarAlerta(e.getMessage());
            logger.warn("[SERV-VIEW] Validación fallida al guardar servicio: {}", e.getMessage());
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
        List<User> users = barberoRepository.findAll();
        barberoChoiceBox.setItems(FXCollections.observableArrayList(users));
        barberoChoiceBox.setConverter(new StringConverter<User>() {
            @Override
            public String toString(User b) {
                return (b == null) ? "" : b.getName();
            }

            @Override
            public User fromString(String s) {
                return null;
            }
        });

        // Selecciona automáticamente el barbero activo si está en la lista
        User activo = SessionManager.getActiveUser();
        if (activo != null && users.contains(activo)) {
            barberoChoiceBox.setValue(activo);
            logger.info("[SERV-VIEW] User activo preseleccionado: {}", activo.getName());
        }
    }

    /** Carga los servicios definidos desde el repositorio y los asigna al ChoiceBox.
     * Configura un conversor para mostrar el nombre del servicio en lugar del objeto completo.
     */
    private void cargarServiciosDefinidos() {
        List<ServicioDefinido> servicios = sdRepository.findAll();
        tipoServicioBox.setItems(FXCollections.observableArrayList(servicios));
        tipoServicioBox.setConverter(new StringConverter<ServicioDefinido>() {
            @Override
            public String toString(ServicioDefinido servicio) {
                return (servicio == null) ? "" : servicio.getNombre();
            }
            @Override
            public ServicioDefinido fromString(String nombre) { return null; }
        });
        logger.info("[SERV-VIEW] {} servicios definidos cargados en ChoiceBox.", servicios.size());
    }

    /**
     * Muestra una alerta de confirmación antes de borrar un servicio realizado.
     * Si el usuario confirma, borra el servicio del repositorio y actualiza la tabla.
     *
     * @param servicio El servicio realizado a borrar.
     */
    private void confirmarYBorrar(ServicioRealizado servicio) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText("¿Seguro que quieres eliminar este servicio?");
        confirm.setContentText(
                "Servicio ID: " + servicio.getId() +
                        "\nPrecio: " + NumberFormatUtil.format(servicio.getPrecio()) + " Gs" +
                        "\nFecha: " + servicio.getFecha()
        );

        // Mostrar el alert y esperar respuesta
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                srRepository.delete(servicio.getId());
                mostrarServicios();
                logger.info("[SERV-VIEW] Servicio eliminado -> ID: {}, Precio: {}, Fecha: {}",
                        servicio.getId(), servicio.getPrecio(), servicio.getFecha());
            }
            else{
                logger.info("[SERV-VIEW] Cancelada eliminación de servicio -> ID: {}", servicio.getId());
            }
        });
    }

}
