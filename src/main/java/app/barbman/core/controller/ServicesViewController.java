package app.barbman.core.controller;

import app.barbman.core.dto.services.ServiceDTO;
import app.barbman.core.dto.services.ServiceHistoryDTO;
import app.barbman.core.model.*;
import app.barbman.core.model.services.Service;
import app.barbman.core.model.services.ServiceDefinition;
import app.barbman.core.model.services.ServiceItem;
import app.barbman.core.repositories.paymentmethod.PaymentMethodRepository;
import app.barbman.core.repositories.paymentmethod.PaymentMethodRepositoryImpl;
import app.barbman.core.repositories.services.serviceitems.ServiceItemRepository;
import app.barbman.core.repositories.services.serviceitems.ServiceItemRepositoryImpl;
import app.barbman.core.repositories.users.UsersRepositoryImpl;
import app.barbman.core.service.paymentmethods.PaymentMethodsService;
import app.barbman.core.service.services.ServiceDefinitionsService;
import app.barbman.core.service.users.UsersService;
import app.barbman.core.util.AlertUtil;
import app.barbman.core.util.NumberFormatterUtil;
import app.barbman.core.util.SessionManager;
import app.barbman.core.repositories.users.UsersRepository;
import app.barbman.core.repositories.services.servicedefinition.ServiceDefinitionRepository;
import app.barbman.core.repositories.services.servicedefinition.ServiceDefinitionRepositoryImpl;
import app.barbman.core.repositories.services.service.ServiceRepository;
import app.barbman.core.repositories.services.service.ServiceRepositoryImpl;
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
import java.util.Comparator;
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
    @FXML private ComboBox<PaymentMethod> paymentComboBox;
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

        // Double-click to delete
        servicesTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && !servicesTable.getSelectionModel().isEmpty()) {
                Service selected = servicesTable.getSelectionModel().getSelectedItem();
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
        List<PaymentMethod> methods = paymentMethodsService.getAllPaymentMethods();
        paymentComboBox.setItems(FXCollections.observableArrayList(methods));
        paymentComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(PaymentMethod p) {
                return p != null ? TextFormatterUtil.capitalizeFirstLetter(p.getName()) : "";
            }
            @Override
            public PaymentMethod fromString(String s) { return null; }
        });
        logger.info("{} {} payment methods loaded into ComboBox.", PREFIX, methods.size());
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

    private void saveService() {
        User user = userComboBox.getValue();
        ServiceDefinition serviceDefinition = serviceComboBox.getValue();
        PaymentMethod paymentMethod = paymentComboBox.getValue();
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

    // FIXME: Repair
    private void confirmAndDelete(Service service) {
        User activeUser = SessionManager.getActiveUser();
        String role = activeUser != null ? activeUser.getRole() : "";

        boolean canDelete = false;

        if ("admin".equalsIgnoreCase(role)) {
            canDelete = true;
        } else if ("user".equalsIgnoreCase(role)) {
            // service.getDate() is already LocalDate
            LocalDate serviceDate = service.getDate();
            LocalDate today = LocalDate.now();
            canDelete = serviceDate.isEqual(today);
        }

        if (!canDelete) {
            showAlert("You do not have permission to delete this service.");
            logger.warn("{} User '{}' tried to delete service ID {} but lacks permission.",
                    PREFIX, activeUser.getName(), service.getId());
            return;
        }

        // Get service name from repository and capitalize
        ServiceDefinition serviceDef = serviceRepo.findById(service.getServiceTypeId());
        String serviceName = serviceDef != null ? TextFormatterUtil.capitalizeFirstLetter(serviceDef.getName()) : "Unknown";

        // First alert: show service info
        Alert firstAlert = new Alert(Alert.AlertType.CONFIRMATION);
        firstAlert.setTitle("Confirm Deletion");
        firstAlert.setHeaderText("Do you want to delete this service?");
        firstAlert.setContentText(
                "Service ID: " + service.getId() +
                        "\nService Name: " + serviceName +
                        "\nNotes: " + (service.getNotes() != null ? TextFormatterUtil.capitalizeFirstLetter(service.getNotes()) : "") +
                        "\nPrice: " + NumberFormatterUtil.format(service.getPrice()) + " Gs" +
                        "\nDate: " + service.getDate()
        );

        firstAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Second alert: final confirmation
                Alert secondAlert = new Alert(Alert.AlertType.CONFIRMATION);
                secondAlert.setTitle("Are you sure?");
                secondAlert.setHeaderText("This action cannot be undone.");
                secondAlert.setContentText("Do you really want to delete this service?");
                secondAlert.showAndWait().ifPresent(secondResponse -> {
                    if (secondResponse == ButtonType.OK) {
                        performedRepo.delete(service.getId());
                        displayServices();
                        logger.info("{} Service deleted -> ID: {}, Name: {}, Price: {}, Date: {}",
                                PREFIX, service.getId(),
                                serviceName,
                                service.getPrice(),
                                service.getDate());
                    } else {
                        logger.info("{} Deletion cancelled at final confirmation -> Service ID: {}", PREFIX, service.getId());
                    }
                });
            } else {
                logger.info("{} Deletion cancelled -> Service ID: {}", PREFIX, service.getId());
            }
        });
    }

    private void clearFields() {
        // Clear TextFields
        priceField.clear();
        notesField.clear();

        // Reset ComboBoxes
        if (!userComboBox.getItems().isEmpty()) {userComboBox.getSelectionModel().selectFirst();}
        if (!serviceComboBox.getItems().isEmpty()) {serviceComboBox.getSelectionModel().selectFirst();}
        if (!paymentComboBox.getItems().isEmpty()) {paymentComboBox.getSelectionModel().selectFirst();}

        // Reset total label
        totalLabel.setText("0 Gs");

        // Reset items table
        if (itemsTable != null) {itemsTable.getItems().clear();}
    }


    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
