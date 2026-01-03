package app.barbman.core.controller.sales;

import app.barbman.core.dto.salecart.SaleCartDTO;
import app.barbman.core.dto.salecart.SaleCartItemDTO;
import app.barbman.core.model.human.User;
import app.barbman.core.model.sales.products.Product;
import app.barbman.core.model.sales.services.ServiceDefinition;
import app.barbman.core.repositories.sales.products.product.ProductRepositoryImpl;
import app.barbman.core.repositories.sales.services.servicedefinition.ServiceDefinitionRepositoryImpl;
import app.barbman.core.repositories.users.UsersRepositoryImpl;
import app.barbman.core.service.sales.products.ProductService;
import app.barbman.core.service.sales.services.ServiceDefinitionsService;
import app.barbman.core.service.users.UsersService;
import app.barbman.core.util.NumberFormatterUtil;
import app.barbman.core.util.SessionManager;
import app.barbman.core.util.TextFormatterUtil;
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

public class SaleCreateViewController implements Initializable {

    private static final Logger logger =
            LogManager.getLogger(SaleCreateViewController.class);

    // =========================
    // FXML
    // =========================
    @FXML private VBox servicesListContainer;
    @FXML private VBox cartContainer;
    @FXML private Label totalLabel;
    @FXML private Button confirmButton;
    @FXML private ToggleButton servicesToggle;
    @FXML private ToggleButton productsToggle;

    // =========================
    // STATE
    // =========================
    private SaleCartDTO cart;
    private Mode currentMode = Mode.SERVICES;

    private enum Mode {
        SERVICES,
        PRODUCTS
    }

    private final ServiceDefinitionsService serviceDefinitionsService =
            new ServiceDefinitionsService(new ServiceDefinitionRepositoryImpl());

    private final ProductService productService =
            new ProductService(new ProductRepositoryImpl());

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        User user = SessionManager.getActiveUser();
        if (user == null) {
            throw new IllegalStateException("No active user in session");
        }

        cart = new SaleCartDTO(user.getId());

