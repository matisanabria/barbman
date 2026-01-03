package app.barbman.core.controller.sales;

import app.barbman.core.dto.SaleItemSummaryDTO;
import app.barbman.core.dto.SaleSummaryDTO;
import app.barbman.core.service.sales.SaleQueryService;
import app.barbman.core.util.NumberFormatterUtil;
import app.barbman.core.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import app.barbman.core.util.window.EmbeddedViewLoader;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.util.ResourceBundle;

public class SaleResultViewController implements Initializable {

    private static final Logger logger =
            LogManager.getLogger(SaleResultViewController.class);

    // =====================
    // FXML
    // =====================
    @FXML private Label clientNameLabel;
    @FXML private Label clientDocLabel;
    @FXML private Label totalLabel;
    @FXML private Label paymentMethodLabel;
    @FXML private VBox itemsContainer;
    @FXML private Button closeButton;

    // =====================
    // SERVICE
    // =====================
    private final SaleQueryService saleQueryService =
            new SaleQueryService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        var sale = SessionManager.getLastSale();
        if (sale == null) {
            throw new IllegalStateException("No sale in session");
        }

        loadSale(sale.getId());

        closeButton.setOnAction(e -> closeView());
    }

    // =====================
    // LOAD
    // =====================
    private void loadSale(int saleId) {

        SaleSummaryDTO dto =
                saleQueryService.getSaleSummary(saleId);

        clientNameLabel.setText(dto.getClientName());
        clientDocLabel.setText(dto.getClientDocument());

        paymentMethodLabel.setText(dto.getPaymentMethod());

        totalLabel.setText(
                NumberFormatterUtil.format(dto.getTotal()) + " Gs"
        );

        renderItems(dto);

        logger.info("[SALE-RESULT] Loaded sale {}", saleId);
    }

    // =====================
    // ITEMS
    // =====================
    private void renderItems(SaleSummaryDTO dto) {
        itemsContainer.getChildren().clear();

        for (SaleItemSummaryDTO item : dto.getItems()) {
            itemsContainer.getChildren()
                    .add(buildItemRow(item));
        }
    }

    private HBox buildItemRow(SaleItemSummaryDTO item) {

        HBox row = new HBox(24);
        row.setPadding(new Insets(4, 0, 4, 0));

        Label name = new Label(item.getName());
        name.setPrefWidth(220);

        Label qty = new Label("x" + item.getQuantity());
        qty.setPrefWidth(80);

        Label price = new Label(
                NumberFormatterUtil.format(item.getUnitPrice())
        );
        price.setPrefWidth(100);

        Label subtotal = new Label(
                NumberFormatterUtil.format(item.getSubtotal())
        );
        subtotal.setPrefWidth(100);

        row.getChildren().addAll(name, qty, price, subtotal);
        return row;
    }

    // =====================
    // ACTIONS
    // =====================
    private void closeView() {

        BorderPane root = SessionManager.getMainBorderPane();

        EmbeddedViewLoader.load(
                root,
                EmbeddedViewLoader.Position.CENTER,
                "/app/barbman/core/view/embed-view/sale-create-view.fxml"
        );

        logger.info("[SALE-RESULT] Back to sale create view");
    }
}
