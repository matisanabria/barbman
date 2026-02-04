package app.barbman.core.controller.sales;

import app.barbman.core.controller.QuickAddClientModalController;
import app.barbman.core.dto.salecart.SaleCartDTO;
import app.barbman.core.dto.salecart.SaleCartItemDTO;
import app.barbman.core.model.human.Client;
import app.barbman.core.model.sales.Sale;
import app.barbman.core.repositories.cashbox.movement.CashboxMovementRepository;
import app.barbman.core.repositories.cashbox.movement.CashboxMovementRepositoryImpl;
import app.barbman.core.repositories.client.ClientRepositoryImpl;
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
import app.barbman.core.service.clients.ClientService;
import app.barbman.core.service.sales.products.ProductHeaderService;
import app.barbman.core.service.sales.products.ProductItemService;
import app.barbman.core.service.sales.saleflow.SaleFlowService;
import app.barbman.core.service.sales.services.ServiceHeaderService;
import app.barbman.core.service.sales.services.ServiceItemService;
import app.barbman.core.util.AlertUtil;
import app.barbman.core.util.NumberFormatterUtil;
import app.barbman.core.util.SessionManager;
import app.barbman.core.util.window.EmbeddedViewLoader;
import app.barbman.core.util.window.WindowManager;
import app.barbman.core.util.window.WindowRequest;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class SalePaymentViewController implements Initializable {

    private static final Logger logger =
            LogManager.getLogger(SalePaymentViewController.class);

    // ============================================================
    // FXML COMPONENTS
    // ============================================================

    @FXML private ComboBox<String> clientComboBox;
    @FXML private Button addClientButton;
    @FXML private TextArea noteArea;
    @FXML private Label clientInfoLabel;

    @FXML private VBox clientInfoContainer;
    @FXML private Label clientNameLabel;
    @FXML private Label clientDocumentLabel;
    @FXML private Label clientPhoneLabel;
    @FXML private Label clientEmailLabel;

    @FXML private VBox summaryContainer;

    @FXML private Label totalLabel;
    @FXML private Label changeLabel;
    @FXML private TextField receivedAmountField;

    @FXML private ToggleButton cashToggle;
    @FXML private ToggleButton transferToggle;
    @FXML private ToggleButton cardToggle;
    @FXML private ToggleButton qrToggle;

    @FXML private Button cancelButton;
    @FXML private Button confirmButton;

    // ============================================================
    // SERVICES
    // ============================================================

    private final ClientService clientService = new ClientService(new ClientRepositoryImpl());
    private final SaleFlowService saleFlowService = new SaleFlowService(
            new SaleRepositoryImpl(),
            new ServiceHeaderService(new ServiceHeaderRepositoryImpl()),
            new ServiceItemService(new ServiceItemRepositoryImpl()),
            new ProductHeaderService(new ProductHeaderRepositoryImpl()),
            new ProductItemService(new ProductSaleItemRepositoryImpl()),
            new CashboxMovementRepositoryImpl()
    );

    // ============================================================
    // STATE
    // ============================================================

    private SaleCartDTO cart;
    private Map<String, Integer> clientMap = new HashMap<>(); // nombre -> id


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
        loadClientsIntoCombo();

        // Permitir busqueda por nombre
        clientComboBox.setEditable(true);

        // Listener para mostrar info del cliente
        clientComboBox.getSelectionModel().selectedItemProperty().addListener((obs, old, newValue) -> {
            updateClientInfo(newValue);
        });

        addClientButton.setOnAction(e -> openQuickAddClientModal());
    }

    private void loadClientsIntoCombo() {
        clientComboBox.getItems().clear();
        clientMap.clear();

        clientComboBox.getItems().add("Ninguno");
        clientMap.put("Ninguno", null);

        // Cargar clientes activos
        clientService.findAll().stream()
                .filter(Client::isActive)
                .forEach(client -> {
                    clientComboBox.getItems().add(client.getName());
                    clientMap.put(client.getName(), client.getId());
                });

        clientComboBox.getSelectionModel().selectFirst();
    }

    private void openQuickAddClientModal() {
        try {
            logger.info("[SALE-PAYMENT] Opening quick add client modal");

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/app/barbman/core/view/quick-add-client-modal.fxml")
            );

            VBox modalContent = loader.load();
            QuickAddClientModalController controller = loader.getController();

            // Set callback BEFORE opening modal
            controller.setOnClientCreated(createdClient -> {
                logger.info("[SALE-PAYMENT] Client created via callback: {}", createdClient.getName());

                // Reload combo
                loadClientsIntoCombo();

                // Select the new client
                clientComboBox.getSelectionModel().select(createdClient.getName());
            });

            // Open modal manually
            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner((Stage) clientComboBox.getScene().getWindow());
            modalStage.setTitle("Agregar Cliente");
            modalStage.setResizable(false);

            Scene scene = new Scene(modalContent);
            scene.getStylesheets().add(
                    getClass().getResource("/app/barbman/core/style/quick-add-client.css").toExternalForm()
            );

            modalStage.setScene(scene);
            modalStage.showAndWait();

        } catch (Exception e) {
            logger.error("[SALE-PAYMENT] Error opening modal", e);
            AlertUtil.showError("Error", "No se pudo abrir el formulario de cliente.");
        }
    }

    private void updateClientInfo(String selectedName) {
        if (selectedName == null || "Ninguno".equals(selectedName)) {
            // Hide info container
            clientInfoContainer.setVisible(false);
            clientInfoContainer.setManaged(false);
            return;
        }

        Integer clientId = clientMap.get(selectedName);
        if (clientId == null) {
            clientInfoContainer.setVisible(false);
            clientInfoContainer.setManaged(false);
            return;
        }

        Client client = clientService.findById(clientId);
        if (client == null) {
            clientInfoContainer.setVisible(false);
            clientInfoContainer.setManaged(false);
            return;
        }

        // Update labels
        clientNameLabel.setText(client.getName() != null ? client.getName() : "-");

        clientDocumentLabel.setText(
                (client.getDocument() != null && !client.getDocument().isBlank())
                        ? client.getDocument()
                        : "-"
        );

        clientPhoneLabel.setText(
                (client.getPhone() != null && !client.getPhone().isBlank())
                        ? client.getPhone()
                        : "-"
        );

        clientEmailLabel.setText(
                (client.getEmail() != null && !client.getEmail().isBlank())
                        ? client.getEmail()
                        : "-"
        );

        // Show info container
        clientInfoContainer.setVisible(true);
        clientInfoContainer.setManaged(true);
    }

    // =========================
    // PAYMENT METHOD
    // =========================
    private void setupPaymentMethod() {

        ToggleGroup group = new ToggleGroup();
        cashToggle.setToggleGroup(group);
        transferToggle.setToggleGroup(group);
        cardToggle.setToggleGroup(group);
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
        name.getStyleClass().add("sale-payment-ticket-name");
        name.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(name, javafx.scene.layout.Priority.ALWAYS);

        Label qty = new Label("x" + item.getQuantity());
        qty.getStyleClass().add("sale-payment-ticket-qty");
        qty.setMinWidth(60);

        Label price = new Label(
                NumberFormatterUtil.format(item.getItemTotal()) + " Gs"
        );
        price.getStyleClass().add("sale-payment-ticket-price");
        price.setMinWidth(100);

        HBox row = new HBox(12, name, qty, price);
        row.getStyleClass().add("sale-payment-summary-row");
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        return row;
    }

    private int getSelectedPaymentMethod() {

        if (cashToggle.isSelected()) {
            return 0;
        }
        if (transferToggle.isSelected()) {
            return 1;
        }
        if (cardToggle.isSelected()) {
            return 2;
        }
        if (qrToggle.isSelected()) {
            return 3;
        }

        throw new IllegalStateException("No payment method selected");
    }
    private Integer getSelectedClientId() {
        String selectedName = clientComboBox.getSelectionModel().getSelectedItem();
        if (selectedName == null || "Ninguno".equals(selectedName)) {
            return null;
        }
        return clientMap.get(selectedName);
    }
    private boolean isPaymentValid() {

        // 1. Método de pago seleccionado
        if (!cashToggle.isSelected()
                && !transferToggle.isSelected()
                && !cardToggle.isSelected()
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
                    "/app/barbman/core/view/embed-view/sale-create-view.fxml",
                    "/app/barbman/core/style/embed-views/sales-view.css"
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
                    "/app/barbman/core/view/embed-view/sale-result-view.fxml",
                    "/app/barbman/core/style/embed-views/sales-view.css"
            );
        });
    }
}
