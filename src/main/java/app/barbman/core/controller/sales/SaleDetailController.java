package app.barbman.core.controller.sales;

import app.barbman.core.dto.history.SaleDetailDTO;
import app.barbman.core.dto.history.SaleItemDTO;
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
import app.barbman.core.util.NumberToWordsUtil;
import app.barbman.core.util.legacy.LegacySaleRepository;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.format.DateTimeFormatter;

/**
 * Controller for sale detail modal.
 * Shows complete information about a sale including all items.
 */
public class SaleDetailController {

    private static final Logger logger = LogManager.getLogger(SaleDetailController.class);
    private static final String PREFIX = "[SALE-DETAIL]";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ============================================================
    // FXML
    // ============================================================

    @FXML private Label saleNumberLabel;
    @FXML private Label saleDateLabel;
    @FXML private Label clientNameLabel;
    @FXML private Label clientDocLabel;
    @FXML private VBox itemsContainer;
    @FXML private Label subtotalLabel;
    @FXML private Label ivaLabel;
    @FXML private Label totalLabel;
    @FXML private Label totalInWordsLabel;
    @FXML private Label paymentMethodLabel;

    // ============================================================
    // SERVICES
    // ============================================================

    private final SalesHistoryService historyService;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public SaleDetailController() {
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
    // PUBLIC API
    // ============================================================

    /**
     * Loads and displays the detail of a sale.
     */
    public void loadSaleDetail(int saleId) {
        logger.info("{} Loading detail for sale ID={}", PREFIX, saleId);

        try {
            SaleDetailDTO detail = historyService.getSaleDetail(saleId);

            if (detail == null) {
                AlertUtil.showInfo("Registro Histórico",
                        "La venta #" + saleId + " corresponde al sistema anterior (Beta).\n" +
                                "No hay detalles de productos o servicios disponibles para visualización.");
                closeModal();
                return;
            }

            displaySaleDetail(detail, saleId);

            logger.info("{} Sale detail loaded successfully", PREFIX);

        } catch (Exception e) {
            logger.error("{} Error loading sale detail", PREFIX, e);
            AlertUtil.showError("Error", "No se pudo cargar el detalle de la venta: " + e.getMessage());
            closeModal();
        }
    }

    // ============================================================
    // DISPLAY
    // ============================================================

    private void displaySaleDetail(SaleDetailDTO detail, int saleId) {
        // Header
        saleNumberLabel.setText(String.format("%03d", saleId));
        saleDateLabel.setText(detail.getDate().format(DATE_FORMATTER));

        // Client
        clientNameLabel.setText(detail.getClientName() != null
                ? detail.getClientName()
                : "Cliente casual");
        clientDocLabel.setText("-");

        // Items
        itemsContainer.getChildren().clear();

        // Add service items
        for (SaleItemDTO item : detail.getServiceItems()) {
            itemsContainer.getChildren().add(createItemRow(item));
        }

        // Add product items
        for (SaleItemDTO item : detail.getProductItems()) {
            itemsContainer.getChildren().add(createItemRow(item));
        }

        // Totals
        double total = detail.getTotal();
        double iva = total - (total / 1.1); // Assuming 10% IVA

        subtotalLabel.setText(NumberFormatterUtil.format(total) + " Gs");
        ivaLabel.setText(NumberFormatterUtil.format(iva) + " Gs");
        totalLabel.setText(NumberFormatterUtil.format(total) + " Gs");

        // Total in words
        totalInWordsLabel.setText(numberToWords(total));

        // Payment method
        paymentMethodLabel.setText(translatePaymentMethod(detail.getPaymentMethod()));
    }

    /**
     * Creates a row for an item in the sale.
     */
    private HBox createItemRow(SaleItemDTO item) {
        HBox row = new HBox(12);
        row.getStyleClass().add("sale-detail-item-row");
        row.setPadding(new Insets(8, 0, 8, 0));

        // Item name
        Label nameLabel = new Label(item.getName());
        nameLabel.getStyleClass().add("sale-detail-item-name");
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(nameLabel, javafx.scene.layout.Priority.ALWAYS);

        // Quantity
        Label qtyLabel = new Label(String.valueOf(item.getQuantity()));
        qtyLabel.getStyleClass().add("sale-detail-item-qty");
        qtyLabel.setPrefWidth(60);
        qtyLabel.setAlignment(javafx.geometry.Pos.CENTER);

        // Unit price
        Label priceLabel = new Label(NumberFormatterUtil.format(item.getUnitPrice()) + " Gs");
        priceLabel.getStyleClass().add("sale-detail-item-price");
        priceLabel.setPrefWidth(100);
        priceLabel.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        // Subtotal
        Label subtotalLabel = new Label(NumberFormatterUtil.format(item.getTotal()) + " Gs");
        subtotalLabel.getStyleClass().add("sale-detail-item-subtotal");
        subtotalLabel.setPrefWidth(100);
        subtotalLabel.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        row.getChildren().addAll(nameLabel, qtyLabel, priceLabel, subtotalLabel);

        return row;
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private String translatePaymentMethod(String method) {
        return switch (method) {
            case "cash" -> "Efectivo";
            case "transfer" -> "Transferencia";
            case "card" -> "Tarjeta";
            case "qr" -> "QR";
            default -> method;
        };
    }

    /**
     * Converts a number to words in Spanish (Guaraníes).
     */
    private String numberToWords(double amount) {
        return NumberToWordsUtil.convert(amount);
    }

    @FXML
    private void onClose() {
        logger.info("{} Closing sale detail modal", PREFIX);
        closeModal();
    }

    private void closeModal() {
        Stage stage = (Stage) saleNumberLabel.getScene().getWindow();
        stage.close();
    }
}