package app.barbman.core.controller;

import app.barbman.core.model.human.Client;
import app.barbman.core.model.human.User;
import app.barbman.core.model.sales.products.Product;
import app.barbman.core.model.sales.services.ServiceDefinition;
import app.barbman.core.repositories.client.ClientRepositoryImpl;
import app.barbman.core.repositories.sales.products.product.ProductRepositoryImpl;
import app.barbman.core.repositories.sales.services.servicedefinition.ServiceDefinitionRepositoryImpl;
import app.barbman.core.repositories.users.UsersRepositoryImpl;
import app.barbman.core.service.clients.ClientService;
import app.barbman.core.service.sales.products.ProductService;
import app.barbman.core.service.sales.services.ServiceDefinitionsService;
import app.barbman.core.service.users.UsersService;
import app.barbman.core.util.AlertUtil;
import app.barbman.core.util.NumberFormatterUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for settings view with tabbed sections.
 */
public class SettingsController implements Initializable {

    private static final Logger logger = LogManager.getLogger(SettingsController.class);
    private static final String PREFIX = "[SETTINGS]";

    // ============================================================
    // SERVICES
    // ============================================================

    private final ProductService productService;
    private final ServiceDefinitionsService serviceService;
    private final UsersService usersService;
    private final ClientService clientService;

    // ============================================================
    // FXML - PRODUCTOS
    // ============================================================

    @FXML private VBox productsListContainer;
    @FXML private Label productsEmptyLabel;
    @FXML private VBox productFormContainer;
    @FXML private Label productFormTitle;
    @FXML private TextField productNameField;
    @FXML private TextField productCostField;
    @FXML private TextField productPriceField;
    @FXML private TextField productStockField;

    private Product currentEditingProduct = null;

    // ============================================================
    // FXML - SERVICIOS
    // ============================================================

    @FXML private VBox servicesListContainer;
    @FXML private Label servicesEmptyLabel;

    // ============================================================
    // FXML - USUARIOS
    // ============================================================

    @FXML private VBox usersListContainer;
    @FXML private Label usersEmptyLabel;

    // ============================================================
    // FXML - CLIENTES
    // ============================================================

    @FXML private VBox clientsListContainer;
    @FXML private Label clientsEmptyLabel;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public SettingsController() {
        this.productService = new ProductService(new ProductRepositoryImpl());
        this.serviceService = new ServiceDefinitionsService(new ServiceDefinitionRepositoryImpl());
        this.usersService = new UsersService(new UsersRepositoryImpl());
        this.clientService = new ClientService(new ClientRepositoryImpl());
    }

    // ============================================================
    // INIT
    // ============================================================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("{} Initializing settings view", PREFIX);

        loadProducts();
        loadServices();
        loadUsers();
        loadClients();

