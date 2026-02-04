package app.barbman.core.controller.cashbox;

import app.barbman.core.model.cashbox.CashboxClosure;
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
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.format.DateTimeFormatter;

/**
 * Controller for cashbox closure confirmation modal.
 */
public class CashboxClosureController {

    private static final Logger logger = LogManager.getLogger(CashboxClosureController.class);
    private static final String PREFIX = "[CASHBOX-CLOSURE]";

    private final CashboxService cashboxService;
    private Runnable onClosureSuccess;

    // ============================================================
    // FXML
    // ============================================================

    @FXML private Label periodLabel;
    @FXML private Label cashLabel;
    @FXML private Label bankLabel;
    @FXML private Label totalLabel;
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
    }

    /**
     * Loads the closure preview with expected amounts.
     */
    private void loadClosurePreview() {
        try {
            var periodStart = cashboxService.getCurrentPeriodStart();
            var periodEnd = cashboxService.getCurrentPeriodEnd();

            // Check if already closed
            var closureRepo = new CashboxClosureRepositoryImpl();
            if (closureRepo.existsForPeriod(periodStart)) {
                AlertUtil.showWarning(
                        "Caja ya cerrada",
                        "Este período ya fue cerrado."
                );
                closeDialog();
                return;
            }

            // Check if opened
            if (!cashboxService.isCurrentPeriodOpened()) {
                AlertUtil.showWarning(
                        "Caja no abierta",
                        "No hay una caja abierta para este período."
                );
                closeDialog();
                return;
            }

            // Calculate expected amounts
            double expectedCash = calculateExpectedCash();
            double expectedBank = calculateExpectedBank();
            double expectedTotal = expectedCash + expectedBank;

            // Display info
            periodLabel.setText(String.format("Período: %s → %s",
                    periodStart.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    periodEnd.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));

            cashLabel.setText("💵 Efectivo esperado: " + NumberFormatterUtil.format(expectedCash) + " Gs");
            bankLabel.setText("🏦 Banco esperado: " + NumberFormatterUtil.format(expectedBank) + " Gs");
            totalLabel.setText("💰 Total esperado: " + NumberFormatterUtil.format(expectedTotal) + " Gs");

            logger.info("{} Closure preview loaded for period {}", PREFIX, periodStart);

        } catch (Exception e) {
            logger.error("{} Error loading closure preview", PREFIX, e);
            AlertUtil.showError("Error", "No se pudo cargar la vista previa del cierre.");
            closeDialog();
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

            String notes = notesArea.getText();
            if (notes == null || notes.isBlank()) {
                notes = "Cierre manual";
            }

            // Close cashbox
            CashboxClosure closure = cashboxService.closeCurrentPeriod(
                    admin.getId(),
                    notes
            );

            logger.info("{} Cashbox closed successfully for period {}",
                    PREFIX, closure.getPeriodStartDate());

            AlertUtil.showInfo(
                    "Caja Cerrada",
                    String.format("Cierre exitoso. Total esperado: %s Gs",
                            NumberFormatterUtil.format(closure.getExpectedTotal()))
            );

            // Notify parent and close
            if (onClosureSuccess != null) {
                onClosureSuccess.run();
            }

            closeDialog();

        } catch (IllegalStateException e) {
            logger.warn("{} Closure validation error", PREFIX, e);
            AlertUtil.showWarning("Validación", e.getMessage());

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

    private double calculateExpectedCash() {
        var start = cashboxService.getCurrentPeriodStart();
        var end = cashboxService.getCurrentPeriodEnd();

        var saleRepo = new app.barbman.core.repositories.sales.SaleRepositoryImpl();
        var salesService = new app.barbman.core.service.sales.SalesService(saleRepo);
        var expenseRepo = new app.barbman.core.repositories.expense.ExpenseRepositoryImpl();

        double salesCash = salesService.getTotalForPaymentMethodInPeriod(0, start, end);
        double expensesCash = expenseRepo.sumTotalByPaymentMethodAndPeriod(0, start, end);

        return salesCash - expensesCash;
    }

    private double calculateExpectedBank() {
        var start = cashboxService.getCurrentPeriodStart();
        var end = cashboxService.getCurrentPeriodEnd();

        var saleRepo = new app.barbman.core.repositories.sales.SaleRepositoryImpl();
        var salesService = new app.barbman.core.service.sales.SalesService(saleRepo);
        var expenseRepo = new app.barbman.core.repositories.expense.ExpenseRepositoryImpl();

        double salesBank =
                salesService.getTotalForPaymentMethodInPeriod(1, start, end)
                        + salesService.getTotalForPaymentMethodInPeriod(2, start, end)
                        + salesService.getTotalForPaymentMethodInPeriod(3, start, end);

        double expensesBank =
                expenseRepo.sumTotalByPaymentMethodAndPeriod(1, start, end)
                        + expenseRepo.sumTotalByPaymentMethodAndPeriod(2, start, end)
                        + expenseRepo.sumTotalByPaymentMethodAndPeriod(3, start, end);

        return salesBank - expensesBank;
    }

    public void setOnClosureSuccess(Runnable callback) {
        this.onClosureSuccess = callback;
    }

    private void closeDialog() {
        Stage stage = (Stage) periodLabel.getScene().getWindow();
        stage.close();
    }
}