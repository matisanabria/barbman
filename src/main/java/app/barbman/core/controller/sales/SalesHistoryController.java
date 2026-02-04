package app.barbman.core.controller.sales;

import app.barbman.core.dto.history.SaleHistoryDTO;
import app.barbman.core.repositories.cashbox.movement.CashboxMovementRepositoryImpl;
import app.barbman.core.repositories.sales.SaleRepositoryImpl;
import app.barbman.core.repositories.sales.products.product.ProductRepositoryImpl;
import app.barbman.core.repositories.sales.products.productheader.ProductHeaderRepositoryImpl;
import app.barbman.core.repositories.sales.products.productsaleitem.ProductSaleItemRepositoryImpl;
import app.barbman.core.repositories.sales.services.servicedefinition.ServiceDefinitionRepositoryImpl;
import app.barbman.core.repositories.sales.services.serviceheader.ServiceHeaderRepositoryImpl;
import app.barbman.core.repositories.sales.services.serviceitems.ServiceItemRepositoryImpl;
import app.barbman.core.service.sales.SalesHistoryService;
import app.barbman.core.util.AlertUtil;
import app.barbman.core.util.NumberFormatterUtil;
import app.barbman.core.util.SessionManager;
import app.barbman.core.util.legacy.LegacySaleRepository;
import app.barbman.core.util.window.WindowManager;
import app.barbman.core.util.window.WindowRequest;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for sales history view.
 * Shows all sales with filters and allows viewing details.
 */
public class SalesHistoryController implements Initializable {

    private static final Logger logger = LogManager.getLogger(SalesHistoryController.class);
    private static final String PREFIX = "[SALES-HISTORY]";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ============================================================
    // FXML
    // ============================================================

    @FXML private DatePicker dateFromPicker;
    @FXML private DatePicker dateToPicker;
    @FXML private Button btnFilter;
    @FXML private Button btnDelete;
    @FXML private TableView<SaleHistoryDTO> salesTable;
    @FXML private TableColumn<SaleHistoryDTO, String> colDate;
    @FXML private TableColumn<SaleHistoryDTO, String> colUser;
    @FXML private TableColumn<SaleHistoryDTO, String> colClient;
    @FXML private TableColumn<SaleHistoryDTO, String> colTotal;
    @FXML private TableColumn<SaleHistoryDTO, String> colPaymentMethod;

    // ============================================================
    // SERVICES
    // ============================================================

    private final SalesHistoryService historyService;
    private ObservableList<SaleHistoryDTO> salesData;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public SalesHistoryController() {
        this.historyService = new SalesHistoryService(
                new SaleRepositoryImpl(),
                new ServiceHeaderRepositoryImpl(),
                new ServiceItemRepositoryImpl(),
                new ProductHeaderRepositoryImpl(),
                new ProductSaleItemRepositoryImpl(),
                new ServiceDefinitionRepositoryImpl(),
                new ProductRepositoryImpl(),
                new CashboxMovementRepositoryImpl(),
                new LegacySaleRepository()
        );
    }

    // ============================================================
    // INIT
    // ============================================================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("{} Initializing sales history view", PREFIX);

        setupTable();
        setupDatePickers();
        setupButtons();
        loadInitialData();

