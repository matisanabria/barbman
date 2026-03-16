package app.barbman.core.controller.appointments;

import app.barbman.core.service.OnBarberApiClient;
import app.barbman.core.service.OnBarberApiClient.AppointmentDTO;
import app.barbman.core.service.OnBarberApiClient.BarberDTO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.util.List;

public class AppointmentModalController {

    private static final Logger logger = LogManager.getLogger(AppointmentModalController.class);

    // Common
    @FXML private Label modalTitle;
    @FXML private VBox viewPane;
    @FXML private VBox createPane;

    // View mode fields
    @FXML private Label viewBarber;
    @FXML private Label viewDate;
    @FXML private Label viewTime;
    @FXML private Label viewClient;
    @FXML private Label viewPhone;
    @FXML private Label viewStatus;
    @FXML private Button btnConfirm;
    @FXML private Button btnComplete;
    @FXML private Button btnCancel;

    // Create mode fields
    @FXML private ComboBox<BarberDTO> barberCombo;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> timeCombo;
    @FXML private Button btnLoadSlots;
    @FXML private TextField clientNameField;
    @FXML private TextField clientPhoneField;
    @FXML private Button btnCreate;

    private OnBarberApiClient apiClient;
    private AppointmentDTO currentAppointment;
    private Runnable onSaved;

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    // ============================================================
    // VIEW MODE
    // ============================================================

    public void setViewMode(AppointmentDTO appointment, OnBarberApiClient apiClient) {
        this.apiClient = apiClient;
        this.currentAppointment = appointment;

        modalTitle.setText("Detalle de Reserva");
        viewPane.setVisible(true);
        viewPane.setManaged(true);
        createPane.setVisible(false);
        createPane.setManaged(false);

        viewBarber.setText(appointment.getBarberName());
        viewDate.setText(appointment.getFormattedDate());
        viewTime.setText(appointment.getFormattedTime());
        viewClient.setText(appointment.getClientName());
        viewPhone.setText(appointment.getClientPhone());
        viewStatus.setText(appointment.getStatusDisplay());

        // Style the status label
        viewStatus.getStyleClass().removeIf(s -> s.startsWith("appointments-badge"));
        viewStatus.getStyleClass().addAll("appointments-badge", "appointments-badge-" + appointment.getStatus());

        // Toggle action buttons based on status
        String status = appointment.getStatus();
        btnConfirm.setVisible("pending".equals(status));
        btnConfirm.setManaged("pending".equals(status));
        btnComplete.setVisible("confirmed".equals(status));
        btnComplete.setManaged("confirmed".equals(status));
        btnCancel.setVisible("pending".equals(status) || "confirmed".equals(status));
        btnCancel.setManaged("pending".equals(status) || "confirmed".equals(status));

        resizeToContent();
    }

    @FXML
    private void handleConfirm() {
        updateAndClose("confirmed");
    }

    @FXML
    private void handleComplete() {
        updateAndClose("completed");
    }

