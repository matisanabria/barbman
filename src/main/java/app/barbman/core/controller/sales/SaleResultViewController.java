package app.barbman.core.controller.sales;

import app.barbman.core.dto.SaleItemSummaryDTO;
import app.barbman.core.dto.SaleSummaryDTO;
import app.barbman.core.service.sales.SaleQueryService;
import app.barbman.core.util.NumberFormatterUtil;
import app.barbman.core.util.NumberToWordsUtil;
import app.barbman.core.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import app.barbman.core.util.window.EmbeddedViewLoader;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class SaleResultViewController implements Initializable {

    private static final Logger logger =
            LogManager.getLogger(SaleResultViewController.class);

    private static final double IVA_RATE = 0.10;

    @FXML private Label saleNumberLabel;
    @FXML private Label saleDateLabel;
    @FXML private Label clientNameLabel;
    @FXML private Label clientDocLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label ivaLabel;
    @FXML private Label totalLabel;
    @FXML private Label totalInWordsLabel;
    @FXML private Label paymentMethodLabel;
    @FXML private VBox itemsContainer;
    @FXML private Button newSaleButton;

    private final SaleQueryService saleQueryService =
            new SaleQueryService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        var sale = SessionManager.getLastSale();
        if (sale == null) {
            throw new IllegalStateException("No sale in session");
        }

        loadSale(sale.getId());
        setupButtons();

        logger.info("[SALE-RESULT] View initialized for sale {}", sale.getId());
    }

    private void loadSale(int saleId) {

        SaleSummaryDTO dto = saleQueryService.getSaleSummary(saleId);

        saleNumberLabel.setText(String.format("%03d", dto.getSaleId()));

        saleDateLabel.setText(
                LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm")
                )
        );

        clientNameLabel.setText(
                dto.getClientName() != null && !dto.getClientName().isBlank()
                        ? dto.getClientName()
                        : "Cliente casual"
        );

        clientDocLabel.setText(
                dto.getClientDocument() != null && !dto.getClientDocument().isBlank()
                        ? dto.getClientDocument()
                        : "-"
        );

        // Traducir payment method
        String paymentKey = dto.getPaymentMethod();
        String paymentDisplay;

        switch (paymentKey) {
            case "cash" -> paymentDisplay = "Efectivo";
            case "transfer" -> paymentDisplay = "Transferencia";
            case "card" -> paymentDisplay = "Tarjeta";
            case "qr" -> paymentDisplay = "QR";
            default -> paymentDisplay = "Desconocido";
        }

        paymentMethodLabel.setText(paymentDisplay);

        double total = dto.getTotal();
        double iva = calculateIva(total);

        subtotalLabel.setText(NumberFormatterUtil.format(total) + " Gs");
        ivaLabel.setText(NumberFormatterUtil.format(iva) + " Gs");
        totalLabel.setText(NumberFormatterUtil.format(total) + " Gs");

        totalInWordsLabel.setText(NumberToWordsUtil.convert(total));

        renderItems(dto);

        logger.info("[SALE-RESULT] Loaded sale {}", saleId);
    }

    private void renderItems(SaleSummaryDTO dto) {
        itemsContainer.getChildren().clear();

        for (SaleItemSummaryDTO item : dto.getItems()) {
            itemsContainer.getChildren().add(buildItemRow(item));
        }
    }

    private HBox buildItemRow(SaleItemSummaryDTO item) {

        Label name = new Label(item.getName());
        name.getStyleClass().add("sale-result-item-name");
        name.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(name, javafx.scene.layout.Priority.ALWAYS);

        Label qty = new Label("x" + item.getQuantity());
        qty.getStyleClass().add("sale-result-item-qty");
        qty.setPrefWidth(60);
        qty.setAlignment(Pos.CENTER);

        Label price = new Label(NumberFormatterUtil.format(item.getUnitPrice()) + " Gs");
        price.getStyleClass().add("sale-result-item-price");
        price.setPrefWidth(100);
        price.setAlignment(Pos.CENTER_RIGHT);

        Label subtotal = new Label(NumberFormatterUtil.format(item.getSubtotal()) + " Gs");
        subtotal.getStyleClass().add("sale-result-item-subtotal");
        subtotal.setPrefWidth(100);
        subtotal.setAlignment(Pos.CENTER_RIGHT);

        HBox row = new HBox(12, name, qty, price, subtotal);
        row.getStyleClass().add("sale-result-item-row");
        row.setAlignment(Pos.CENTER_LEFT);

        return row;
    }

    private double calculateIva(double total) {
        return total * IVA_RATE;
    }

    private String numberToWords(double amount) {
        return NumberToWordsUtil.convert(amount);
    }

    private void setupButtons() {
        newSaleButton.setOnAction(e -> {
            BorderPane root = SessionManager.getMainBorderPane();

            EmbeddedViewLoader.load(
                    root,
                    EmbeddedViewLoader.Position.CENTER,
                    "/app/barbman/core/view/embed-view/sale-create-view.fxml",
                    "/app/barbman/core/style/embed-views/sales-view.css"
            );

            logger.info("[SALE-RESULT] Back to sale create view");
        });
    }
}