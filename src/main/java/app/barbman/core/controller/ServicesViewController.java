package app.barbman.core.controller;

import app.barbman.core.model.*;
import app.barbman.core.model.services.Service;
import app.barbman.core.model.services.ServiceDefinition;
import app.barbman.core.repositories.paymentmethod.PaymentMethodRepository;
import app.barbman.core.repositories.paymentmethod.PaymentMethodRepositoryImpl;
import app.barbman.core.repositories.users.UsersRepositoryImpl;
import app.barbman.core.util.NumberFormatterUtil;
import app.barbman.core.util.SessionManager;
import app.barbman.core.repositories.users.UsersRepository;
import app.barbman.core.repositories.services.servicedefinition.ServiceDefinitionRepository;
import app.barbman.core.repositories.services.servicedefinition.ServiceDefinitionRepositoryImpl;
import app.barbman.core.repositories.services.service.ServiceRepository;
import app.barbman.core.repositories.services.service.ServiceRepositoryImpl;
import app.barbman.core.service.servicios.ServicioRealizadoService;
import app.barbman.core.util.TextFormatterUtil;
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
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador para la vista de servicios realizados.
 * Gestiona la tabla que muestra los servicios realizados por los barberos.
 */
public class ServicesViewController implements Initializable {

    private static final Logger logger = LogManager.getLogger(ServicesViewController.class);
    private static final String PREFIX = "[SERV-VIEW]";

    @FXML private TableView<Service> servicesTable;
    @FXML private TableColumn<Service, String> colUser;
    @FXML private TableColumn<Service, String> colServiceType;
    @FXML private TableColumn<Service, String> colPrice;
    @FXML private TableColumn<Service, String> colPaymentMethod;
    @FXML private TableColumn<Service, java.util.Date> colDate;
    @FXML private TableColumn<Service, String> colNotes;

    @FXML private ChoiceBox<User> userChoiceBox;
    @FXML private ChoiceBox<ServiceDefinition> serviceChoiceBox;
    @FXML private ChoiceBox<PaymentMethod> paymentMethodBox;
    @FXML private TextField priceField;
    @FXML private TextField notesField;
    @FXML private Button saveButton;

    private final UsersRepository usersRepo = new UsersRepositoryImpl();
    private final ServiceDefinitionRepository serviceRepo = new ServiceDefinitionRepositoryImpl();
    private final PaymentMethodRepository paymentRepo = new PaymentMethodRepositoryImpl();
    private final ServiceRepository performedRepo = new ServiceRepositoryImpl();
    private final ServicioRealizadoService performedService = new ServicioRealizadoService(performedRepo);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("{} Initializing services view...", PREFIX);

        setupTable();

        // ChoiceBoxes
        loadUsers();
        loadDefinedServices();
        loadPaymentMethods();

        displayServices();

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

