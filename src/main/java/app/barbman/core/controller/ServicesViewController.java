package app.barbman.core.controller;

import app.barbman.core.dto.services.ServiceDTO;
import app.barbman.core.dto.services.ServiceHistoryDTO;
import app.barbman.core.model.*;
import app.barbman.core.model.services.Service;
import app.barbman.core.model.services.ServiceDefinition;
import app.barbman.core.model.services.ServiceItem;
import app.barbman.core.repositories.paymentmethod.PaymentMethodRepositoryImpl;;
import app.barbman.core.repositories.users.UsersRepositoryImpl;
import app.barbman.core.service.paymentmethods.PaymentMethodsService;
import app.barbman.core.service.services.ServiceDefinitionsService;
import app.barbman.core.service.users.UsersService;
import app.barbman.core.util.AlertUtil;
import app.barbman.core.util.SessionManager;
import app.barbman.core.repositories.users.UsersRepository;
import app.barbman.core.repositories.services.servicedefinition.ServiceDefinitionRepositoryImpl;
import app.barbman.core.service.services.ServicesService;
import app.barbman.core.util.TextFormatterUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador para la vista de services realizados.
 * Gestiona la tabla que muestra los services realizados por los barberos.
 */
public class ServicesViewController implements Initializable {

    private static final Logger logger = LogManager.getLogger(ServicesViewController.class);
    private static final String PREFIX = "[SERV-VIEW]";

    @FXML private TableView<ServiceHistoryDTO> servicesTable;
    @FXML private TableColumn<ServiceHistoryDTO, String> colDate;
    @FXML private TableColumn<ServiceHistoryDTO, String> colUser;
    @FXML private TableColumn<ServiceHistoryDTO, String> colServiceType;
    @FXML private TableColumn<ServiceHistoryDTO, String> colPaymentMethod;
    @FXML private TableColumn<ServiceHistoryDTO, String> colPrice;
    @FXML private TableColumn<ServiceHistoryDTO, String> colNotes;

    // Service registration block
    @FXML private ComboBox<User> userComboBox;
    @FXML private ComboBox<ServiceDefinition> serviceComboBox;
    @FXML private TextField priceField;
    @FXML private TextField notesField;
    @FXML private Button addItemButton;
    @FXML private Button saveButton;
    @FXML private Button resetButton;
    @FXML private Label totalLabel;

    // Service items table
    @FXML private TableView<ServiceItem> itemsTable;
    @FXML private TableColumn<ServiceItem, String> colItemServiceType;
    @FXML private TableColumn<ServiceItem, String> colItemPrice;
    @FXML private TableColumn<ServiceItem, String> colItemRemove;

    // Payment method ComboBox and ToggleButtons
    @FXML private ToggleGroup paymentGroup;
    @FXML private ToggleButton cashButton;
    @FXML private ToggleButton cardButton;
    @FXML private ToggleButton transferButton;
    @FXML private ToggleButton qrButton;

    // History table filter controls
    @FXML private ComboBox<User> filterUserComboBox;
    @FXML private DatePicker filterDatePicker;
    @FXML private Button filterButton;
    @FXML private Button clearFilterButton;

    private final UsersRepository usersRepo = new UsersRepositoryImpl();
    private final UsersService usersService = new UsersService(new UsersRepositoryImpl());

    private final ServiceDefinitionsService serviceDefinitionsService =
            new ServiceDefinitionsService(new ServiceDefinitionRepositoryImpl());

    private final PaymentMethodsService paymentMethodsService =
            new PaymentMethodsService(new PaymentMethodRepositoryImpl());

    private final ServicesService servicesService = new ServicesService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("{} Initializing services view...", PREFIX);

        setupTable();

        // ComboBoxes
        loadUsers();
        loadDefinedServices();
        loadPaymentMethods();

        loadServicesHistory();

