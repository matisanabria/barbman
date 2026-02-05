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
import app.barbman.core.util.window.WindowManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
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
    @FXML
    private VBox servicesListContainer;
    @FXML
    private VBox cartContainer;
    @FXML
    private Label totalLabel;
    @FXML
    private Button confirmButton;
    @FXML
    private ToggleButton servicesToggle;
    @FXML
    private ToggleButton productsToggle;
    @FXML
    private BorderPane rootPane;
    @FXML
    private Label saleCreateTitle;
    @FXML
    private ComboBox<User> userComboBox;

    @FXML private Label todayTotalLabel;
    @FXML private Label weekTotalLabel;
    @FXML private Label monthTotalLabel;
    @FXML private Label cartItemsCount;


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
        loadServices();
        refreshCart();
        setupConfirmButton();
        updateStats();

        Tooltip.install(saleCreateTitle, new Tooltip("Easter egg"));
    }

    /**
     * Carga el selector de usuarios desde el FXML.
     * IMPORTANTE: El selectedUserId es el que se usa para toda la venta.
     */
    private void setupUserSelector() {
        try {
            List<User> users = usersService.getAllUsers();
            userComboBox.setItems(FXCollections.observableArrayList(users));

            // Converter para mostrar solo el nombre del usuario
            userComboBox.setConverter(new StringConverter<User>() {
                @Override
                public String toString(User user) {
                    return user != null ? user.getName() : "";
                }

                @Override
                public User fromString(String string) {
                    // No se usa en este caso
                    return null;
                }
            });

            // Seleccionar el usuario actual por defecto
            User activeUser = SessionManager.getActiveUser();
            userComboBox.setValue(activeUser);

            // SET: Toda la venta será a nombre de este usuario
            cart.setSelectedUserId(activeUser.getId());

            // Listener: cuando cambia el usuario seleccionado
            userComboBox.valueProperty().addListener((obs, old, selected) -> {
                if (selected != null) {
                    // IMPORTANTE: Todo (Sale + ServiceHeader) a nombre del usuario seleccionado
                    cart.setSelectedUserId(selected.getId());
                    logger.info("[SALE-CREATE] ✅ Venta registrada a: {} (ID: {})",
                            selected.getName(), selected.getId());
                }
            });

        } catch (Exception e) {
            logger.error("[USER-SELECTOR] Error cargando usuarios", e);
            AlertUtil.showError("Error", "No se pudieron cargar los usuarios disponibles");
        }
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

        // Filter: Only show available services
        services.stream()
                .filter(ServiceDefinition::isAvailable)
                .forEach(def ->
                        servicesListContainer.getChildren()
                                .add(buildServiceCard(def))
                );
    }

    private void loadProducts() {
        var products = productService.getAll();

        // Filter: Only show products with stock > 0
        products.stream()
                .filter(p -> p.getStock() > 0)
                .forEach(p ->
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

        // Actualizar contador de ítems
        int itemCount = cart.getCartItems().stream()
                .mapToInt(SaleCartItemDTO::getQuantity)
                .sum();
        cartItemsCount.setText(itemCount + " ítem" + (itemCount != 1 ? "s" : ""));
    }

    private void setupConfirmButton() {
        confirmButton.setOnAction(e -> {
            if (cart.getCartItems().isEmpty()) {
                AlertUtil.showError("Carrito Vacío", "Agrega al menos un ítem al carrito antes de continuar.");
                return;
            }

            SessionManager.setCurrentCartDTO(cart);

            EmbeddedViewLoader.load(
                    SessionManager.getMainBorderPane(),
                    EmbeddedViewLoader.Position.CENTER,
                    "/app/barbman/core/view/embed-view/sale-payment-view.fxml",
                    "/app/barbman/core/style/embed-views/sales-view.css"
            );

            logger.info("[SALE] Navegando a pantalla de pago con usuario seleccionado: {}",
                    cart.getSelectedUserId());
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
        Tooltip.install(
                add,
                new Tooltip("Agregar ítem al carrito")
        );

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
        price.getStyleClass().add("svc-card-price-static");
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
        Tooltip.install(
                add,
                new Tooltip("Agregar ítem al carrito")
        );

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
        Tooltip.install(
                removeOne,
                new Tooltip("Quitar un ítem")
        );

        // Quitar todos
        Button removeAll = new Button("X");
        removeAll.getStyleClass().add("salecart-row-remove-all");
        removeAll.setOnAction(e -> {
            cart.removeItem(item);
            refreshCart();
        });
        Tooltip.install(
                removeAll,
                new Tooltip("Quitar todos los ítems")
        );

        row.getChildren().addAll(
                name, price, qty, removeOne, removeAll
        );
        return row;
    }

    private void updateStats() {
        try {
            double todayTotal = salesService.getTodayTotal();
            double weekTotal = salesService.getWeekTotal();
            double monthTotal = salesService.getMonthTotal();

            todayTotalLabel.setText(NumberFormatterUtil.format(todayTotal) + " Gs");
            weekTotalLabel.setText(NumberFormatterUtil.format(weekTotal) + " Gs");
            monthTotalLabel.setText(NumberFormatterUtil.format(monthTotal) + " Gs");

            logger.debug("[STATS] Today: {}, Week: {}, Month: {}",
                    todayTotal, weekTotal, monthTotal);
        } catch (Exception e) {
            logger.error("[STATS] Error loading sales statistics", e);
            // Mantener los valores en 0 si hay error
            todayTotalLabel.setText("0 Gs");
            weekTotalLabel.setText("0 Gs");
            monthTotalLabel.setText("0 Gs");
        }
    }

}