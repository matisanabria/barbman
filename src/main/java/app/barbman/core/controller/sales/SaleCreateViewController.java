package app.barbman.core.controller.sales;

import app.barbman.core.dto.salecart.SaleCartDTO;
import app.barbman.core.dto.salecart.SaleCartItemDTO;
import app.barbman.core.model.human.User;
import app.barbman.core.model.sales.products.Product;
import app.barbman.core.model.sales.services.ServiceDefinition;
import app.barbman.core.repositories.sales.SaleRepositoryImpl;
import app.barbman.core.repositories.sales.products.product.ProductRepositoryImpl;
import app.barbman.core.repositories.sales.services.servicedefinition.ServiceDefinitionRepositoryImpl;
import app.barbman.core.repositories.users.UsersRepositoryImpl;
import app.barbman.core.service.sales.SalesService;
import app.barbman.core.service.sales.products.ProductService;
import app.barbman.core.service.sales.services.ServiceDefinitionsService;
import app.barbman.core.service.users.UsersService;
import app.barbman.core.util.AlertUtil;
import app.barbman.core.util.NumberFormatterUtil;
import app.barbman.core.util.SessionManager;
import app.barbman.core.util.TextFormatterUtil;
import app.barbman.core.util.window.EmbeddedViewLoader;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.*;

public class SaleCreateViewController implements Initializable {

    private static final Logger logger =
            LogManager.getLogger(SaleCreateViewController.class);

    // FXML
    @FXML private FlowPane itemsGrid;
    @FXML private VBox cartContainer;
    @FXML private Label totalLabel;
    @FXML private Button confirmButton;
    @FXML private ToggleButton servicesToggle;
    @FXML private ToggleButton productsToggle;
    @FXML private Label saleCreateTitle;
    @FXML private ComboBox<User> userComboBox;
    @FXML private TextField searchField;
    @FXML private Label todayTotalLabel;
    @FXML private Label weekTotalLabel;
    @FXML private Label monthTotalLabel;
    @FXML private Label cartItemsCount;

    // STATE
    private SaleCartDTO cart;
    private Mode currentMode = Mode.SERVICES;
    private List<ServiceDefinition> cachedServices = new ArrayList<>();
    private List<Product> cachedProducts = new ArrayList<>();

    private enum Mode { SERVICES, PRODUCTS }

    // SERVICES
    private final ServiceDefinitionsService serviceDefinitionsService =
            new ServiceDefinitionsService(new ServiceDefinitionRepositoryImpl());
    private final ProductService productService =
            new ProductService(new ProductRepositoryImpl());
    private final SalesService salesService =
            new SalesService(new SaleRepositoryImpl());
    private final UsersService usersService =
            new UsersService(new UsersRepositoryImpl());

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        User user = SessionManager.getActiveUser();
        if (user == null) {
            throw new IllegalStateException("No active user in session");
        }

        cart = new SaleCartDTO(user.getId());

