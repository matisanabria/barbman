package app.barbman.core.controller;

import app.barbman.core.dto.sale.CheckoutDTO;
import app.barbman.core.dto.history.ServiceHistoryDTO;
import app.barbman.core.model.User;
import app.barbman.core.repositories.users.UsersRepositoryImpl;
import app.barbman.core.service.users.UsersService;
import app.barbman.core.util.AlertUtil;
import app.barbman.core.util.SessionManager;
import app.barbman.core.util.WindowManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

/**
 * SOLO controla el historial de servicios.
 * No maneja registro ni creación.
 */
public class ServicesHistoryViewController implements Initializable {

    private static final Logger logger = LogManager.getLogger(ServicesHistoryViewController.class);
    private static final String PREFIX = "[SERV-HISTORY]";

    @FXML private TableView<ServiceHistoryDTO> servicesTable;
    @FXML private TableColumn<ServiceHistoryDTO, String> colDate;
    @FXML private TableColumn<ServiceHistoryDTO, String> colUser;
    @FXML private TableColumn<ServiceHistoryDTO, String> colServiceType;
    @FXML private TableColumn<ServiceHistoryDTO, String> colPaymentMethod;
    @FXML private TableColumn<ServiceHistoryDTO, String> colPrice;
    @FXML private TableColumn<ServiceHistoryDTO, String> colNotes;

    @FXML private Button createServiceButton;

    // Filtros
    @FXML private ComboBox<User> filterUserComboBox;
    @FXML private ComboBox<String> dateFilterCombo;
    @FXML private DatePicker filterDatePicker;
    @FXML private Button filterButton;
    @FXML private Button clearFilterButton;

    private final UsersService usersService = new UsersService(new UsersRepositoryImpl());
    //private final ServicesService servicesService = new ServicesService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logger.info("{} Initializing history view...", PREFIX);

        setupTable();
        loadUsers();
        loadPredefinedDateFilters();
        loadServicesHistory();
        setupNewServiceButton();

        servicesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Double click delete
        servicesTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && !servicesTable.getSelectionModel().isEmpty()) {
                confirmAndDelete(servicesTable.getSelectionModel().getSelectedItem());
            }
        });

        logger.info("{} Initialized successfully.", PREFIX);
    }

    // =========================================================================
    // TABLE
    // =========================================================================

    private void setupTable() {
        colUser.setCellValueFactory(new PropertyValueFactory<>("userName"));
        colServiceType.setCellValueFactory(new PropertyValueFactory<>("serviceNames"));
        colPaymentMethod.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("totalFormatted"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colNotes.setCellValueFactory(new PropertyValueFactory<>("notes"));
    }

    private void loadServicesHistory() {
        List<ServiceHistoryDTO> services = servicesService.getServiceHistory();
        servicesTable.setItems(FXCollections.observableArrayList(services));
    }

    // =========================================================================
    // FILTERS
    // =========================================================================

    private void loadUsers() {
        var users = usersService.getAllUsers();
        filterUserComboBox.setItems(FXCollections.observableArrayList(users));
        filterUserComboBox.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(User user) { return user != null ? user.getName() : ""; }
            @Override public User fromString(String s) { return null; }
        });
    }

    private void loadPredefinedDateFilters() {
        dateFilterCombo.setItems(FXCollections.observableArrayList(
                "Today", "Yesterday", "Last 7 days", "This month", "Last month", "Custom..."
        ));

        dateFilterCombo.valueProperty().addListener((obs, old, selected) -> {
            if (selected == null) return;

            switch (selected) {
                case "Today" -> filterDatePicker.setValue(LocalDate.now());
                case "Yesterday" -> filterDatePicker.setValue(LocalDate.now().minusDays(1));
                case "Custom..." -> filterDatePicker.setDisable(false);
                default -> filterDatePicker.setDisable(true);
            }
        });
    }

    // =========================================================================
    // DELETE
    // =========================================================================

    private void confirmAndDelete(ServiceHistoryDTO dto) {
        if (dto == null) return;

        User activeUser = SessionManager.getActiveUser();
        String role = activeUser.getRole();

        boolean canDelete = "admin".equalsIgnoreCase(role)
                || ("user".equalsIgnoreCase(role) && dto.getDate().isEqual(LocalDate.now()));

        if (!canDelete) {
            AlertUtil.showError("No tienes permiso para eliminar este servicio.");
            return;
        }

        Alert alert1 = new Alert(Alert.AlertType.CONFIRMATION);
        alert1.setTitle("Confirmar eliminación");
        alert1.setHeaderText("¿Deseas eliminar este registro?");
        alert1.setContentText(
                "Usuario: " + dto.getUserName() +
                        "\nServicios: " + dto.getServiceNames() +
                        "\nPago: " + dto.getPaymentMethod() +
                        "\nTotal: " + dto.getTotalFormatted() +
                        "\nFecha: " + dto.getDate() +
                        "\nNotas: " + dto.getNotes()
        );

        ButtonType aceptar = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert1.getButtonTypes().setAll(aceptar, cancelar);

        alert1.showAndWait().ifPresent(resp -> {
            if (resp == aceptar) {
                confirmFinalDelete(dto);
            }
        });
    }

    private void confirmFinalDelete(ServiceHistoryDTO dto) {
        Alert alert2 = new Alert(Alert.AlertType.CONFIRMATION);
        alert2.setTitle("Confirmación final");
        alert2.setHeaderText("Esta acción no se puede deshacer.");
        alert2.setContentText("¿Confirmas la eliminación?");

        ButtonType aceptar = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert2.getButtonTypes().setAll(aceptar, cancelar);

        alert2.showAndWait().ifPresent(resp -> {
            if (resp == aceptar) {
                try {
                    servicesService.deleteServiceById(dto.getId());
                    loadServicesHistory();
                    AlertUtil.showInfo("Servicio eliminado con éxito.");
                } catch (Exception e) {
                    AlertUtil.showError("Error al eliminar:\n" + e.getMessage());
                }
            }
        });
    }

    private void setupNewServiceButton() {
        createServiceButton.setOnAction(event -> startNewServiceFlow());
    }

    private void startNewServiceFlow() {
        logger.info("{} Starting new service creation flow...", PREFIX);

        User active = SessionManager.getActiveUser();
        if (active == null) {
            AlertUtil.showError("No hay un usuario en sesión.");
            return;
        }

        // 🔥 Crear el nuevo carrito vacío
        CheckoutDTO cartDTO = new CheckoutDTO(active.getId());

        // Guardarlo en sesión
        SessionManager.setCurrentCartDTO(cartDTO);

        // 🔥 Navegar a la pantalla de creación
        WindowManager.setEmbeddedView(
                SessionManager.getMainBorderPane(),
                "center",
                "/app/barbman/core/view/embed-view/sale-create-view.fxml"
        );

        logger.info("{} Redirected to service-create-view.", PREFIX);
    }


}
