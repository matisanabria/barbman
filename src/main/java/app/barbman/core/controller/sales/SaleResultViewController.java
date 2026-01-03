package app.barbman.core.controller.sales;

import app.barbman.core.dto.salecart.SaleCartItemDTO;
import app.barbman.core.model.PaymentMethod;
import app.barbman.core.model.human.Client;
import app.barbman.core.model.sales.Sale;
import app.barbman.core.repositories.client.ClientRepositoryImpl;
import app.barbman.core.repositories.paymentmethod.PaymentMethodRepositoryImpl;
import app.barbman.core.repositories.sales.SaleRepository;
import app.barbman.core.repositories.sales.SaleRepositoryImpl;
import app.barbman.core.repositories.sales.products.productsaleitem.ProductSaleItemRepository;
import app.barbman.core.repositories.sales.products.productsaleitem.ProductSaleItemRepositoryImpl;
import app.barbman.core.repositories.sales.services.serviceitems.ServiceItemRepository;
import app.barbman.core.repositories.sales.services.serviceitems.ServiceItemRepositoryImpl;
import app.barbman.core.service.clients.ClientService;
import app.barbman.core.service.paymentmethods.PaymentMethodsService;
import app.barbman.core.service.sales.products.ProductItemService;
import app.barbman.core.service.sales.services.ServiceItemService;
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

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class SaleResultViewController implements Initializable {

    private static final Logger logger =
            LogManager.getLogger(SaleResultViewController.class);

    // =====================
    // FXML
    // =====================
    @FXML private Label subtitleLabel;

    @FXML private Label clientNameLabel;
    @FXML private Label clientDocLabel;

    @FXML private VBox itemsContainer;

    @FXML private Label totalLabel;
    @FXML private Label paymentMethodLabel;

    @FXML private Button deleteSaleButton;
    @FXML private Button closeButton;

    // =====================
    // STATE (TEMPORAL)
    // =====================
    private int saleId;

    private final ClientService clientService =
            new ClientService(new ClientRepositoryImpl());

    private final PaymentMethodsService paymentMethodsService =
            new PaymentMethodsService(new PaymentMethodRepositoryImpl());

    // (si ya los tenés)
    private final ServiceItemRepository serviceItemRepository = new ServiceItemRepositoryImpl();
    private final ServiceItemService serviceItemService = new ServiceItemService(serviceItemRepository);
    private final ProductSaleItemRepository productSaleItemRepository = new ProductSaleItemRepositoryImpl();
    private final ProductItemService productItemService = new ProductItemService(productSaleItemRepository);
    private final SaleRepository saleRepository = new SaleRepositoryImpl();


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        logger.info("[SALE-RESULT] View initialized");

        Sale sale = SessionManager.getLastSale();
        if (sale == null) {
            throw new IllegalStateException("No sale found in session");
        }
        this.saleId = sale.getId();
        loadSaleById(saleId);

        // Acciones
        closeButton.setOnAction(e -> closeView());
        deleteSaleButton.setOnAction(e -> requestDeleteSale());

        // Por ahora deshabilitamos delete real
        deleteSaleButton.setDisable(true);
    }

    // ==================================================
    // PUBLIC API (la vista recibe datos desde afuera)
    // ==================================================

    private void loadSaleById(int saleId) {

        Sale sale = saleRepository.findById(saleId);

        totalLabel.setText(
                NumberFormatterUtil.format(sale.getTotal()) + " Gs"
        );

        // Payment method (SIEMPRE por ID)
        PaymentMethod pm =
                paymentMethodsService.getPaymentMethodById(
                        sale.getPaymentMethodId()
                );
        paymentMethodLabel.setText(pm.getName());

        // Client (opcional)
        if (sale.getClientId() != null) {
            Client c = clientService.findById(sale.getClientId());
            clientNameLabel.setText(c.getName());
            clientDocLabel.setText(c.getDocument());
        } else {
            clientNameLabel.setText("Cliente casual");
            clientDocLabel.setText("-");
        }

        // Items (services + products)
        renderItemsForSale(saleId);

        logger.info("[SALE-RESULT] Loaded sale {}", saleId);
    }



    // ==================================================
    // UI BUILDERS
    // ==================================================

    private void renderItems(List<SaleCartItemDTO> items) {
        itemsContainer.getChildren().clear();

        for (SaleCartItemDTO item : items) {
            itemsContainer.getChildren().add(buildItemRow(item));
        }
    }

    private HBox buildItemRow(SaleCartItemDTO item) {

        HBox row = new HBox(24);
        row.setPadding(new Insets(4, 0, 4, 0));

        Label name = new Label(item.getDisplayName());
        name.setPrefWidth(220);

        Label qty = new Label("x" + item.getQuantity());
        qty.setPrefWidth(80);

        Label price = new Label(
                NumberFormatterUtil.format(item.getUnitPrice())
        );
        price.setPrefWidth(100);

        double subtotal = item.getUnitPrice() * item.getQuantity();
        Label subtotalLabel = new Label(
                NumberFormatterUtil.format(subtotal)
        );
        subtotalLabel.setPrefWidth(100);

        row.getChildren().addAll(name, qty, price, subtotalLabel);
        return row;
    }

    // ==================================================
    // ACTIONS
    // ==================================================

    private void closeView() {
        logger.info("[SALE-RESULT] Closing result view");
        closeButton.getScene().getWindow().hide();
    }

    private void requestDeleteSale() {
        logger.warn("[SALE-RESULT] Delete requested for sale {}", saleId);

        // 🚧 FUTURO:
        // - pedir PIN admin
        // - confirmar
        // - llamar SaleFlowService.deleteSale(...)
    }
    private void renderItemsForSale(int saleId) {
        itemsContainer.getChildren().clear();

        serviceItemService.findBySaleId(saleId)
                .forEach(item ->
                        itemsContainer.getChildren()
                                .add(buildServiceRow(item))
                );

        productItemService.findBySaleId(saleId)
                .forEach(item ->
                        itemsContainer.getChildren()
                                .add(buildProductRow(item))
                );
    }

}
