package app.barbman.core.controller.sales;

import app.barbman.core.dto.salecart.SaleCartDTO;
import app.barbman.core.dto.salecart.SaleCartItemDTO;
import app.barbman.core.model.sales.Sale;
import app.barbman.core.repositories.cashbox.closure.CashboxClosureRepository;
import app.barbman.core.repositories.cashbox.closure.CashboxClosureRepositoryImpl;
import app.barbman.core.repositories.cashbox.movement.CashboxMovementRepository;
import app.barbman.core.repositories.cashbox.movement.CashboxMovementRepositoryImpl;
import app.barbman.core.repositories.cashbox.opening.CashboxOpeningRepository;
import app.barbman.core.repositories.cashbox.opening.CashboxOpeningRepositoryImpl;
import app.barbman.core.repositories.expense.ExpenseRepository;
import app.barbman.core.repositories.expense.ExpenseRepositoryImpl;
import app.barbman.core.repositories.sales.SaleRepository;
import app.barbman.core.repositories.sales.SaleRepositoryImpl;
import app.barbman.core.repositories.sales.products.productheader.ProductHeaderRepository;
import app.barbman.core.repositories.sales.products.productheader.ProductHeaderRepositoryImpl;
import app.barbman.core.repositories.sales.products.productsaleitem.ProductSaleItemRepository;
import app.barbman.core.repositories.sales.products.productsaleitem.ProductSaleItemRepositoryImpl;
import app.barbman.core.repositories.sales.services.serviceheader.ServiceHeaderRepository;
import app.barbman.core.repositories.sales.services.serviceheader.ServiceHeaderRepositoryImpl;
import app.barbman.core.repositories.sales.services.serviceitems.ServiceItemRepository;
import app.barbman.core.repositories.sales.services.serviceitems.ServiceItemRepositoryImpl;
import app.barbman.core.service.cashbox.CashboxService;
import app.barbman.core.service.expenses.ExpensesService;
import app.barbman.core.service.sales.SalesService;
import app.barbman.core.service.sales.products.ProductHeaderService;
import app.barbman.core.service.sales.products.ProductItemService;
import app.barbman.core.service.sales.saleflow.SaleFlowService;
import app.barbman.core.service.sales.services.ServiceHeaderService;
import app.barbman.core.service.sales.services.ServiceItemService;
import app.barbman.core.util.NumberFormatterUtil;
import app.barbman.core.util.SessionManager;
import app.barbman.core.util.window.EmbeddedViewLoader;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.ResourceBundle;

public class SalePaymentViewController implements Initializable {

    private static final Logger logger =
            LogManager.getLogger(SalePaymentViewController.class);

    // =========================
    // FXML
    // =========================
    @FXML private ComboBox<String> clientComboBox;
    @FXML private Button addClientButton;
    @FXML private TextArea noteArea;

    @FXML private VBox summaryContainer;

    @FXML private Label totalLabel;
    @FXML private Label changeLabel;
    @FXML private TextField receivedAmountField;

    @FXML private ToggleButton cashToggle;
    @FXML private ToggleButton transferToggle;
    @FXML private ToggleButton posToggle;
    @FXML private ToggleButton qrToggle;

    @FXML private Button cancelButton;
    @FXML private Button confirmButton;

    private final SaleRepository saleRepository = new SaleRepositoryImpl();
    private final ServiceHeaderRepository serviceHeaderRepository = new ServiceHeaderRepositoryImpl();
    private final ServiceHeaderService serviceHeaderService = new ServiceHeaderService(serviceHeaderRepository);
    private final ServiceItemRepository serviceItemRepository = new ServiceItemRepositoryImpl();
    private final ServiceItemService serviceItemService = new ServiceItemService(serviceItemRepository);
    private final ProductHeaderRepository productHeaderRepository = new ProductHeaderRepositoryImpl();
    private final ProductHeaderService productHeaderService = new ProductHeaderService(productHeaderRepository);
    private final ProductSaleItemRepository productSaleItemRepository = new ProductSaleItemRepositoryImpl();
    private final ProductItemService productItemService = new ProductItemService(productSaleItemRepository);

    private final CashboxMovementRepository cashboxMovementRepository = new CashboxMovementRepositoryImpl();

    private final SaleFlowService saleFlowService =
            new SaleFlowService(
            saleRepository,
            serviceHeaderService,
            serviceItemService,
            productHeaderService,
            productItemService,
            cashboxMovementRepository
            );

    // =========================
    // STATE
    // =========================
    private SaleCartDTO cart;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        cart = SessionManager.getCurrentCartDTO();
        if (cart == null) {
            throw new IllegalStateException("No active cart found for payment");
        }

        setupClientCombo();
        setupPaymentMethod();
        loadSummary();
        setupButtons();

        totalLabel.setText(
                NumberFormatterUtil.format(cart.getTotal()) + " Gs"
        );