        setupToggle();
        loadServices();
        refreshCart();
        setupConfirmButton();
    }

    private void setupToggle() {
        ToggleGroup group = new ToggleGroup();
        servicesToggle.setToggleGroup(group);
        productsToggle.setToggleGroup(group);

        servicesToggle.setSelected(true);

        group.selectedToggleProperty().addListener((obs, old, selected) -> {
            if (selected == servicesToggle) {
                currentMode = Mode.SERVICES;
            } else if (selected == productsToggle) {
                currentMode = Mode.PRODUCTS;
            }
            loadCurrentMode();
        });
    }

    private void loadCurrentMode() {
        servicesListContainer.getChildren().clear();

        switch (currentMode) {
            case SERVICES -> loadServices();
            case PRODUCTS -> loadProducts();
        }
    }

    private void loadServices() {
        var services = serviceDefinitionsService.getAll();
        services.forEach(def ->
                servicesListContainer.getChildren()
                        .add(buildServiceCard(def))
        );
    }

    private void loadProducts() {
        var products = productService.getAll();
        products.forEach(p ->
                servicesListContainer.getChildren()
                        .add(buildProductCard(p))
        );
    }

    private void refreshCart() {
        cartContainer.getChildren().clear();

        for (SaleCartItemDTO item : cart.getCartItems()) {
            cartContainer.getChildren().add(buildCartRow(item));
        }

        totalLabel.setText(
                NumberFormatterUtil.format(cart.getTotal()) + " Gs"
        );
    }
    private void setupConfirmButton() {
        confirmButton.setOnAction(e -> {
            if (cart.getCartItems().isEmpty()) {
                new Alert(Alert.AlertType.ERROR,
                        "Debe agregar al menos un item").show();
                return;
            }

            SessionManager.setCurrentCartDTO(cart);
            logger.info("[SALE] Proceeding to payment with {} items",
                    cart.getCartItems().size());
        });
    }

    // BUILD CARDS
    private HBox buildServiceCard(ServiceDefinition def) {

        HBox card = new HBox(12);
        card.setPadding(new Insets(8, 12, 8, 12));
        card.getStyleClass().add("svc-card");

        // Nombre
        Label name = new Label(
                TextFormatterUtil.capitalizeFirstLetter(def.getName())
        );
        name.getStyleClass().add("svc-card-name");
        name.setPrefWidth(160);

        // Precio editable
        TextField priceField =
                new TextField(NumberFormatterUtil.format(def.getBasePrice()));
        priceField.getStyleClass().add("svc-card-price");
        priceField.setPrefWidth(90);

        // Formateo en vivo
        priceField.textProperty().addListener((obs, old, val) -> {
            if (val == null || val.isBlank()) return;

            String clean = val.replace(".", "").trim();
            if (!clean.matches("\\d+")) {
                priceField.setText(old);
                return;
            }

            String formatted =
                    NumberFormatterUtil.format(Double.parseDouble(clean));
            priceField.setText(formatted);
            priceField.positionCaret(formatted.length());
        });

        // Botón agregar
        Button add = new Button("+");
        add.getStyleClass().add("svc-card-add");

        add.setOnAction(e -> {
            String clean = priceField.getText().replace(".", "").trim();
            if (!clean.matches("\\d+")) return;

            double price = Double.parseDouble(clean);

            cart.addService(
                    def.getId(),
                    def.getName(),
                    price
            );

            refreshCart();
        });

        card.getChildren().addAll(name, priceField, add);
        return card;
    }

    private HBox buildProductCard(Product p) {

        HBox card = new HBox(12);
        card.setPadding(new Insets(8, 12, 8, 12));
        card.getStyleClass().add("svc-card");

        // Nombre
        Label name = new Label(
                TextFormatterUtil.capitalizeFirstLetter(p.getName())
        );
        name.getStyleClass().add("svc-card-name");
        name.setPrefWidth(160);

        // Precio fijo
        Label price = new Label(
                NumberFormatterUtil.format(p.getUnitPrice())
        );
        price.getStyleClass().add("svc-card-price");
        price.setPrefWidth(80);

        // Stock
        Label stock = new Label("Stock: " + p.getStock());
        stock.getStyleClass().add("svc-card-stock");
        stock.setPrefWidth(80);

        // Botón agregar
        Button add = new Button("+");
        add.getStyleClass().add("svc-card-add");

        add.setOnAction(e -> {
            if (p.getStock() <= 0) {
                new Alert(
                        Alert.AlertType.ERROR,
                        "No hay stock disponible"
                ).show();
                return;
            }

            cart.addProduct(
                    p.getId(),
                    p.getName(),
                    p.getUnitPrice()
            );

            refreshCart();
        });

        card.getChildren().addAll(name, price, stock, add);
        return card;
    }

    private HBox buildCartRow(SaleCartItemDTO item) {

        HBox row = new HBox(20);
        row.setPadding(new Insets(8, 12, 8, 12));
        row.getStyleClass().add("salecart-row");

        // Nombre
        Label name = new Label(
                TextFormatterUtil.capitalizeFirstLetter(item.getDisplayName())
        );
        name.setPrefWidth(160);
        name.getStyleClass().add("salecart-row-name");

        // Precio unitario
        Label price = new Label(
                NumberFormatterUtil.format(item.getUnitPrice())
        );
        price.setPrefWidth(80);
        price.getStyleClass().add("salecart-row-price");

        // Cantidad
        Label qty = new Label("x" + item.getQuantity());
        qty.setPrefWidth(40);
        qty.getStyleClass().add("salecart-row-qty");

        // Quitar uno
        Button removeOne = new Button("-");
        removeOne.getStyleClass().add("salecart-row-remove");
        removeOne.setOnAction(e -> {
            cart.removeSingleUnit(item);
            refreshCart();
        });

        // Quitar todos
        Button removeAll = new Button("✕");
        removeAll.getStyleClass().add("salecart-row-remove");
        removeAll.setOnAction(e -> {
            cart.removeItem(item);
            refreshCart();
        });

        row.getChildren().addAll(
                name, price, qty, removeOne, removeAll
        );
        return row;
    }


}