        logger.info("{} Initialized successfully.", PREFIX);
    }

    private void setupTable() {
        servicesTable.getColumns().forEach(c -> c.setReorderable(false));
        servicesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        colUser.setCellValueFactory(cd -> {
            User user = usersRepo.findById(cd.getValue().getUserId());
            String userName = (user != null && user.getName() != null)
                    ? TextFormatterUtil.capitalizeFirstLetter(user.getName())
                    : "Unknown";
            return new SimpleStringProperty(userName);
        });

        colServiceType.setCellValueFactory(cd -> {
            ServiceDefinition serviceDef = serviceRepo.findById(cd.getValue().getServiceTypeId());
            String serviceName = (serviceDef != null && serviceDef.getName() != null)
                    ? TextFormatterUtil.capitalizeFirstLetter(serviceDef.getName())
                    : "Unknown";
            return new SimpleStringProperty(serviceName);
        });

        colPaymentMethod.setCellValueFactory(cd -> {
            PaymentMethod p = paymentRepo.findById(cd.getValue().getPaymentMethodId());
            String formattedName = (p != null && p.getName() != null)
                    ? TextFormatterUtil.capitalizeFirstLetter(p.getName())
                    : "Unknown";
            return new SimpleStringProperty(formattedName);
        });

        colPrice.setCellValueFactory(cd -> new SimpleStringProperty(NumberFormatterUtil.format(cd.getValue().getPrice()) + " Gs"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colNotes.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getNotes()));
    }

    private void loadUsers() {
        List<User> users = usersRepo.findAll();
        userChoiceBox.setItems(FXCollections.observableArrayList(users));
        userChoiceBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(User u) { return u != null ? u.getName() : ""; }
            @Override
            public User fromString(String s) { return null; }
        });

        User active = SessionManager.getActiveUser();
        if (active != null && users.contains(active)) {
            userChoiceBox.setValue(active);
            logger.info("{} Active user preselected: {}", PREFIX, active.getName());
        }
    }

    private void loadDefinedServices() {
        List<ServiceDefinition> services = serviceRepo.findAll();
        serviceChoiceBox.setItems(FXCollections.observableArrayList(services));
        serviceChoiceBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(ServiceDefinition s) { return s != null ? s.getName() : ""; }
            @Override
            public ServiceDefinition fromString(String s) { return null; }
        });
        logger.info("{} {} defined services loaded into ChoiceBox.", PREFIX, services.size());
    }

    private void loadPaymentMethods() {
        List<PaymentMethod> payments = paymentRepo.findAll();
        paymentMethodBox.setItems(FXCollections.observableArrayList(payments));
        paymentMethodBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(PaymentMethod p) {
                return p != null ? TextFormatterUtil.capitalizeFirstLetter(p.getName()) : "";
            }
            @Override
            public PaymentMethod fromString(String s) { return null; }
        });
        logger.info("{} {} payment methods loaded into ChoiceBox.", PREFIX, payments.size());
    }

    private void displayServices() {
        logger.info("{} Loading performed services list...", PREFIX);
        List<Service> services = performedRepo.findAll();
        Collections.reverse(services);
        servicesTable.setItems(FXCollections.observableArrayList(services));
        logger.info("{} {} services loaded in table.", PREFIX, services.size());
    }

    private void saveService() {
        User user = userChoiceBox.getValue();
        ServiceDefinition serviceDefinition = serviceChoiceBox.getValue();
        PaymentMethod paymentMethod = paymentMethodBox.getValue();
        String priceStr = priceField.getText().replace(".", "").trim();
        String notes = TextFormatterUtil.capitalizeFirstLetter(notesField.getText().trim());

        if (user == null) {
            showAlert("You must select a barber.");
            logger.warn("{} Validation failed: barber not selected.", PREFIX);
            return;
        }
        if (serviceDefinition == null) {
            showAlert("You must select a service type.");
            logger.warn("{} Validation failed: service not selected.", PREFIX);
            return;
        }
        if (priceStr.isEmpty()) {
            showAlert("You must enter a price.");
            logger.warn("{} Validation failed: price is empty.", PREFIX);
            return;
        }
        if (paymentMethod == null) {
            showAlert("You must select a payment method.");
            logger.warn("{} Validation failed: payment method empty.", PREFIX);
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);

            performedService.addServicioRealizado(
                    user.getId(),
                    serviceDefinition.getId(),
                    price,
                    paymentMethod.getId(),
                    notes
            );
            logger.info("{} Service registered -> User: {}, Service: {}, Price: {}, PaymentMethod: {}",
                    PREFIX,
                    user.getName(),
                    serviceDefinition.getName(),
                    price,
                    paymentMethod.getId()
            );

            displayServices();
            clearFields();
        } catch (NumberFormatException e) {
            showAlert("The 'Price' field must be a valid number.");
            logger.error("{} Error parsing price: {}", PREFIX, e.getMessage());
        } catch (IllegalArgumentException e) {
            showAlert(e.getMessage());
            logger.warn("{} Validation failed when saving service: {}", PREFIX, e.getMessage());
        }
    }


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
        // Limpia los campos de texto
        priceField.clear();
        notesField.clear();

        // Resetea las ChoiceBox al primer elemento disponible (si existe)
        if (!userChoiceBox.getItems().isEmpty())
            userChoiceBox.getSelectionModel().selectFirst();

        if (!serviceChoiceBox.getItems().isEmpty())
            serviceChoiceBox.getSelectionModel().selectFirst();

        if (!paymentMethodBox.getItems().isEmpty())
            paymentMethodBox.getSelectionModel().selectFirst();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
