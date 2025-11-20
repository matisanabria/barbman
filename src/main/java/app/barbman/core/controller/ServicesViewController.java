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
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kordamp.ikonli.javafx.FontIcon;

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
    private final ToggleGroup paymentGroup = new ToggleGroup();

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

    @FXML
    private ResourceBundle resources; // This will be injected by JavaFX
    @FXML
    private HBox paymentButtonsBox;

    // History table filter controls
    @FXML private ComboBox<User> filterUserComboBox;
    @FXML private DatePicker filterDatePicker;
    @FXML private Button filterButton;
    @FXML private Button clearFilterButton;

    @FXML private ComboBox<String> dateFilterCombo;

    private final UsersRepository usersRepo = new UsersRepositoryImpl();
    private final UsersService usersService = new UsersService(new UsersRepositoryImpl());

    private final ServiceDefinitionsService serviceDefinitionsService =
            new ServiceDefinitionsService(new ServiceDefinitionRepositoryImpl());

    private final PaymentMethodsService paymentMethodsService =
            new PaymentMethodsService(new PaymentMethodRepositoryImpl());

    private final ServicesService servicesService = new ServicesService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logger.info("{} Initializing services view...", PREFIX);

        this.resources = resourceBundle; // ✅ Guardamos el bundle recibido
        logger.info("{} Bundle cargado -> {}", PREFIX,
                (resources != null ? resources.getBaseBundleName() : "null"));

        setupTable();

        // ComboBoxes
        loadUsers();
        loadDefinedServices();
        loadPaymentMethodButtons();

        loadServicesHistory();


        dateFilterCombo.setItems(FXCollections.observableArrayList(
                "Today",
                "Yesterday",
                "Last 7 days",
                "This month",
                "Last month",
                "Custom..."
        ));

        dateFilterCombo.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected == null) return;

            switch (selected) {
                case "Today" -> filterDatePicker.setValue(LocalDate.now());
                case "Yesterday" -> filterDatePicker.setValue(LocalDate.now().minusDays(1));
                case "Last 7 days" -> {
                    // TODO
                }
                case "This month" -> {
                }
                case "Last month" -> {
                }
                case "Custom..." -> {
                }
            }
        });


        itemsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        servicesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

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

    /** Loads defined services into the serviceComboBox with a custom StringConverter. */
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

    /** Dynamically loads payment method buttons into the HBox. */
    private FontIcon getPaymentIcon(String methodName) {
        switch (methodName.toLowerCase()) {
            case "cash":
                return new FontIcon("fas-money-bill");
            case "transfer":
                return new FontIcon("fas-university"); // banco
            case "qr":
                return new FontIcon("fas-qrcode");
            case "card":
                return new FontIcon("fas-credit-card");
            default:
                return new FontIcon("fas-question-circle");
        }
    }
    private void loadPaymentMethodButtons() {
        List<PaymentMethod> methods = paymentMethodsService.getAllPaymentMethods();

        for (PaymentMethod method : methods) {

            // === 🔤 1) Buscar traducción si existe ===
            String key = "payment_method_" + method.getName().toLowerCase();
            String label;

            if (resources != null && resources.containsKey(key)) {
                label = resources.getString(key); // traducido
            } else {
                label = method.getName(); // fallback
            }

            // === 🧩 2) Crear botón ===
            ToggleButton btn = new ToggleButton();
            btn.setUserData(method);
            btn.setToggleGroup(paymentGroup);
            btn.getStyleClass().add("payment-toggle");

            // === 🎨 3) Color por método ===
            switch (method.getName().toLowerCase()) {
                case "cash", "efectivo" -> btn.getStyleClass().add("payment-cash");
                case "transfer", "transferencia" -> btn.getStyleClass().add("payment-transfer");
                case "qr" -> btn.getStyleClass().add("payment-qr");
                case "card", "tarjeta" -> btn.getStyleClass().add("payment-card");
                default -> btn.getStyleClass().add("payment-default");
            }

            // === 🖼️ 4) Icono según método ===
            FontIcon icon = getPaymentIcon(method.getName());
            icon.setIconSize(16);

            Label text = new Label(label); // 👈 ahora sí usa traducción

            HBox box = new HBox(8, icon, text);
            box.setStyle("-fx-alignment: center;");
            btn.setGraphic(box);

            paymentButtonsBox.getChildren().add(btn);
        }
    }




    /** Shows a confirmation dialog before saving the service. */
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

    /** Returns the selected PaymentMethod from the ToggleGroup, or null if none is selected. */
    private PaymentMethod getSelectedPaymentMethod() {
        Toggle selected = paymentGroup.getSelectedToggle();
        if (selected == null) {
            AlertUtil.showError("Please select a payment method.");
            return null;
        }
        return (PaymentMethod) selected.getUserData();
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
