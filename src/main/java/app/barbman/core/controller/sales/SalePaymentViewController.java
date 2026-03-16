package app.barbman.core.controller.sales;

import app.barbman.core.controller.QuickAddClientModalController;
import app.barbman.core.dto.salecart.SaleCartDTO;
import app.barbman.core.dto.salecart.SaleCartItemDTO;
import app.barbman.core.model.human.Client;
import app.barbman.core.model.sales.Sale;
import app.barbman.core.repositories.cashbox.movement.CashboxMovementRepositoryImpl;
import app.barbman.core.repositories.client.ClientRepositoryImpl;
import app.barbman.core.repositories.sales.SaleRepositoryImpl;
import app.barbman.core.repositories.sales.products.productheader.ProductHeaderRepositoryImpl;
import app.barbman.core.repositories.sales.products.productsaleitem.ProductSaleItemRepositoryImpl;
import app.barbman.core.repositories.sales.services.serviceheader.ServiceHeaderRepositoryImpl;
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
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class SalePaymentViewController implements Initializable {

    private static final Logger logger =
            LogManager.getLogger(SalePaymentViewController.class);

    // ============================================================
    // FXML COMPONENTS
    // ============================================================

    @FXML private TextField clientSearchField;
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
    private Map<String, Integer> clientMap = new HashMap<>();
    private List<String> allClientNames = new ArrayList<>();
    private String selectedClientName = null;

    private Popup clientPopup;
    private ListView<String> clientListView;


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
        loadClients();
        buildClientPopup();

        // Filtrar al escribir
        clientSearchField.textProperty().addListener((obs, old, text) -> {
            if (text == null || text.isEmpty()) {
                selectedClientName = null;
                updateClientInfo(null);
                clientListView.getItems().setAll(allClientNames);
                showPopup();
                return;
            }

            // Si el texto coincide exactamente con el seleccionado, no re-filtrar
            if (text.equals(selectedClientName)) {
                return;
            }

            // Se está escribiendo → limpiar selección previa
            selectedClientName = null;

            String filter = text.toLowerCase();
            List<String> filtered = allClientNames.stream()
                    .filter(name -> name.toLowerCase().contains(filter))
                    .toList();

            clientListView.getItems().setAll(filtered);

            if (!filtered.isEmpty()) {
                showPopup();
            } else {
                clientPopup.hide();
            }
        });

        // Enter selecciona el item resaltado en la lista
        clientSearchField.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER && clientPopup.isShowing()) {
                String selected = clientListView.getSelectionModel().getSelectedItem();
                if (selected == null && !clientListView.getItems().isEmpty()) {
                    selected = clientListView.getItems().get(0);
                }
                if (selected != null) {
                    selectedClientName = selected;
                    clientSearchField.setText(selected);
                    clientPopup.hide();
                    updateClientInfo(selected);
                }
                e.consume();
            } else if (e.getCode() == javafx.scene.input.KeyCode.DOWN && clientPopup.isShowing()) {
                clientListView.requestFocus();
                if (clientListView.getSelectionModel().getSelectedIndex() < 0) {
                    clientListView.getSelectionModel().selectFirst();
                }
                e.consume();
            } else if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                clientPopup.hide();
                e.consume();
            }
        });

        // Enter/Escape en la lista
        clientListView.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                String selected = clientListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    selectedClientName = selected;
                    clientSearchField.setText(selected);
                    clientPopup.hide();
                    updateClientInfo(selected);
                }
                e.consume();
            } else if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                clientPopup.hide();
                clientSearchField.requestFocus();
                e.consume();
            }
        });

        // Mostrar popup al enfocar
        clientSearchField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (isFocused) {
                if (clientListView.getItems().isEmpty()) {
                    clientListView.getItems().setAll(allClientNames);
                }
                showPopup();
            } else {
                clientPopup.hide();
            }
        });

        addClientButton.setOnAction(e -> openQuickAddClientModal());
    }

    private void buildClientPopup() {
        clientListView = new ListView<>();
        clientListView.setPrefWidth(400);
        clientListView.setPrefHeight(200);
        clientListView.getStyleClass().add("sale-payment-combo-list");

        clientListView.setOnMouseClicked(e -> {
            String selected = clientListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selectedClientName = selected;
                clientSearchField.setText(selected);
                clientPopup.hide();
                updateClientInfo(selected);
            }
        });

        clientPopup = new Popup();
        clientPopup.setAutoHide(true);
        clientPopup.getContent().add(clientListView);
    }

    private void showPopup() {
        if (!clientSearchField.isVisible() || clientSearchField.getScene() == null) {
            return;
        }
        Bounds bounds = clientSearchField.localToScreen(clientSearchField.getBoundsInLocal());
        if (bounds == null) return;
        clientPopup.show(
                clientSearchField,
                bounds.getMinX(),
                bounds.getMaxY()
        );
    }

    private void loadClients() {
        clientMap.clear();
        allClientNames.clear();

        allClientNames.add("Ninguno");
        clientMap.put("Ninguno", null);

        clientService.findAll().stream()
                .filter(Client::isActive)
                .sorted(Comparator.comparing(Client::getName, String.CASE_INSENSITIVE_ORDER))
                .forEach(client -> {
                    allClientNames.add(client.getName());
                    clientMap.put(client.getName(), client.getId());
                });

        selectedClientName = null;
        clientSearchField.clear();
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

                // Reload clients
                loadClients();

                // Select the new client
                selectedClientName = createdClient.getName();
                clientSearchField.setText(createdClient.getName());
                updateClientInfo(createdClient.getName());
            });

            // Open modal manually
            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner((Stage) clientSearchField.getScene().getWindow());
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
        if (selectedClientName == null || "Ninguno".equals(selectedClientName)) {
            return null;
        }
        return clientMap.get(selectedClientName);
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
            logger.info("Client: {}", selectedClientName);
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
