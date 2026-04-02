package app.barbman.core.controller.cashbox;

import app.barbman.core.model.cashbox.CashboxClosure;
import app.barbman.core.model.cashbox.CashboxOpening;
import app.barbman.core.repositories.cashbox.closure.CashboxClosureRepositoryImpl;
import app.barbman.core.repositories.cashbox.movement.CashboxMovementRepositoryImpl;
import app.barbman.core.repositories.cashbox.opening.CashboxOpeningRepositoryImpl;
import app.barbman.core.service.cashbox.CashboxService;
import app.barbman.core.util.AlertUtil;
import app.barbman.core.util.NumberFormatterUtil;
import app.barbman.core.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.format.DateTimeFormatter;

/**
 * Controller for cashbox closure with reconciliation.
 */
public class CashboxClosureController {

    private static final Logger logger = LogManager.getLogger(CashboxClosureController.class);
    private static final String PREFIX = "[CASHBOX-CLOSURE]";

    private final CashboxService cashboxService;
    private Runnable onClosureSuccess;

    private double expectedCash;
    private double expectedBank;

    // ============================================================
    // FXML
    // ============================================================

    @FXML private Label periodLabel;
    @FXML private Label expectedCashLabel;
    @FXML private Label expectedBankLabel;
    @FXML private TextField actualCashField;
    @FXML private TextField actualBankField;
    @FXML private Label cashNegativeWarning;
    @FXML private Label bankNegativeWarning;
    @FXML private Label cashDiscrepancyLabel;
    @FXML private Label bankDiscrepancyLabel;
    @FXML private Label expectedTotalLabel;
    @FXML private Label actualTotalLabel;
    @FXML private Label totalDiscrepancyLabel;
    @FXML private TextArea notesArea;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public CashboxClosureController() {
        this.cashboxService = new CashboxService(
                new CashboxOpeningRepositoryImpl(),
                new CashboxClosureRepositoryImpl(),
                new CashboxMovementRepositoryImpl()
        );
    }

    // ============================================================
    // INIT
    // ============================================================

    @FXML
    public void initialize() {
        loadClosurePreview();
        setupDiscrepancyListeners();
        updateDiscrepancies();
    }

    private void loadClosurePreview() {
        try {
            CashboxOpening opening = cashboxService.getCurrentOpening();
            if (opening == null) {
                AlertUtil.showWarning("Caja no abierta", "No hay una caja abierta.");
                closeDialog();
                return;
            }

            periodLabel.setText(String.format("Abierta desde: %s",
                    opening.getOpenedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));

            expectedCash = cashboxService.getExpectedCash(opening.getId());
            expectedBank = cashboxService.getExpectedBank(opening.getId());

            expectedCashLabel.setText(NumberFormatterUtil.format(expectedCash) + " Gs");
            expectedBankLabel.setText(NumberFormatterUtil.format(expectedBank) + " Gs");

            double expectedTotal = expectedCash + expectedBank;
            expectedTotalLabel.setText(NumberFormatterUtil.format(expectedTotal) + " Gs");

            // Initialize actual total and discrepancy
            actualTotalLabel.setText("0 Gs");
            totalDiscrepancyLabel.setText(NumberFormatterUtil.format(-expectedTotal) + " Gs");

            NumberFormatterUtil.applyToTextField(actualCashField);
            NumberFormatterUtil.applyToTextField(actualBankField);

            actualCashField.setText(NumberFormatterUtil.format(Math.max(0, expectedCash)));
            actualBankField.setText(NumberFormatterUtil.format(Math.max(0, expectedBank)));

            cashNegativeWarning.setVisible(expectedCash < 0);
            cashNegativeWarning.setManaged(expectedCash < 0);
            bankNegativeWarning.setVisible(expectedBank < 0);
            bankNegativeWarning.setManaged(expectedBank < 0);

            logger.info("{} Closure preview loaded for opening {}", PREFIX, opening.getId());

        } catch (Exception e) {
            logger.error("{} Error loading closure preview", PREFIX, e);
            AlertUtil.showError("Error", "No se pudo cargar la vista previa del cierre.");
            closeDialog();
        }
    }