        logger.info("{} Sales history view initialized", PREFIX);
    }

    private void setupTable() {
        // Configure columns
        colDate.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDate().format(DATE_FORMATTER)));

        colUser.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getUserName()));

        colClient.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getClientName() != null
                        ? data.getValue().getClientName()
                        : "Cliente casual"));

        colTotal.setCellValueFactory(data ->
                new SimpleStringProperty(NumberFormatterUtil.format(data.getValue().getTotal()) + " Gs"));

        colPaymentMethod.setCellValueFactory(data ->
                new SimpleStringProperty(translatePaymentMethod(data.getValue().getPaymentMethod())));

        // Alternating row colors by DATE
        salesTable.setRowFactory(tv -> {
            TableRow<SaleHistoryDTO> row = new TableRow<>() {
                @Override
                protected void updateItem(SaleHistoryDTO item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setStyle("");
                    } else {
                        // Get index in the filtered list
                        int index = getIndex();

                        // Determine color based on date grouping
                        if (index > 0) {
                            SaleHistoryDTO prevItem = getTableView().getItems().get(index - 1);
                            if (!item.getDate().equals(prevItem.getDate())) {
                                // Date changed, toggle color
                                boolean isPrevLight = prevItem.getDate().toEpochDay() % 2 == 0;
                                if (isPrevLight) {
                                    getStyleClass().removeAll("history-row-light");
                                    getStyleClass().add("history-row-dark");
                                } else {
                                    getStyleClass().removeAll("history-row-dark");
                                    getStyleClass().add("history-row-light");
                                }
                            } else {
                                // Same date as previous, keep same color
                                if (prevItem.getDate().toEpochDay() % 2 == 0) {
                                    getStyleClass().removeAll("history-row-dark");
                                    getStyleClass().add("history-row-light");
                                } else {
                                    getStyleClass().removeAll("history-row-light");
                                    getStyleClass().add("history-row-dark");
                                }
                            }
                        } else {
                            // First row
                            if (item.getDate().toEpochDay() % 2 == 0) {
                                getStyleClass().add("history-row-light");
                            } else {
                                getStyleClass().add("history-row-dark");
                            }
                        }
                    }
                }
            };

            // Double-click to view detail
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    SaleHistoryDTO sale = row.getItem();
                    openSaleDetail(sale.getSaleId());
                }
            });

            return row;
        });

        // Disable column reordering
        salesTable.getColumns().forEach(col -> col.setReorderable(false));
    }

    private void setupDatePickers() {
        // Default: last 30 days
        dateToPicker.setValue(LocalDate.now());
        dateFromPicker.setValue(LocalDate.now().minusDays(30));
    }

    private void setupButtons() {
        // Filter button
        btnFilter.setOnAction(e -> loadSalesData());

        // Delete button (only visible for admin)
        var user = SessionManager.getActiveUser();
        if (user != null && "admin".equals(user.getRole())) {
            btnDelete.setVisible(true);
            btnDelete.setOnAction(e -> handleDeleteSale());
        } else {
            btnDelete.setVisible(false);
        }
    }

    private void loadInitialData() {
        loadSalesData();
    }

    // ============================================================
    // DATA LOADING
    // ============================================================

    private void loadSalesData() {
        try {
            LocalDate from = dateFromPicker.getValue();
            LocalDate to = dateToPicker.getValue();

            if (from == null || to == null) {
                AlertUtil.showWarning("Validación", "Debes seleccionar ambas fechas.");
                return;
            }

            if (from.isAfter(to)) {
                AlertUtil.showWarning("Validación", "La fecha 'Desde' no puede ser posterior a 'Hasta'.");
                return;
            }

            logger.info("{} Loading sales from {} to {}", PREFIX, from, to);

            List<SaleHistoryDTO> sales = historyService.getSalesHistory(from, to);

            // Invertir para mostrar más recientes arriba
            Collections.reverse(sales);
            salesData = FXCollections.observableArrayList(sales);
            salesTable.setItems(salesData);

            logger.info("{} Loaded {} sales", PREFIX, sales.size());

        } catch (Exception e) {
            logger.error("{} Error loading sales data", PREFIX, e);
            AlertUtil.showError("Error", "No se pudieron cargar las ventas: " + e.getMessage());
        }
    }

    // ============================================================
    // ACTIONS
    // ============================================================

    private void openSaleDetail(int saleId) {
        logger.info("{} Opening detail for sale ID={}", PREFIX, saleId);


        try {
            SaleDetailController controller = (SaleDetailController) WindowManager.openModal(
                    WindowRequest.builder()
                            .fxml("/app/barbman/core/view/embed-view/sale-detail-view.fxml")
                            .title("Detalle de Venta #" + saleId)
                            .css("/app/barbman/core/style/embed-views/sale-detail.css")
                            .owner((Stage) salesTable.getScene().getWindow())
                            .modal(true)
                            .resizable(false)
                            .returnController(true)
                            .build()
            );

            if (controller != null) {
                controller.loadSaleDetail(saleId);
            }

        } catch (Exception e) {
            logger.error("{} Error opening sale detail", PREFIX, e);
            AlertUtil.showError("Error", "No se pudo abrir el detalle de la venta.");
        }
    }

    private void handleDeleteSale() {
        SaleHistoryDTO selected = salesTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertUtil.showWarning("Selección", "Debes seleccionar una venta para eliminar.");
            return;
        }

        // Confirmation
        boolean confirmed = showConfirmation(
                "Confirmar eliminación",
                String.format("¿Estás seguro de eliminar la venta #%d?\n\nEsta acción no se puede deshacer.",
                        selected.getSaleId())
        );

        if (!confirmed) {
            return;
        }

        try {
            historyService.deleteSale(selected.getSaleId());

            logger.info("{} Sale deleted: ID={}", PREFIX, selected.getSaleId());

            AlertUtil.showInfo(
                    "Venta eliminada",
                    "La venta fue eliminada exitosamente."
            );

            // Reload data
            loadSalesData();

        } catch (Exception e) {
            logger.error("{} Error deleting sale", PREFIX, e);
            AlertUtil.showError("Error", "No se pudo eliminar la venta: " + e.getMessage());
        }
    }

    // ============================================================
    // HELPERS
    // ============================================================

    /**
     * Shows a confirmation dialog.
     * Returns true if user confirms, false otherwise.
     */
    private boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Styling (optional - matches your theme)
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/app/barbman/core/style/main.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("alert-dialog");

        var result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    private String translatePaymentMethod(String method) {
        return switch (method) {
            case "cash" -> "Efectivo";
            case "transfer" -> "Transferencia";
            case "card" -> "Tarjeta";
            case "qr" -> "QR";
            default -> method;
        };
    }
}