    @FXML
    private void handleCancelAppointment() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancelar Reserva");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Seguro que deseas cancelar esta reserva?");

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                updateAndClose("cancelled");
            }
        });
    }

    private void updateAndClose(String newStatus) {
        disableAll(true);

        Thread thread = new Thread(() -> {
            try {
                apiClient.updateAppointmentStatus(currentAppointment.getId(), newStatus);
                Platform.runLater(() -> {
                    if (onSaved != null) onSaved.run();
                    closeModal();
                });
            } catch (Exception e) {
                logger.error("[APPOINTMENT-MODAL] Failed to update: {}", e.getMessage());
                Platform.runLater(() -> {
                    disableAll(false);
                    showError("Error al actualizar: " + e.getMessage());
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // ============================================================
    // CREATE MODE
    // ============================================================

    public void setCreateMode(OnBarberApiClient apiClient) {
        this.apiClient = apiClient;

        modalTitle.setText("Nueva Reserva");
        viewPane.setVisible(false);
        viewPane.setManaged(false);
        createPane.setVisible(true);
        createPane.setManaged(true);

        datePicker.setValue(LocalDate.now());
        timeCombo.setDisable(true);
        btnCreate.setDisable(true);

        // Load barbers
        loadBarbers();

        // Enable slot loading when barber + date selected
        barberCombo.valueProperty().addListener((obs, old, val) -> checkSlotLoadReady());
        datePicker.valueProperty().addListener((obs, old, val) -> checkSlotLoadReady());

        // Enable create button when all fields filled
        timeCombo.valueProperty().addListener((obs, old, val) -> checkCreateReady());
        clientNameField.textProperty().addListener((obs, old, val) -> checkCreateReady());
        clientPhoneField.textProperty().addListener((obs, old, val) -> checkCreateReady());
    }

    /**
     * Pre-filled create mode: barber, date, and time are already selected.
     * The user only needs to enter client name and phone.
     */
    public void setCreateModePrefilled(OnBarberApiClient apiClient, BarberDTO barber, LocalDate date, String time) {
        this.apiClient = apiClient;

        modalTitle.setText("Nueva Reserva — " + barber.getName());
        viewPane.setVisible(false);
        viewPane.setManaged(false);
        createPane.setVisible(true);
        createPane.setManaged(true);

        // Pre-fill and lock barber, date, time
        barberCombo.setItems(FXCollections.observableArrayList(barber));
        barberCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(BarberDTO b) { return b != null ? b.getName() : ""; }
            @Override
            public BarberDTO fromString(String s) { return null; }
        });
        barberCombo.setValue(barber);
        barberCombo.setDisable(true);

        datePicker.setValue(date);
        datePicker.setDisable(true);

        timeCombo.setItems(FXCollections.observableArrayList(time));
        timeCombo.setValue(time);
        timeCombo.setDisable(true);

        btnLoadSlots.setVisible(false);
        btnLoadSlots.setManaged(false);

        btnCreate.setDisable(true);

        // Enable create button when client fields filled
        clientNameField.textProperty().addListener((obs, old, val) -> checkCreateReady());
        clientPhoneField.textProperty().addListener((obs, old, val) -> checkCreateReady());

        resizeToContent();
    }

    private void loadBarbers() {
        Thread thread = new Thread(() -> {
            try {
                List<BarberDTO> barbers = apiClient.getBarbers();
                Platform.runLater(() -> {
                    barberCombo.setItems(FXCollections.observableArrayList(barbers));
                    barberCombo.setConverter(new StringConverter<>() {
                        @Override
                        public String toString(BarberDTO b) { return b != null ? b.getName() : ""; }
                        @Override
                        public BarberDTO fromString(String s) { return null; }
                    });
                });
            } catch (Exception e) {
                logger.error("[APPOINTMENT-MODAL] Failed to load barbers: {}", e.getMessage());
                Platform.runLater(() -> showError("Error al cargar barberos: " + e.getMessage()));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void checkSlotLoadReady() {
        boolean ready = barberCombo.getValue() != null && datePicker.getValue() != null;
        btnLoadSlots.setDisable(!ready);
    }

    private void checkCreateReady() {
        boolean ready = barberCombo.getValue() != null
                && datePicker.getValue() != null
                && timeCombo.getValue() != null
                && clientNameField.getText() != null && !clientNameField.getText().isBlank()
                && clientPhoneField.getText() != null && !clientPhoneField.getText().isBlank();
        btnCreate.setDisable(!ready);
    }

    @FXML
    private void handleLoadSlots() {
        BarberDTO barber = barberCombo.getValue();
        LocalDate date = datePicker.getValue();
        if (barber == null || date == null) return;

        btnLoadSlots.setDisable(true);
        timeCombo.setDisable(true);
        timeCombo.getItems().clear();

        Thread thread = new Thread(() -> {
            try {
                List<String> slots = apiClient.getAvailableSlots(barber.getId(), date);
                Platform.runLater(() -> {
                    if (slots.isEmpty()) {
                        timeCombo.setPromptText("Sin horarios disponibles");
                    } else {
                        timeCombo.setItems(FXCollections.observableArrayList(slots));
                        timeCombo.setPromptText("Seleccionar hora");
                        timeCombo.setDisable(false);
                    }
                    btnLoadSlots.setDisable(false);
                });
            } catch (Exception e) {
                logger.error("[APPOINTMENT-MODAL] Failed to load slots: {}", e.getMessage());
                Platform.runLater(() -> {
                    showError("Error al cargar horarios: " + e.getMessage());
                    btnLoadSlots.setDisable(false);
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleCreate() {
        BarberDTO barber = barberCombo.getValue();
        LocalDate date = datePicker.getValue();
        String time = timeCombo.getValue();
        String name = clientNameField.getText().trim();
        String phone = clientPhoneField.getText().trim();

        if (barber == null || date == null || time == null || name.isEmpty() || phone.isEmpty()) {
            showError("Completar todos los campos.");
            return;
        }

        btnCreate.setDisable(true);

        Thread thread = new Thread(() -> {
            try {
                apiClient.createAppointment(barber.getId(), name, phone, date, time);
                Platform.runLater(() -> {
                    if (onSaved != null) onSaved.run();
                    closeModal();
                });
            } catch (Exception e) {
                logger.error("[APPOINTMENT-MODAL] Failed to create: {}", e.getMessage());
                Platform.runLater(() -> {
                    btnCreate.setDisable(false);
                    showError("Error al crear reserva: " + e.getMessage());
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // ============================================================
    // COMMON
    // ============================================================

    @FXML
    private void handleClose() {
        closeModal();
    }

    private void closeModal() {
        Stage stage = (Stage) modalTitle.getScene().getWindow();
        stage.close();
    }

    private void disableAll(boolean disabled) {
        btnConfirm.setDisable(disabled);
        btnComplete.setDisable(disabled);
        btnCancel.setDisable(disabled);
    }

    private void resizeToContent() {
        Platform.runLater(() -> {
            if (modalTitle.getScene() != null && modalTitle.getScene().getWindow() instanceof Stage stage) {
                stage.sizeToScene();
            }
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