        setupUserSelector();
        setupToggle();
        setupSearch();
        cacheData();
        loadCurrentMode();
        refreshCart();
        setupConfirmButton();
        updateStats();
    }

    // ── User selector ──────────────────────────────────────────

    private void setupUserSelector() {
        try {
            List<User> users = usersService.getAllUsers();
            userComboBox.setItems(FXCollections.observableArrayList(users));

            userComboBox.setConverter(new StringConverter<>() {
                @Override
                public String toString(User u) { return u != null ? u.getName() : ""; }
                @Override
                public User fromString(String s) { return null; }
            });

            User activeUser = SessionManager.getActiveUser();
            userComboBox.setValue(activeUser);
            cart.setSelectedUserId(activeUser.getId());

            userComboBox.valueProperty().addListener((obs, old, selected) -> {
                if (selected != null) {
                    cart.setSelectedUserId(selected.getId());
                    logger.info("[SALE-CREATE] Registrar a: {} (ID: {})",
                            selected.getName(), selected.getId());
                }
            });
        } catch (Exception e) {
            logger.error("[USER-SELECTOR] Error cargando usuarios", e);
            AlertUtil.showError("Error", "No se pudieron cargar los usuarios disponibles");
        }
    }

    // ── Toggle & Search ────────────────────────────────────────

    private void setupToggle() {
        ToggleGroup group = new ToggleGroup();
        servicesToggle.setToggleGroup(group);
        productsToggle.setToggleGroup(group);
        servicesToggle.setSelected(true);

        group.selectedToggleProperty().addListener((obs, old, selected) -> {
            if (selected == servicesToggle) currentMode = Mode.SERVICES;
            else if (selected == productsToggle) currentMode = Mode.PRODUCTS;
            searchField.clear();
            loadCurrentMode();
            searchField.requestFocus();
        });
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, old, text) -> loadCurrentMode());
    }

    private void cacheData() {
        cachedServices = serviceDefinitionsService.getAll().stream()
                .filter(ServiceDefinition::isAvailable)
                .toList();
        cachedProducts = productService.getAll().stream()
                .filter(p -> p.getStock() > 0)
                .toList();
    }

    // ── Load items ─────────────────────────────────────────────

    private void loadCurrentMode() {
        itemsGrid.getChildren().clear();
        String filter = searchField.getText();

        switch (currentMode) {
            case SERVICES -> cachedServices.stream()
                    .filter(def -> matches(def.getName(), filter))
                    .forEach(def -> itemsGrid.getChildren().add(buildServiceCard(def)));
            case PRODUCTS -> cachedProducts.stream()
                    .filter(p -> matches(p.getName(), filter))
                    .forEach(p -> itemsGrid.getChildren().add(buildProductCard(p)));
        }
    }

    private boolean matches(String name, String filter) {
        return filter == null || filter.isBlank()
                || name.toLowerCase().contains(filter.toLowerCase());
    }

    // ── Cart ───────────────────────────────────────────────────

    private void refreshCart() {
        cartContainer.getChildren().clear();

        if (cart.getCartItems().isEmpty()) {
            Label empty = new Label("Carrito vacio");
            empty.getStyleClass().add("cart-empty");
            cartContainer.getChildren().add(empty);
        } else {
            for (SaleCartItemDTO item : cart.getCartItems()) {
                cartContainer.getChildren().add(buildCartRow(item));
            }
        }

        totalLabel.setText(NumberFormatterUtil.format(cart.getTotal()) + " Gs");

        int count = cart.getCartItems().stream()
                .mapToInt(SaleCartItemDTO::getQuantity).sum();
        cartItemsCount.setText(String.valueOf(count));
    }

    private void setupConfirmButton() {
        confirmButton.setOnAction(e -> {
            if (cart.getCartItems().isEmpty()) {
                AlertUtil.showError("Carrito Vacio",
                        "Agrega al menos un item al carrito antes de continuar.");
                return;
            }

            SessionManager.setCurrentCartDTO(cart);

            EmbeddedViewLoader.load(
                    SessionManager.getMainBorderPane(),
                    EmbeddedViewLoader.Position.CENTER,
                    "/app/barbman/core/view/embed-view/sale-payment-view.fxml",
                    "/app/barbman/core/style/embed-views/sales-view.css"
            );

            logger.info("[SALE] Navegando a pago, usuario: {}", cart.getSelectedUserId());
        });
    }

    // ── Build service card ─────────────────────────────────────

    private VBox buildServiceCard(ServiceDefinition def) {
        VBox card = new VBox(8);
        card.getStyleClass().add("item-card");
        card.setPrefWidth(220);

        Label name = new Label(TextFormatterUtil.capitalizeFirstLetter(def.getName()));
        name.getStyleClass().add("item-card-name");
        name.setWrapText(true);

        TextField priceField = new TextField(NumberFormatterUtil.format(def.getBasePrice()));
        priceField.getStyleClass().add("item-card-price-field");

        priceField.textProperty().addListener((obs, old, val) -> {
            if (val == null || val.isBlank()) return;
            String clean = val.replace(".", "").trim();
            if (!clean.matches("\\d+")) {
                priceField.setText(old);
                return;
            }
            String formatted = NumberFormatterUtil.format(Double.parseDouble(clean));
            priceField.setText(formatted);
            priceField.positionCaret(formatted.length());
        });

        Button add = new Button("Agregar");
        add.getStyleClass().add("item-card-add");
        add.setMaxWidth(Double.MAX_VALUE);

        add.setOnAction(e -> {
            String clean = priceField.getText().replace(".", "").trim();
            if (!clean.matches("\\d+")) return;
            double price = Double.parseDouble(clean);
            cart.addService(def.getId(), def.getName(), price);
            refreshCart();
        });

        card.getChildren().addAll(name, priceField, add);
        return card;
    }

    // ── Build product card ─────────────────────────────────────

    private VBox buildProductCard(Product p) {
        VBox card = new VBox(8);
        card.getStyleClass().add("item-card");
        card.setPrefWidth(220);

        Label name = new Label(TextFormatterUtil.capitalizeFirstLetter(p.getName()));
        name.getStyleClass().add("item-card-name");
        name.setWrapText(true);

        HBox meta = new HBox(8);
        meta.setAlignment(Pos.CENTER_LEFT);

        Label price = new Label(NumberFormatterUtil.format(p.getUnitPrice()) + " Gs");
        price.getStyleClass().add("item-card-price");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label stock = new Label("Stock: " + p.getStock());
        stock.getStyleClass().add("item-card-stock");

        meta.getChildren().addAll(price, spacer, stock);

        Button add = new Button("Agregar");
        add.getStyleClass().add("item-card-add");
        add.setMaxWidth(Double.MAX_VALUE);

        add.setOnAction(e -> {
            if (p.getStock() <= 0) {
                AlertUtil.showError("Sin stock", "No hay stock disponible");
                return;
            }
            cart.addProduct(p.getId(), p.getName(), p.getUnitPrice());
            refreshCart();
        });

        card.getChildren().addAll(name, meta, add);
        return card;
    }

    // ── Build cart row ─────────────────────────────────────────

    private HBox buildCartRow(SaleCartItemDTO item) {
        HBox row = new HBox(0);
        row.getStyleClass().add("cart-row");
        row.setAlignment(Pos.CENTER_LEFT);

        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label name = new Label(TextFormatterUtil.capitalizeFirstLetter(item.getDisplayName()));
        name.getStyleClass().add("cart-row-name");

        Label sub = new Label(
                NumberFormatterUtil.format(item.getUnitPrice()) + " x" + item.getQuantity()
                        + "  =  " + NumberFormatterUtil.format(item.getItemTotal()) + " Gs"
        );
        sub.getStyleClass().add("cart-row-sub");

        info.getChildren().addAll(name, sub);

        // Quantity controls
        Button minus = new Button("-");
        minus.getStyleClass().add("cart-qty-btn");
        minus.setOnAction(e -> { cart.removeSingleUnit(item); refreshCart(); });

        Label qty = new Label(String.valueOf(item.getQuantity()));
        qty.getStyleClass().add("cart-qty-label");
        qty.setMinWidth(28);
        qty.setAlignment(Pos.CENTER);

        Button plus = new Button("+");
        plus.getStyleClass().add("cart-qty-btn");
        plus.setOnAction(e -> {
            if (item.getType() == SaleCartItemDTO.ItemType.PRODUCT) {
                cart.addProduct(item.getReferenceId(), item.getDisplayName(), item.getUnitPrice());
            } else {
                cart.addService(item.getReferenceId(), item.getDisplayName(), item.getUnitPrice());
            }
            refreshCart();
        });

        HBox qtyBox = new HBox(0, minus, qty, plus);
        qtyBox.setAlignment(Pos.CENTER);
        qtyBox.getStyleClass().add("cart-qty-box");

        Button remove = new Button("X");
        remove.getStyleClass().add("cart-remove-btn");
        remove.setOnAction(e -> { cart.removeItem(item); refreshCart(); });

        row.getChildren().addAll(info, qtyBox, remove);
        return row;
    }

    // ── Stats ──────────────────────────────────────────────────

    private void updateStats() {
        try {
            todayTotalLabel.setText(NumberFormatterUtil.format(salesService.getTodayTotal()) + " Gs");
            weekTotalLabel.setText(NumberFormatterUtil.format(salesService.getWeekTotal()) + " Gs");
            monthTotalLabel.setText(NumberFormatterUtil.format(salesService.getMonthTotal()) + " Gs");
        } catch (Exception e) {
            logger.error("[STATS] Error loading sales statistics", e);
            todayTotalLabel.setText("0 Gs");
            weekTotalLabel.setText("0 Gs");
            monthTotalLabel.setText("0 Gs");
        }
    }
}
