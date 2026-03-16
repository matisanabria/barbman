package app.barbman.core.controller;

import app.barbman.core.model.human.User;
import app.barbman.core.repositories.DbBootstrap;
import app.barbman.core.util.SessionManager;
import app.barbman.core.util.window.EmbeddedViewLoader;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;

public class SidebarController {

    private static final Logger logger = LogManager.getLogger(SidebarController.class);
    private static final String PREFIX = "[SIDEBAR]";

    private BorderPane targetPane;

    @FXML private ImageView userAvatar;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private VBox menuContainer;
    @FXML private ToggleButton btnLogout;

    private final ToggleGroup menuGroup = new ToggleGroup();

    // ============================================================
    // ======================= BINDING ============================
    // ============================================================

    public void bind(BorderPane borderPane) {
        this.targetPane = borderPane;
    }

    // ============================================================
    // ====================== INITIALIZE ==========================
    // ============================================================

    @FXML
    private void initialize() {
        logger.info("{} Initializing sidebar", PREFIX);

        loadUserInfo();
        injectMenuButtons();
        setupLogout();

        logger.info("{} Sidebar initialized", PREFIX);
    }

    // ============================================================
    // ===================== USER INFO ============================
    // ============================================================

    private void loadUserInfo() {
        User user = SessionManager.getActiveUser();

        if (user != null) {
            userNameLabel.setText(user.getName());
            userRoleLabel.setText(translateRole(user.getRole()));

            // Cargar avatar desde carpeta de datos
            loadUserAvatar(user.getAvatarPath());

            logger.info("{} User info loaded: {} ({})", PREFIX, user.getName(), user.getRole());
        }
    }

    private void loadUserAvatar(String avatarFileName) {
        if (avatarFileName == null || avatarFileName.isEmpty()) {
            avatarFileName = "default.png";
        }

        try {
            // Construir path completo al avatar
            File avatarsFolder = DbBootstrap.getAvatarsFolder();
            File avatarFile = new File(avatarsFolder, avatarFileName);

            if (avatarFile.exists()) {
                // Cargar desde archivo externo
                Image avatar = new Image(avatarFile.toURI().toString());
                userAvatar.setImage(avatar);
                logger.debug("{} Avatar loaded from: {}", PREFIX, avatarFile.getAbsolutePath());
            } else {
                // Fallback a default
                logger.warn("{} Avatar file not found: {}, using default", PREFIX, avatarFileName);
                loadDefaultAvatar();
            }

        } catch (Exception e) {
            logger.warn("{} Could not load avatar: {}, using default", PREFIX, avatarFileName);
            loadDefaultAvatar();
        }
    }