        // Double-click to delete service from history table
        servicesTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && !servicesTable.getSelectionModel().isEmpty()) {
                ServiceHistoryDTO selected = servicesTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    confirmAndDelete(selected);
                }
            }
        });

        saveButton.setOnAction(event -> saveService()); // It's better to set this in initialize than FXML
        notesField.setOnKeyPressed(event -> {
            if (event.isShiftDown() && event.getCode() == KeyCode.ENTER) {
                confirmAndSaveService();
            }
        });

        logger.info("{} Initialized successfully.", PREFIX);
    }

    // ========================================================================
    //                          HISTORY TABLE METHODS
    // ========================================================================
    /**
     * Configures the table columns to map to ServiceHistoryDTO properties.
     * And loads data from DTOs into the table.
     */
    private void setupTable() { // Checked
        colUser.setCellValueFactory(new PropertyValueFactory<>("userName"));
        colServiceType.setCellValueFactory(new PropertyValueFactory<>("serviceNames"));
        colPaymentMethod.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("totalFormatted"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colNotes.setCellValueFactory(new PropertyValueFactory<>("notes"));
    }
    private void loadServicesHistory() { // Checked
        List<ServiceHistoryDTO> services = servicesService.getServiceHistory();
        servicesTable.setItems(FXCollections.observableArrayList(services)); // Gets data from backend
    }

    /**
     * Confirms and deletes a service from history after validating user permissions.
     * Admins can delete any service, Users can only delete services from the current day.
     * @param dto ServiceHistoryDTO to delete
     */
    private void confirmAndDelete(ServiceHistoryDTO dto) {
        User activeUser = SessionManager.getActiveUser();
        String role = activeUser.getRole();
        boolean canDelete = false;

        // Permissions. Admin can delete any, User only today's
        if ("admin".equalsIgnoreCase(role)) {
            canDelete = true;
        } else if ("user".equalsIgnoreCase(role)) {
            try {
                LocalDate serviceDate = dto.getDate();
                canDelete = serviceDate.isEqual(LocalDate.now());
            } catch (Exception e) {
                logger.warn("{} Fecha inválida al validar permisos de borrado: {}", PREFIX, e.getMessage());
            }
        }

        // If no permission, show error and exit
        if (!canDelete) {
            AlertUtil.showError("No tienes permiso para eliminar este servicio.");
            logger.warn("{} Usuario '{}' intentó eliminar un servicio sin permisos.", PREFIX, activeUser.getName());
            return;
        }

        // First alert confirmation
        Alert firstAlert = new Alert(Alert.AlertType.CONFIRMATION);
        firstAlert.setTitle("Confirmar eliminación");
        firstAlert.setHeaderText("¿Deseas eliminar este registro?");
        firstAlert.setContentText(
                        "Usuario: " + dto.getUserName() +
                        "\nServicios: " + dto.getServiceNames() +
                        "\nPago: " + dto.getPaymentMethod() +
                        "\nTotal: " + dto.getTotalFormatted() +
                        "\nFecha: " + dto.getDate() +
                        "\nNotas: " + dto.getNotes()
        );

        ButtonType aceptar = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        firstAlert.getButtonTypes().setAll(aceptar, cancelar);

        firstAlert.getDialogPane().lookupButton(aceptar).requestFocus();

        firstAlert.showAndWait().ifPresent(response -> {
            if (response == aceptar) {
                // Second confirmation alert
                Alert secondAlert = new Alert(Alert.AlertType.CONFIRMATION);
                secondAlert.setTitle("Confirmación final");
                secondAlert.setHeaderText("Esta acción no se puede deshacer.");
                secondAlert.setContentText("¿Confirmas la eliminación definitiva?");
                secondAlert.getButtonTypes().setAll(aceptar, cancelar);
                secondAlert.getDialogPane().lookupButton(aceptar).requestFocus();

                secondAlert.showAndWait().ifPresent(res2 -> {
                    if (res2 == aceptar) {
                        try {
                            servicesService.deleteServiceById(dto.getId());
                            loadServicesHistory();
                            AlertUtil.showInfo("Servicio eliminado con éxito.");
                            logger.info("{} Service deleted by {} -> ID={}", PREFIX, activeUser.getName(), dto.getId());
                        } catch (Exception e) {
                            AlertUtil.showError("Error al eliminar el servicio:\n" + e.getMessage());
                            logger.error("{} Error deleting service: {}", PREFIX, e.getMessage());
                        }
                    }
                });
            }
        });
    }

    // ========================================================================
    //                      SERVICE REGISTRATION METHODS
    // ========================================================================

    private void loadUsers() {
        userComboBox.setItems(FXCollections.observableArrayList(usersService.getAllUsers()));
    }

    private void loadDefinedServices() {
        List<ServiceDefinition> services = serviceDefinitionsService.getAll();
        serviceComboBox.setItems(FXCollections.observableArrayList(services));
        serviceComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(ServiceDefinition s) {
                return s != null ? s.getName() : "";
            }
            @Override
            public ServiceDefinition fromString(String s) {
                return null; // not editable, safe to return null
            }
        });
        logger.info("{} {} defined services loaded into ComboBox.", PREFIX, services.size());
    }

    private void loadPaymentMethods() {
        if (paymentGroup == null) {
            paymentGroup = new ToggleGroup();
            cashButton.setToggleGroup(paymentGroup);
            cardButton.setToggleGroup(paymentGroup);
            transferButton.setToggleGroup(paymentGroup);
            qrButton.setToggleGroup(paymentGroup);
        }
        cashButton.setSelected(true); // Default selection

    }

    private void confirmAndSaveService() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmar registro");
        confirmAlert.setHeaderText("¿Deseas registrar este servicio?");
        confirmAlert.setContentText("Se guardará el servicio actual en el historial.");

        // Botones personalizados (Aceptar por defecto)
        ButtonType accept = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmAlert.getButtonTypes().setAll(accept, cancel);

        // Forzar foco en el botón "Aceptar" al abrir
        confirmAlert.getDialogPane().lookupButton(accept).requestFocus();

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == accept) {
                saveService(); // Ejecuta la lógica original
            } else {
                logger.info("{} Registro cancelado por el usuario.", PREFIX);
            }
        });
    }

    private PaymentMethod getSelectedPaymentMethod() {
        ToggleButton selected = (ToggleButton) paymentGroup.getSelectedToggle();
        if (selected == null) return null;

        String key = switch (selected.getId()) {
            case "cashButton" -> "cash";
            case "cardButton" -> "card";
            case "transferButton" -> "transfer";
            case "qrButton" -> "qr";
            default -> null;
        };

        if (key != null) {
            return paymentMethodsService.getPaymentMethodByName(key);
        }
        return null;
    }

    private void saveService() {
        User user = userComboBox.getValue();
        ServiceDefinition serviceDefinition = serviceComboBox.getValue();
        PaymentMethod paymentMethod = getSelectedPaymentMethod();
        String priceStr = priceField.getText().replace(".", "").trim();
        String notes = TextFormatterUtil.capitalizeFirstLetter(notesField.getText().trim());

        if (user == null || serviceDefinition == null || paymentMethod == null || priceStr.isEmpty()) {
            AlertUtil.showError("Debes completar todos los campos obligatorios.");
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);

            Service service = new Service(
                    user.getId(),
                    LocalDate.now(),
                    paymentMethod.getId(),
                    0,
                    notes
            );

            ServiceDTO dto = new ServiceDTO(service);
            dto.addItem(new ServiceItem(0, serviceDefinition.getId(), price));

            servicesService.saveServiceWithItems(dto);

            AlertUtil.showInfo("Servicio registrado con éxito.");
            loadServicesHistory();
            clearFields();

        } catch (NumberFormatException e) {
            AlertUtil.showError("El campo 'Precio' debe ser numérico.");
            logger.error("{} Error parsing price: {}", PREFIX, e.getMessage());
        } catch (Exception e) {
            AlertUtil.showError("Error al guardar el servicio:\n" + e.getMessage());
            logger.error("{} Error saving service: {}", PREFIX, e.getMessage());
        }
    }

    private void clearFields() {
        // Clear TextFields
        priceField.clear();
        notesField.clear();

        // Reset ComboBoxes
        if (!userComboBox.getItems().isEmpty()) {userComboBox.getSelectionModel().selectFirst();}
        if (!serviceComboBox.getItems().isEmpty()) {serviceComboBox.getSelectionModel().selectFirst();}
        paymentGroup.selectToggle(null);

        // Reset total label
        totalLabel.setText("0 Gs");

        // Reset items table
        if (itemsTable != null) {itemsTable.getItems().clear();}
    }
}
