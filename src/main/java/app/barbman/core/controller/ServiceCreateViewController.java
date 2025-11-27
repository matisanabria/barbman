package app.barbman.core.controller;

import app.barbman.core.dto.services.ServiceCartDTO;
import app.barbman.core.dto.services.CartItem;
import app.barbman.core.model.User;
import app.barbman.core.model.services.ServiceDefinition;
import app.barbman.core.repositories.services.servicedefinition.ServiceDefinitionRepositoryImpl;
import app.barbman.core.repositories.users.UsersRepositoryImpl;
import app.barbman.core.service.services.ServiceDefinitionsService;
import app.barbman.core.service.users.UsersService;
import app.barbman.core.util.NumberFormatterUtil;
import app.barbman.core.util.SessionManager;
import app.barbman.core.util.TextFormatterUtil;
import app.barbman.core.util.WindowManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.*;

public class ServiceCreateViewController implements Initializable {

    private static final Logger logger = LogManager.getLogger(ServiceCreateViewController.class);
    private static final String PREFIX = "[SERV-CREATE]";

    // ========= FXML =========
    @FXML private VBox servicesListContainer;   // left side
    @FXML private VBox cartContainer;           // right side
    @FXML private Label totalLabel;
    @FXML private Button confirmButton;

    private final ServiceDefinitionsService serviceDefinitionsService =
            new ServiceDefinitionsService(new ServiceDefinitionRepositoryImpl());
    private final UsersService usersService =
            new UsersService(new UsersRepositoryImpl());

    private ServiceCartDTO dto;

    // ====================================================================================
    //                                    INITIALIZE
    // ====================================================================================

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        logger.info("{} Initializing service creation view...", PREFIX);

        initDTO();
        loadServiceCards();
        setupConfirmButton();
        refreshCart();
    }

    /** Initializes DTO with active user info */
    private void initDTO() {
        User user = SessionManager.getActiveUser();
        if (user == null) {
            logger.error("{} No user active in session!", PREFIX);
            throw new IllegalStateException("No active user in session.");
        }

        // DTO moderno
        dto = new ServiceCartDTO(user.getId());
    }

    // ====================================================================================
    //                                    LEFT SIDE
    // ====================================================================================

    /** Loads service definition cards into the left container.*/
    private void loadServiceCards() {
        servicesListContainer.getChildren().clear(); // clear existing

        List<ServiceDefinition> defs = serviceDefinitionsService.getAll();

        for (ServiceDefinition def : defs) { // fill cards
            servicesListContainer.getChildren().add(buildServiceCard(def));
        }
    }

    /** Builds a service card UI component for a given service definition. */
    private HBox buildServiceCard(ServiceDefinition def) {
        HBox card = new HBox(12);
        card.getStyleClass().add("svc-card");
        card.setPadding(new Insets(8, 12, 8, 12));

        Label name = new Label(TextFormatterUtil.capitalizeFirstLetter(def.getName()));
        name.getStyleClass().add("svc-card-name");

        TextField priceField = new TextField(NumberFormatterUtil.format(def.getBasePrice()));
        priceField.getStyleClass().add("svc-card-price");
        priceField.setPrefWidth(90);

        // === FORMATEAR MIENTRAS EL USUARIO ESCRIBE ===
        priceField.textProperty().addListener((obs, old, newVal) -> {
            // permitir vacío
            if (newVal == null || newVal.isEmpty()) return;

            // limpiar puntos
            String clean = newVal.replace(".", "").trim();

            // si no es número, revertimos
            if (!clean.matches("\\d+")) {
                priceField.setText(old);
                return;
            }

            // formatear y mantener cursor al final
            String formatted = NumberFormatterUtil.format(Double.parseDouble(clean));
            priceField.setText(formatted);
            priceField.positionCaret(formatted.length());
        });

        // add button
        Button add = new Button("+");
        add.getStyleClass().add("svc-card-add");

        add.setOnAction(e -> {
            String clean = priceField.getText().replace(".", "").trim();
            if (!clean.matches("\\d+")) return;

            double price = Double.parseDouble(clean);

            dto.addItem(
                    def.getId(),
                    def.getName(),
                    price
            );

            refreshCart();
        });

        card.getChildren().addAll(name, priceField, add);
        return card;
    }

    // ====================================================================================
    //                                        CART
    // ====================================================================================

    /** Refreshes the cart UI to reflect the current state of the DTO. */
    private void refreshCart() {
        cartContainer.getChildren().clear();

        for (CartItem item : dto.getCartItems()) {
            cartContainer.getChildren().add(buildCartRow(item));
        }

        totalLabel.setText(NumberFormatterUtil.format(dto.getTotal()) + " Gs");
    }

    /** Builds a cart row UI component for a given cart item. */
    private HBox buildCartRow(CartItem item) {
        HBox row = new HBox(20);
        row.getStyleClass().add("cart-row");
        row.setPadding(new Insets(8, 12, 8, 12));

        Label name = new Label(TextFormatterUtil.capitalizeFirstLetter(item.getServiceName()));
        name.setPrefWidth(150);
        name.getStyleClass().add("cart-row-name");

        Label price = new Label(NumberFormatterUtil.format(item.getPrice()));
        price.setPrefWidth(80);
        price.getStyleClass().add("cart-row-price");

        Label qty = new Label("x" + item.getQuantity());
        qty.setPrefWidth(40);
        qty.getStyleClass().add("cart-row-qty");

        // remover only 1 item
        Button removeOne = new Button("-");
        removeOne.getStyleClass().add("cart-row-remove");
        removeOne.setOnAction(e -> {
            dto.removeSingleUnit(item);
            refreshCart();
        });

        // removes completely the item from the cart
        Button removeAll = new Button("✕");
        removeAll.getStyleClass().add("cart-row-remove");
        removeAll.setOnAction(e -> {
            dto.removeItem(item);
            refreshCart();
        });

        // add all to row
        row.getChildren().addAll(name, price, qty, removeOne, removeAll);
        return row;
    }

    // ====================================================================================
    //                                    CONFIRM BUTTON
    // ====================================================================================

    // Sets up the confirm button action.
    private void setupConfirmButton() {
        confirmButton.setOnAction(e -> {
            if (dto.getCartItems().isEmpty()) {
                // If item list is empty, show alert
                Alert alert = new Alert(Alert.AlertType.ERROR, "You must add at least 1 service to proceed.", ButtonType.OK);
                alert.show();
                return;
            }

            goToPaymentScreen();
        });
    }

    /** Hook for navigating to the payment screen. */
    private void goToPaymentScreen() {

        SessionManager.setCurrentCartDTO(dto);

        WindowManager.setEmbeddedView(
                SessionManager.getMainBorderPane(),
                "center",
                "/app/barbman/core/view/payment-view.fxml"
        );

        logger.info("{} Navigating to payment screen with {} items.", PREFIX, dto.getCartItems().size());
    }
}