    private void setupDiscrepancyListeners() {
        actualCashField.textProperty().addListener((obs, o, n) -> updateDiscrepancies());
        actualBankField.textProperty().addListener((obs, o, n) -> updateDiscrepancies());
    }

    private void updateDiscrepancies() {
        double actualCash = parseAmount(actualCashField.getText());
        double actualBank = parseAmount(actualBankField.getText());

        double cashDisc = actualCash - expectedCash;
        double bankDisc = actualBank - expectedBank;
        double totalDisc = cashDisc + bankDisc;

        cashDiscrepancyLabel.setText(formatDiscrepancy(cashDisc));
        bankDiscrepancyLabel.setText(formatDiscrepancy(bankDisc));

        double actualTotal = actualCash + actualBank;
        actualTotalLabel.setText(NumberFormatterUtil.format(actualTotal) + " Gs");
        totalDiscrepancyLabel.setText(formatDiscrepancy(totalDisc));

        // Color: green if 0, red if negative, yellow if positive
        setDiscrepancyStyle(cashDiscrepancyLabel, cashDisc);
        setDiscrepancyStyle(bankDiscrepancyLabel, bankDisc);
        setDiscrepancyStyle(totalDiscrepancyLabel, totalDisc);
    }

    private String formatDiscrepancy(double value) {
        String sign = value > 0 ? "+" : "";
        return sign + NumberFormatterUtil.format(value) + " Gs";
    }

    private void setDiscrepancyStyle(Label label, double value) {
        label.getStyleClass().removeAll("discrepancy-positive", "discrepancy-negative", "discrepancy-zero");
        if (value == 0) {
            label.getStyleClass().add("discrepancy-zero");
        } else if (value > 0) {
            label.getStyleClass().add("discrepancy-positive");
        } else {
            label.getStyleClass().add("discrepancy-negative");
        }
    }

    // ============================================================
    // ACTIONS
    // ============================================================

    @FXML
    private void onConfirmClosure() {
        try {
            var admin = SessionManager.getActiveUser();
            if (admin == null) {
                AlertUtil.showError("Error", "No hay usuario activo.");
                return;
            }

            double actualCash = parseAmount(actualCashField.getText());
            double actualBank = parseAmount(actualBankField.getText());

            String notes = notesArea.getText();
            if (notes == null || notes.isBlank()) {
                notes = "Cierre manual";
            }

            CashboxClosure closure = cashboxService.closeCashbox(
                    actualCash,
                    actualBank,
                    admin.getId(),
                    notes
            );

            logger.info("{} Cashbox closed successfully (openingId={})", PREFIX, closure.getOpeningId());

            String message = String.format(
                    "Cierre exitoso.\nEfectivo: %s Gs (dif: %s)\nBanco: %s Gs (dif: %s)",
                    NumberFormatterUtil.format(closure.getActualCash()),
                    formatDiscrepancy(closure.getCashDiscrepancy()),
                    NumberFormatterUtil.format(closure.getActualBank()),
                    formatDiscrepancy(closure.getBankDiscrepancy())
            );

            AlertUtil.showInfo("Caja Cerrada", message);

            if (onClosureSuccess != null) {
                onClosureSuccess.run();
            }

            closeDialog();

        } catch (IllegalStateException e) {
            logger.warn("{} Closure validation error", PREFIX, e);
            AlertUtil.showWarning("Validacion", e.getMessage());

        } catch (Exception e) {
            logger.error("{} Error closing cashbox", PREFIX, e);
            AlertUtil.showError("Error", "No se pudo cerrar la caja: " + e.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        logger.info("{} Closure cancelled by user", PREFIX);
        closeDialog();
    }

    // ============================================================
    // HELPERS
    // ============================================================

    public void setOnClosureSuccess(Runnable callback) {
        this.onClosureSuccess = callback;
    }

    private void closeDialog() {
        Stage stage = (Stage) periodLabel.getScene().getWindow();
        stage.close();
    }

    private double parseAmount(String value) {
        if (value == null || value.isBlank()) return 0;
        String clean = value.replace(".", "");
        try {
            return Double.parseDouble(clean);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