    private void loadDefaultAvatar() {
        try {
            // Intentar cargar desde carpeta de datos
            File defaultFile = new File(DbBootstrap.getAvatarsFolder(), "default.png");
            if (defaultFile.exists()) {
                Image avatar = new Image(defaultFile.toURI().toString());
                userAvatar.setImage(avatar);
                logger.debug("{} Default avatar loaded from: {}", PREFIX, defaultFile.getAbsolutePath());
            } else {
                // Si no existe en carpeta de datos, cargar desde resources (embedded en JAR)
                Image avatar = new Image(getClass().getResourceAsStream(
                        "/app/barbman/core/assets/avatars/default.png"
                ));
                userAvatar.setImage(avatar);
                logger.debug("{} Default avatar loaded from resources", PREFIX);
            }
        } catch (Exception e) {
            logger.error("{} Failed to load default avatar: {}", PREFIX, e.getMessage());
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
    // =================== MENU INJECTION =========================
    // ============================================================

    private void injectMenuButtons() {
        User user = SessionManager.getActiveUser();
        String role = user != null ? user.getRole() : "user";

        if ("admin".equalsIgnoreCase(role)) {
            injectAdminMenu();
        } else {
            injectUserMenu();
        }

        // Seleccionar primer botón por defecto
        if (!menuGroup.getToggles().isEmpty()) {
            menuGroup.getToggles().get(0).setSelected(true);
        }
    }

    private void injectAdminMenu() {
        // ADMIN: tiene acceso a TODO
        addMenuButton(
                "Ventas",
                "fas-cart-plus",
                "/app/barbman/core/view/embed-view/sale-create-view.fxml",
                "/app/barbman/core/style/embed-views/sales-view.css",
                true // cargar al inicio
        );

        // 👇 NUEVO - HISTORIAL
        addMenuButton(
                "Historial",
                "fas-history",
                "/app/barbman/core/view/sale-history-view.fxml",
                "/app/barbman/core/style/embed-views/sale-history.css",
                false
        );

        addMenuButton(
                "Egresos",
                "fas-arrow-down",
                "/app/barbman/core/view/embed-view/expenses-view.fxml",
                "/app/barbman/core/style/embed-views/expenses-view.css",
                false
        );

        addMenuButton(
                "Sueldos",
                "far-money-bill-alt",
                "/app/barbman/core/view/embed-view/salary-view.fxml",
                "/app/barbman/core/style/embed-views/salary-view.css",
                false
        );

        addMenuButton(
                "Caja",
                "fas-cash-register",
                "/app/barbman/core/view/embed-view/cashbox-view.fxml",
                "/app/barbman/core/style/embed-views/cashbox-view.css",
                false
        );

        addMenuButton(
                "Reservas",
                "fas-calendar-alt",
                "/app/barbman/core/view/embed-view/appointments-view.fxml",
                "/app/barbman/core/style/embed-views/appointments-view.css",
                false
        );

        addMenuButton(
                "Configuración",
                "fas-cog",
                "/app/barbman/core/view/embed-view/settings-view.fxml",
                "/app/barbman/core/style/embed-views/settings-view.css",
                false
        );

        logger.info("{} Admin menu injected (7 buttons)", PREFIX);
    }

    private void injectUserMenu() {
        // USER: solo Ventas, Historial, Egresos y Caja (sin Sueldos ni Configuración)
        addMenuButton(
                "Ventas",
                "fas-cart-plus",
                "/app/barbman/core/view/embed-view/sale-create-view.fxml",
                "/app/barbman/core/style/embed-views/sales-view.css",
                true // cargar al inicio
        );

        // 👇 NUEVO - HISTORIAL
        addMenuButton(
                "Historial",
                "fas-history",
                "/app/barbman/core/view/sale-history-view.fxml",
                "/app/barbman/core/style/embed-views/sale-history.css",
                false
        );

        addMenuButton(
                "Egresos",
                "fas-arrow-down",
                "/app/barbman/core/view/embed-view/expenses-view.fxml",
                "/app/barbman/core/style/embed-views/expenses-view.css",
                false
        );

        addMenuButton(
                "Caja",
                "fas-cash-register",
                "/app/barbman/core/view/embed-view/cashbox-view.fxml",
                "/app/barbman/core/style/embed-views/cashbox-view.css",
                false
        );

        logger.info("{} User menu injected (4 buttons)", PREFIX); // 👈 Cambiar de 3 a 4
    }

    private void addMenuButton(
            String text,
            String iconLiteral,
            String fxmlPath,
            String cssPath,
            boolean loadOnInit
    ) {
        ToggleButton btn = new ToggleButton(text);
        btn.getStyleClass().add("sidebar-btn");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setToggleGroup(menuGroup);

        // Icon
        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(18);
        btn.setGraphic(icon);

        // Action
        btn.setOnAction(event -> load(fxmlPath, cssPath));

        menuContainer.getChildren().add(btn);

        // Cargar vista inicial si corresponde
        if (loadOnInit) {
            load(fxmlPath, cssPath);
        }
    }

    // ============================================================
    // ======================= LOGOUT =============================
    // ============================================================

    private void setupLogout() {
        btnLogout.setOnAction(event -> {
            logger.info("{} Logout requested", PREFIX);
            SessionManager.endSession();
            MainViewController.logoutFromSidebar(); // LLAMADA ESTÁTICA
        });
    }

    // ============================================================
    // ==================== VIEW LOADING ==========================
    // ============================================================

    private void load(String fxmlPath, String cssPath) {
        if (targetPane == null) {
            logger.warn("{} No target pane bound", PREFIX);
            return;
        }

        EmbeddedViewLoader.load(
                targetPane,
                EmbeddedViewLoader.Position.CENTER,
                fxmlPath,
                cssPath
        );

        logger.info("{} Loaded view: {} with css: {}", PREFIX, fxmlPath, cssPath);
    }
}