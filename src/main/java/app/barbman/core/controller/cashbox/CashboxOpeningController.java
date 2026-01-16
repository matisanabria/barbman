package app.barbman.core.controller.cashbox;

import app.barbman.core.model.cashbox.CashboxClosure;
import app.barbman.core.model.human.User;
import app.barbman.core.service.cashbox.CashboxService;
import app.barbman.core.util.AlertUtil;
import app.barbman.core.util.NumberFormatterUtil;
import app.barbman.core.util.SessionManager;
import app.barbman.core.util.window.WindowManager;
import app.barbman.core.util.window.WindowRequest;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class CashboxOpeningController {

    private static final Logger logger = LogManager.getLogger(CashboxOpeningController.class);
    private static final String PREFIX = "[CASHBOX-OPENING]";

    private final CashboxService cashboxService;

    public CashboxOpeningController() {
        // hardcoded
        this.cashboxService = new CashboxService(
                new app.barbman.core.repositories.cashbox.opening.CashboxOpeningRepositoryImpl(),
                new app.barbman.core.repositories.cashbox.closure.CashboxClosureRepositoryImpl(),
                new app.barbman.core.repositories.cashbox.movement.CashboxMovementRepositoryImpl()
        );
    }

    // ============================================================
    // FXML
    // ============================================================

    @FXML private Label periodLabel;
    @FXML private Label openedByLabel;
    @FXML private Label openedAtLabel;

    @FXML private TextField cashField;
    @FXML private TextField bankField;
    @FXML private TextArea notesArea;

    @FXML private Label previousCashLabel;
    @FXML private Label previousBankLabel;
    @FXML private Label previousTotalLabel;
    @FXML private Label totalOpeningLabel;


    // ============================================================
    // INIT
    // ============================================================

    @FXML
    public void initialize() {
        User admin = SessionManager.getActiveUser();

        LocalDate periodStart = cashboxService.getCurrentPeriodStart();
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        periodLabel.setText("Fecha de inicio de periodo: " + periodStart);
        openedByLabel.setText("Abierto por: " + admin.getName());
        openedAtLabel.setText("Hora de apertura: " + time);

        CashboxClosure last = cashboxService.getLastClosure();

        if (last != null) {
            previousCashLabel.setText(
                    "Previous cash: " + NumberFormatterUtil.format(last.getExpectedCash())
            );
            previousBankLabel.setText(
                    "Previous bank: " + NumberFormatterUtil.format(last.getExpectedBank())
            );
            previousTotalLabel.setText(
                    "Previous total: " + NumberFormatterUtil.format(last.getExpectedTotal())
            );
        } else {
            previousCashLabel.setText("Previous cash: 0.00");
            previousBankLabel.setText("Previous bank: 0.00");
            previousTotalLabel.setText("Previous total: 0.00");
        }

        // Listen to changes on fields to update total
        NumberFormatterUtil.applyToTextField(cashField);
        NumberFormatterUtil.applyToTextField(bankField);

        cashField.textProperty().addListener((obs, o, n) -> updateTotal());
        bankField.textProperty().addListener((obs, o, n) -> updateTotal());

    }

    // ============================================================
    // ACTIONS
    // ============================================================

    @FXML
    private void onOpenCashbox() {
        try {
            double cash = parseAmount(cashField.getText());
            double bank = parseAmount(bankField.getText());
            String notes = notesArea.getText();

            User admin = SessionManager.getActiveUser();

            if (admin == null) {
                AlertUtil.showError("No active user session.", "An unexpected error occurred while opening the cashbox.");
                return;
            }

            cashboxService.openCashbox(
                    cash,
                    bank,
                    admin.getId(),
                    notes
            );

            logger.info("{} Cashbox opened successfully", PREFIX);

            AlertUtil.showInfo(
                    "Cashbox opened",
                    "The cashbox was opened successfully.\nYou can now continue working."
            );

            // volver al main como ventana exclusiva
            WindowManager.showExclusive(
                    WindowRequest.builder()
                            .fxml("/app/barbman/core/view/main-view.fxml")
                            .build()
            );

        } catch (IllegalArgumentException e) {
            // errores de parseo / validación
            logger.warn("{} Invalid input while opening cashbox", PREFIX, e);

            AlertUtil.showWarning(
                    "Invalid values",
                    e.getMessage()
            );

        } catch (IllegalStateException e) {
            // errores de dominio (ya abierta, estado inválido)
            logger.warn("{} Cashbox state error", PREFIX, e);

            AlertUtil.showWarning(
                    "Cashbox error",
                    e.getMessage()
            );

        } catch (Exception e) {
            // error inesperado
            logger.error("{} Failed to open cashbox", PREFIX, e);

            AlertUtil.showError(
                    "Unexpected error",
                    "An unexpected error occurred while opening the cashbox."
            );
        }
    }

    @FXML
    private void onCancel() {
        // cancelar = logout
        Stage stage = (Stage) cashField.getScene().getWindow();
        SessionManager.endSession();

        // volver al login
        WindowManager.switchWindow(
                stage,
                WindowRequest.builder()
                        .fxml("/app/barbman/core/view/login-view.fxml")
                        .css("/app/barbman/core/style/login.css")
                        .build()
        );
    }

    private void updateTotal() {
        double cash = parseAmount(cashField.getText());
        double bank = parseAmount(bankField.getText());

        double total = cash + bank;
        totalOpeningLabel.setText(NumberFormatterUtil.format(total));
    }


    private double parseAmount(String value) {
        if (value == null || value.isBlank()) return 0;

        // quitar separadores de miles
        String clean = value.replace(".", "");
        return Double.parseDouble(clean);
    }

}