        logger.info("{} Settings view initialized", PREFIX);
    }

    // ============================================================
    // PRODUCTS
    // ============================================================

    private void loadProducts() {
        logger.info("{} Loading products...", PREFIX);

        List<Product> products = productService.getAll();

        productsListContainer.getChildren().clear();

        if (products.isEmpty()) {
            productsEmptyLabel.setVisible(true);
            productsEmptyLabel.setManaged(true);
        } else {
            productsEmptyLabel.setVisible(false);
            productsEmptyLabel.setManaged(false);

            for (Product product : products) {
                productsListContainer.getChildren().add(createProductItem(product));
            }
        }

        logger.info("{} Loaded {} products", PREFIX, products.size());
    }

    private HBox createProductItem(Product product) {
        HBox item = new HBox(12);
        item.getStyleClass().add("settings-item");
        item.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        item.setPadding(new Insets(14, 16, 14, 16));

        // Name
        Label nameLabel = new Label(product.getName());
        nameLabel.getStyleClass().add("settings-item-name");
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        // Price
        Label priceLabel = new Label(
                "Costo: " + NumberFormatterUtil.format(product.getCostPrice()) + " | " +
                        "Venta: " + NumberFormatterUtil.format(product.getUnitPrice()) + " Gs"
        );
        priceLabel.getStyleClass().add("settings-item-price");

        // Stock
        Label stockLabel = new Label("Stock: " + product.getStock());
        stockLabel.getStyleClass().add("settings-item-stock");

        // Edit button
        Button editBtn = new Button("Editar");
        editBtn.getStyleClass().add("settings-btn-edit");
        editBtn.setOnAction(e -> editProduct(product));

        // Delete button
        Button deleteBtn = new Button("Eliminar");
        deleteBtn.getStyleClass().add("settings-btn-delete");
        deleteBtn.setOnAction(e -> deleteProduct(product));

        item.getChildren().addAll(nameLabel, priceLabel, stockLabel, editBtn, deleteBtn);

        return item;
    }

    @FXML
    private void onAddProduct() {
        logger.info("{} Opening product form (CREATE mode)", PREFIX);

        currentEditingProduct = null;
        productFormTitle.setText("Agregar Producto");

        // Clear fields
        productNameField.clear();
        productCostField.clear();
        productPriceField.clear();
        productStockField.clear();

        // Show form
        productFormContainer.setVisible(true);
        productFormContainer.setManaged(true);
    }

    private void editProduct(Product product) {
        logger.info("{} Opening product form (EDIT mode): {}", PREFIX, product.getId());

        currentEditingProduct = product;
        productFormTitle.setText("Editar Producto");

        // Load data
        productNameField.setText(product.getName());
        productCostField.setText(String.valueOf((int)product.getCostPrice()));
        productPriceField.setText(String.valueOf((int)product.getUnitPrice()));
        productStockField.setText(String.valueOf(product.getStock()));

        // Show form
        productFormContainer.setVisible(true);
        productFormContainer.setManaged(true);
    }

    @FXML
    private void onCancelProductForm() {
        logger.info("{} Product form cancelled", PREFIX);

        productFormContainer.setVisible(false);
        productFormContainer.setManaged(false);
        currentEditingProduct = null;
    }

    @FXML
    private void onSaveProduct() {
        logger.info("{} Saving product...", PREFIX);

        // Validate
        String name = productNameField.getText();
        if (name == null || name.isBlank()) {
            AlertUtil.showWarning("Validacion", "El nombre es obligatorio.");
            return;
        }

        String costStr = productCostField.getText();  // NUEVO
        if (costStr == null || costStr.isBlank()) {
            AlertUtil.showWarning("Validacion", "El precio de costo es obligatorio.");
            return;
        }

        String priceStr = productPriceField.getText();
        if (priceStr == null || priceStr.isBlank()) {
            AlertUtil.showWarning("Validacion", "El precio de venta es obligatorio.");
            return;
        }

        String stockStr = productStockField.getText();
        if (stockStr == null || stockStr.isBlank()) {
            AlertUtil.showWarning("Validacion", "El stock es obligatorio.");
            return;
        }

        double costPrice;  // NUEVO
        double unitPrice;
        int stock;

        try {
            costPrice = Double.parseDouble(costStr);  // NUEVO
            unitPrice = Double.parseDouble(priceStr);
            stock = Integer.parseInt(stockStr);
        } catch (NumberFormatException e) {
            AlertUtil.showWarning("Validacion", "Los precios y stock deben ser numeros validos.");
            return;
        }

        if (costPrice <= 0) {  // NUEVO
            AlertUtil.showWarning("Validacion", "El precio de costo debe ser mayor a 0.");
            return;
        }

        if (unitPrice <= 0) {
            AlertUtil.showWarning("Validacion", "El precio de venta debe ser mayor a 0.");
            return;
        }

        if (unitPrice < costPrice) {
            AlertUtil.showWarning("Validacion", "El precio de venta debe ser mayor al precio de costo.");
            return;
        }

        if (stock < 0) {
            AlertUtil.showWarning("Validacion", "El stock no puede ser negativo.");
            return;
        }

        try {
            if (currentEditingProduct == null) {
                // CREATE
                Product newProduct = new Product(name, costPrice, unitPrice, stock);  // ACTUALIZADO
                productService.save(newProduct);

                AlertUtil.showInfo("Exito", "Producto creado exitosamente.");
            } else {
                // UPDATE
                currentEditingProduct.setName(name);
                currentEditingProduct.setCostPrice(costPrice);  // NUEVO
                currentEditingProduct.setUnitPrice(unitPrice);
                currentEditingProduct.setStock(stock);

                productService.update(currentEditingProduct);

                AlertUtil.showInfo("Exito", "Producto actualizado exitosamente.");
            }

            // Hide form and refresh
            onCancelProductForm();
            loadProducts();

        } catch (Exception e) {
            logger.error("{} Error saving product", PREFIX, e);
            AlertUtil.showError("Error", "No se pudo guardar el producto: " + e.getMessage());
        }
    }

    private void deleteProduct(Product product) {
        logger.info("{} Delete product requested: {}", PREFIX, product.getId());

        boolean confirmed = AlertUtil.showConfirmation(
                "Eliminar Producto",
                String.format("¿Estas seguro de eliminar '%s'?\n\nEl stock se pondra en 0.", product.getName())
        );

        if (!confirmed) {
            return;
        }

        try {
            productService.softDelete(product.getId());

            AlertUtil.showInfo(
                    "Producto eliminado",
                    "El producto fue eliminado exitosamente."
            );

            loadProducts(); // Refresh list

        } catch (Exception e) {
            logger.error("{} Error deleting product", PREFIX, e);
            AlertUtil.showError("Error", "No se pudo eliminar el producto: " + e.getMessage());
        }
    }

    // ============================================================
    // SERVICES
    // ============================================================

    private void loadServices() {
        logger.info("{} Loading services...", PREFIX);

        List<ServiceDefinition> services = serviceService.getAll();

        servicesListContainer.getChildren().clear();

        if (services.isEmpty()) {
            servicesEmptyLabel.setVisible(true);
            servicesEmptyLabel.setManaged(true);
        } else {
            servicesEmptyLabel.setVisible(false);
            servicesEmptyLabel.setManaged(false);

            for (ServiceDefinition service : services) {
                servicesListContainer.getChildren().add(createServiceItem(service));
            }
        }

        logger.info("{} Loaded {} services", PREFIX, services.size());
    }

    private HBox createServiceItem(ServiceDefinition service) {
        HBox item = new HBox(12);
        item.getStyleClass().add("settings-item");
        item.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        item.setPadding(new Insets(14, 16, 14, 16));

        // Name
        Label nameLabel = new Label(service.getName());
        nameLabel.getStyleClass().add("settings-item-name");
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        // Price
        Label priceLabel = new Label(NumberFormatterUtil.format(service.getBasePrice()) + " Gs");
        priceLabel.getStyleClass().add("settings-item-price");

        // Status
        String status = service.isAvailable() ? "Disponible" : "No disponible";
        Label statusLabel = new Label(status);
        statusLabel.getStyleClass().add("settings-item-status");

        // Edit button
        Button editBtn = new Button("Editar");
        editBtn.getStyleClass().add("settings-btn-edit");
        editBtn.setOnAction(e -> editService(service));

        // Delete button
        Button deleteBtn = new Button("Eliminar");
        deleteBtn.getStyleClass().add("settings-btn-delete");
        deleteBtn.setOnAction(e -> deleteService(service));

        item.getChildren().addAll(nameLabel, priceLabel, statusLabel, editBtn, deleteBtn);

        return item;
    }

    @FXML
    private void onAddService() {
        logger.info("{} Add service clicked", PREFIX);
        // TODO: Implementar en Fase 2
    }
    private void editService(ServiceDefinition service) {
        logger.info("{} Edit service: {}", PREFIX, service.getId());
        // TODO: Implementar en Fase 2
    }

    private void deleteService(ServiceDefinition service) {
        logger.info("{} Delete service requested: {}", PREFIX, service.getId());

        boolean confirmed = AlertUtil.showConfirmation(
                "Eliminar Servicio",
                String.format("¿Estas seguro de eliminar '%s'?\n\nSe marcara como no disponible.", service.getName())
        );

        if (!confirmed) {
            return;
        }

        try {
            serviceService.softDelete(service.getId());

            AlertUtil.showInfo(
                    "Servicio eliminado",
                    "El servicio fue eliminado exitosamente."
            );

            loadServices(); // Refresh list

        } catch (Exception e) {
            logger.error("{} Error deleting service", PREFIX, e);
            AlertUtil.showError("Error", "No se pudo eliminar el servicio: " + e.getMessage());
        }
    }

    // ============================================================
    // USERS
    // ============================================================

    private void loadUsers() {
        logger.info("{} Loading users...", PREFIX);

        List<User> users = usersService.getAllUsers();

        usersListContainer.getChildren().clear();

        if (users.isEmpty()) {
            usersEmptyLabel.setVisible(true);
            usersEmptyLabel.setManaged(true);
        } else {
            usersEmptyLabel.setVisible(false);
            usersEmptyLabel.setManaged(false);

            for (User user : users) {
                usersListContainer.getChildren().add(createUserItem(user));
            }
        }

        logger.info("{} Loaded {} users", PREFIX, users.size());
    }

    private HBox createUserItem(User user) {
        HBox item = new HBox(12);
        item.getStyleClass().add("settings-item");
        item.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        item.setPadding(new Insets(14, 16, 14, 16));

        // Name
        Label nameLabel = new Label(user.getName());
        nameLabel.getStyleClass().add("settings-item-name");
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        // Role
        Label roleLabel = new Label(translateRole(user.getRole()));
        roleLabel.getStyleClass().add("settings-item-role");

        // PIN (masked)
        Label pinLabel = new Label("PIN: ****");
        pinLabel.getStyleClass().add("settings-item-info");

        // Edit button
        Button editBtn = new Button("Editar");
        editBtn.getStyleClass().add("settings-btn-edit");
        editBtn.setOnAction(e -> editUser(user));

        // Delete button
        Button deleteBtn = new Button("Eliminar");
        deleteBtn.getStyleClass().add("settings-btn-delete");
        deleteBtn.setOnAction(e -> deleteUser(user));

        item.getChildren().addAll(nameLabel, roleLabel, pinLabel, editBtn, deleteBtn);

        return item;
    }
    @FXML
    private void onAddUser() {
        logger.info("{} Add user clicked", PREFIX);
        // TODO: Implementar en Fase 2
    }

    private void editUser(User user) {
        logger.info("{} Edit user: {}", PREFIX, user.getId());
        // TODO: Implementar en Fase 2
    }

    private void deleteUser(User user) {
        logger.info("{} Delete user requested: {}", PREFIX, user.getId());

        // Confirmacion 1 - Normal
        boolean step1 = AlertUtil.showConfirmation(
                "Eliminar Usuario",
                String.format("¿Estas seguro de eliminar a '%s'?", user.getName())
        );

        if (!step1) return;

        // Confirmacion 2 - Advertencia
        boolean step2 = AlertUtil.showConfirmation(
                "¿Seguro?",
                "Esta accion marcara al usuario como eliminado.\n\n¿Realmente quieres continuar?"
        );

        if (!step2) return;

        // Confirmacion 3 - Mas serio
        boolean step3 = AlertUtil.showConfirmation(
                "Ultima oportunidad",
                "El usuario no podra iniciar sesion nunca mas.\n\nNo apareceran en reportes ni estadisticas.\n\n¿Continuar?"
        );

        if (!step3) return;

        // Confirmacion 4 - Desesperacion
        boolean step4 = AlertUtil.showConfirmation(
                "POR FAVOR RECONSIDERA ESTO",
                String.format("'%s' sera eliminado PERMANENTEMENTE.\n\nTODOS sus datos quedaran marcados como 'deleted'.\n\n¿DE VERDAD quieres hacer esto?", user.getName())
        );

        if (!step4) return;

        // Confirmacion 5 - Ultima suplica
        boolean step5 = AlertUtil.showConfirmation(
                "ULTIMA ADVERTENCIA",
                "OK OK OK.\n\nSi realmente NECESITAS hacer esto, adelante.\n\nPero por favor, PORFAVOR, piensalo una vez mas.\n\n¿Eliminar usuario?"
        );

        if (!step5) return;

        // Si llego hasta aca... bueno, se lo gano
        try {
            usersService.softDelete(user.getId());

            AlertUtil.showInfo(
                    "Usuario eliminado",
                    "Bueno... lo hiciste. El usuario fue eliminado.\n\nEspero que estes feliz."
            );

            loadUsers(); // Refresh list

        } catch (Exception e) {
            logger.error("{} Error deleting user", PREFIX, e);
            AlertUtil.showError(
                    "Error",
                    "Ni siquiera se pudo eliminar correctamente.\n\n" + e.getMessage()
            );
        }
    }

    private String translateRole(String role) {
        return switch (role.toLowerCase()) {
            case "admin" -> "Administrador";
            case "user" -> "Usuario";
            default -> role;
        };
    }

    // ============================================================
    // CLIENTS
    // ============================================================

    private void loadClients() {
        logger.info("{} Loading clients...", PREFIX);

        List<Client> clients = clientService.findAll();

        clientsListContainer.getChildren().clear();

        if (clients.isEmpty()) {
            clientsEmptyLabel.setVisible(true);
            clientsEmptyLabel.setManaged(true);
        } else {
            clientsEmptyLabel.setVisible(false);
            clientsEmptyLabel.setManaged(false);

            for (Client client : clients) {
                clientsListContainer.getChildren().add(createClientItem(client));
            }
        }

        logger.info("{} Loaded {} clients", PREFIX, clients.size());
    }

    private HBox createClientItem(Client client) {
        HBox item = new HBox(12);
        item.getStyleClass().add("settings-item");
        item.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        item.setPadding(new Insets(14, 16, 14, 16));

        // Name
        Label nameLabel = new Label(client.getName());
        nameLabel.getStyleClass().add("settings-item-name");
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        // Phone
        Label phoneLabel = new Label(client.getPhone());
        phoneLabel.getStyleClass().add("settings-item-info");

        // Status
        String status = client.isActive() ? "Activo" : "Inactivo";
        Label statusLabel = new Label(status);
        statusLabel.getStyleClass().add("settings-item-status");

        // Edit button
        Button editBtn = new Button("Editar");
        editBtn.getStyleClass().add("settings-btn-edit");
        editBtn.setOnAction(e -> editClient(client));

        // Delete button
        Button deleteBtn = new Button("Eliminar");
        deleteBtn.getStyleClass().add("settings-btn-delete");
        deleteBtn.setOnAction(e -> deleteClient(client));

        item.getChildren().addAll(nameLabel, phoneLabel, statusLabel, editBtn, deleteBtn);

        return item;
    }
    @FXML
    private void onAddClient() {
        logger.info("{} Add client clicked", PREFIX);
        // TODO: Implementar en Fase 2
    }

    private void editClient(Client client) {
        logger.info("{} Edit client: {}", PREFIX, client.getId());
        // TODO: Implementar en Fase 2
    }

    private void deleteClient(Client client) {
        logger.info("{} Delete client requested: {}", PREFIX, client.getId());

        boolean confirmed = AlertUtil.showConfirmation(
                "Eliminar Cliente",
                String.format("¿Estas seguro de eliminar a '%s'?\n\nSe marcara como inactivo.", client.getName())
        );

        if (!confirmed) {
            return;
        }

        try {
            clientService.softDelete(client.getId());

            AlertUtil.showInfo(
                    "Cliente eliminado",
                    "El cliente fue eliminado exitosamente."
            );

            loadClients(); // Refresh list

        } catch (Exception e) {
            logger.error("{} Error deleting client", PREFIX, e);
            AlertUtil.showError("Error", "No se pudo eliminar el cliente: " + e.getMessage());
        }
    }
}