        logger.info("[SALE-PAYMENT] Payment view initialized");
    }

    // =========================
    // CLIENT
    // =========================
    private void setupClientCombo() {
        clientComboBox.getItems().clear();
        clientComboBox.getItems().add("Ninguno");
        clientComboBox.getSelectionModel().selectFirst();

        clientComboBox.setEditable(true);

        addClientButton.setOnAction(e ->
                logger.info("[SALE-PAYMENT] Add client clicked (pending)")
        );
    }

    // =========================
    // PAYMENT METHOD
    // =========================
    private void setupPaymentMethod() {

        ToggleGroup group = new ToggleGroup();
        cashToggle.setToggleGroup(group);
        transferToggle.setToggleGroup(group);
        posToggle.setToggleGroup(group);
        qrToggle.setToggleGroup(group);

        receivedAmountField.setDisable(true);

        group.selectedToggleProperty().addListener((obs, old, selected) -> {
            boolean isCash = selected == cashToggle;

            receivedAmountField.setDisable(!isCash);
            receivedAmountField.clear();
            changeLabel.setText("0 Gs");
        });

        receivedAmountField.textProperty().addListener((obs, old, val) -> {
            if (val == null || val.isBlank()) {
                changeLabel.setText("0 Gs");
                return;
            }

            String clean = val.replace(".", "").trim();
            if (!clean.matches("\\d+")) {
                receivedAmountField.setText(old);
                return;
            }

            double received = Double.parseDouble(clean);
            double change = received - cart.getTotal();

            changeLabel.setText(
                    NumberFormatterUtil.format(Math.max(change, 0)) + " Gs"
            );
        });
    }

    // =========================
    // SUMMARY
    // =========================
    private void loadSummary() {
        summaryContainer.getChildren().clear();

        for (SaleCartItemDTO item : cart.getCartItems()) {
            summaryContainer.getChildren().add(buildSummaryRow(item));
        }
    }

    private HBox buildSummaryRow(SaleCartItemDTO item) {

        Label name = new Label(item.getDisplayName());
        Label qty = new Label("x" + item.getQuantity());
        Label price = new Label(
                NumberFormatterUtil.format(item.getItemTotal()) + " Gs"
        );

        HBox row = new HBox(12, name, qty, price);
        return row;
    }

    private int getSelectedPaymentMethod() {

        if (cashToggle.isSelected()) {
            return 0;
        }
        if (transferToggle.isSelected()) {
            return 1;
        }
        if (posToggle.isSelected()) {
            return 2;
        }
        if (qrToggle.isSelected()) {
            return 3;
        }

        throw new IllegalStateException("No payment method selected");
    }
    private Integer getSelectedClientId() {
        return null;
    }

    private boolean isPaymentValid() {

        // 1. Método de pago seleccionado
        if (!cashToggle.isSelected()
                && !transferToggle.isSelected()
                && !posToggle.isSelected()
                && !qrToggle.isSelected()) {

            new Alert(Alert.AlertType.ERROR,
                    "Seleccione un método de pago").show();
            return false;
        }

        // 2. Si es efectivo, validar monto recibido
        if (cashToggle.isSelected()) {

            String raw = receivedAmountField.getText();
            if (raw == null || raw.isBlank()) {
                new Alert(Alert.AlertType.ERROR,
                        "Ingrese el monto recibido").show();
                return false;
            }

            String clean = raw.replace(".", "").trim();
            if (!clean.matches("\\d+")) {
                new Alert(Alert.AlertType.ERROR,
                        "Monto inválido").show();
                return false;
            }

            double received = Double.parseDouble(clean);
            if (received < cart.getTotal()) {
                new Alert(Alert.AlertType.ERROR,
                        "El monto recibido es menor al total").show();
                return false;
            }
        }

        return true;
    }

    // =========================
    // ACTIONS
    // =========================
    private void setupButtons() {

        cancelButton.setOnAction(e -> {
            BorderPane root = SessionManager.getMainBorderPane();

            EmbeddedViewLoader.load(
                    root,
                    EmbeddedViewLoader.Position.CENTER,
                    "/app/barbman/core/view/embed-view/sale-create-view.fxml"
            );
        });

        confirmButton.setOnAction(e -> {
            logger.info("[SALE-PAYMENT] Confirm payment pressed");
            logger.info("Payment method selected");
            logger.info("Client: {}", clientComboBox.getValue());
            logger.info("Note: {}", noteArea.getText());

            if (!isPaymentValid()) return;

            cart.setPaymentMethod(getSelectedPaymentMethod());
            cart.setClientId(getSelectedClientId());
            cart.setNotes(noteArea.getText());

            // Save sale on database
            Sale sale = saleFlowService.completeSale(cart);

            // Save on session
            SessionManager.setLastSale(sale);

            // Load result view
            EmbeddedViewLoader.load(
                    SessionManager.getMainBorderPane(),
                    EmbeddedViewLoader.Position.CENTER,
                    "/app/barbman/core/view/embed-view/sale-result-view.fxml"
            );
        });
    }
}
