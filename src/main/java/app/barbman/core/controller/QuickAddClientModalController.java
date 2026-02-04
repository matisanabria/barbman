package app.barbman.core.controller;

import app.barbman.core.model.human.Client;
import app.barbman.core.repositories.client.ClientRepositoryImpl;
import app.barbman.core.service.clients.ClientService;
import app.barbman.core.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * Quick modal to add a client during sale flow.
 */
public class QuickAddClientModalController implements Initializable {

    private static final Logger logger = LogManager.getLogger(QuickAddClientModalController.class);
    private static final String PREFIX = "[QUICK-CLIENT]";

    // ============================================================
    // FXML
    // ============================================================

    @FXML private TextField nameField;
    @FXML private TextField documentField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;

    // ============================================================
    // STATE
    // ============================================================

    private final ClientService clientService;
    private Consumer<Client> onClientCreated; // Callback

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public QuickAddClientModalController() {
        this.clientService = new ClientService(new ClientRepositoryImpl());
    }

    // ============================================================
    // INIT
    // ============================================================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("{} Quick add client modal initialized", PREFIX);
    }

    // ============================================================
    // PUBLIC METHODS
    // ============================================================

    /**
     * Set callback to be called when client is created.
     */
    public void setOnClientCreated(Consumer<Client> callback) {
        this.onClientCreated = callback;
    }

    // ============================================================
    // ACTIONS
    // ============================================================

    @FXML
    private void onCancel() {
        logger.info("{} Cancel clicked", PREFIX);
        closeModal();
    }

    @FXML
    private void onSave() {
        logger.info("{} Save clicked", PREFIX);

        // Validate
        String name = nameField.getText();
        if (name == null || name.isBlank()) {
            AlertUtil.showWarning("Validacion", "El nombre es obligatorio.");
            return;
        }

        // Optional fields
        String document = documentField.getText();
        String phone = phoneField.getText();
        String email = emailField.getText();

        try {
            // Create client
            clientService.registerClient(name, document, phone, email, null);

            // Get the created client (find by name - last one with that name)
            Client createdClient = clientService.findAll().stream()
                    .filter(c -> c.getName().equals(name))
                    .reduce((first, second) -> second) // Get last
                    .orElse(null);

            logger.info("{} Client created successfully: {}", PREFIX, name);

            // Call callback if set
            if (onClientCreated != null && createdClient != null) {
                onClientCreated.accept(createdClient);
            }

            closeModal();

        } catch (Exception e) {
            logger.error("{} Error creating client", PREFIX, e);
            AlertUtil.showError("Error", "No se pudo crear el cliente: " + e.getMessage());
        }
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private void closeModal() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}