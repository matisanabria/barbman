package app.barbman.core.controller.salary;

import app.barbman.core.dto.SalaryDTO;
import app.barbman.core.model.human.User;
import app.barbman.core.model.salaries.Salary;
import app.barbman.core.repositories.expense.ExpenseRepositoryImpl;
import app.barbman.core.repositories.salaries.advance.AdvanceRepositoryImpl;
import app.barbman.core.repositories.salaries.salaries.SalariesRepositoryImpl;
import app.barbman.core.repositories.sales.services.serviceheader.ServiceHeaderRepositoryImpl;
import app.barbman.core.repositories.users.UsersRepositoryImpl;
import app.barbman.core.service.expenses.ExpensesService;
import app.barbman.core.service.salaries.SalariesService;
import app.barbman.core.service.salaries.advances.AdvancesService;
import app.barbman.core.service.salaries.period.SalaryPeriodResolver;
import app.barbman.core.service.sales.services.ServiceHeaderService;
import app.barbman.core.service.users.UsersService;
import app.barbman.core.util.AlertUtil;
import app.barbman.core.util.NumberFormatterUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * Controller for the salary payment confirmation dialog.
 * Displays salary details, allows adding a bonus, selecting payment method,
 * and confirms the final payment.
 */
public class ConfirmSalaryController implements Initializable {

    private static final Logger logger = LogManager.getLogger(ConfirmSalaryController.class);
    private static final String PREFIX = "[CONFIRM-SALARY]";

    // ============================================================
    // FXML COMPONENTS
    // ============================================================

    @FXML private ChoiceBox<String> paymentTypeChoiceBox;
    @FXML private TextField bonusField;
    @FXML private TextField manualAmountField;
    @FXML private Label lblProduction;
    @FXML private Label lblAdvances;
    @FXML private Label lblFinalAmount;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;
    @FXML private VBox manualMontoBox;

    // ============================================================
    // SERVICES
    // ============================================================

    private final SalariesService salariesService;
    private final UsersService usersService;

    // ============================================================
    // STATE
    // ============================================================

    private SalaryController parentController;
    private User user;
    private SalaryDTO salaryDTO;
    private LocalDate currentPeriodReference;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public ConfirmSalaryController() {
        // Initialize repositories
        var salariesRepo = new SalariesRepositoryImpl();
        var expenseRepo = new ExpenseRepositoryImpl();
        var advanceRepo = new AdvanceRepositoryImpl();
        var serviceHeaderRepo = new ServiceHeaderRepositoryImpl();
        var userRepo = new UsersRepositoryImpl();

        // Initialize services
        var cashboxService = new app.barbman.core.service.cashbox.CashboxService(
                new app.barbman.core.repositories.cashbox.opening.CashboxOpeningRepositoryImpl(),
                new app.barbman.core.repositories.cashbox.closure.CashboxClosureRepositoryImpl(),
                new app.barbman.core.repositories.cashbox.movement.CashboxMovementRepositoryImpl()
        );
        var expensesService = new ExpensesService(expenseRepo, cashboxService);
        var advancesService = new AdvancesService();
        var serviceHeaderService = new ServiceHeaderService(serviceHeaderRepo);
        var periodResolver = new SalaryPeriodResolver();
        this.usersService = new UsersService(userRepo);

        this.salariesService = new SalariesService(
                salariesRepo,
                expensesService,
                advancesService,
                serviceHeaderService,
                periodResolver
        );

        this.currentPeriodReference = LocalDate.now();
    }

    // ============================================================
    // INITIALIZATION
    // ============================================================

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Setup payment method options
        paymentTypeChoiceBox.setItems(FXCollections.observableArrayList(
                "Efectivo",
                "Transferencia"
        ));
        paymentTypeChoiceBox.setValue("Efectivo");

        // Hide manual amount box by default (only shown for paymentType = 0)
        manualMontoBox.setVisible(false);
        manualMontoBox.setManaged(false);

        // Apply number formatters
        NumberFormatterUtil.applyToTextField(manualAmountField);
        NumberFormatterUtil.applyToTextField(bonusField);

        // Listen to bonus changes to update final amount
        bonusField.textProperty().addListener((obs, oldVal, newVal) -> updateFinalAmount());

        logger.info("{} ConfirmSalary dialog initialized", PREFIX);
    }

    // ============================================================
    // PUBLIC SETTERS (called from parent)
    // ============================================================

    /**
     * Sets the salary DTO and loads the user data.
     * Must be called after the dialog is shown.
     */
    public void setSalaryDTO(SalaryDTO dto) {
        this.salaryDTO = dto;
        this.user = usersService.getUserById(dto.getUserId());

        if (user == null) {
            logger.error("{} User not found for ID: {}", PREFIX, dto.getUserId());
            AlertUtil.showError("Error", "No se encontró el usuario.");
            closeDialog();
            return;
        }

        loadSalaryDetails();
        checkManualAmountRequired();
    }

    /**
     * Sets the parent controller to refresh the table after payment.
     */
    public void setParentController(SalaryController parent) {
        this.parentController = parent;
    }

    // ============================================================
    // LOAD SALARY DETAILS
    // ============================================================

    private void loadSalaryDetails() {
        lblProduction.setText(
                "Producción: " + NumberFormatterUtil.format(salaryDTO.getProduction()) + " Gs"
        );

        lblAdvances.setText(
                "Adelantos: " + NumberFormatterUtil.format(salaryDTO.getAdvances()) + " Gs"
        );

        updateFinalAmount();

        logger.info("{} Salary details loaded for user: {}", PREFIX, user.getName());
    }

    /**
     * Checks if manual amount input is required (for paymentType = 0).
     */
    private void checkManualAmountRequired() {
        if (user.getPaymentType() == 0) {
            manualMontoBox.setVisible(true);
            manualMontoBox.setManaged(true);
            logger.info("{} Manual amount required for user: {}", PREFIX, user.getName());
        }
    }

    /**
     * Updates the final amount label based on calculated amount + bonus.
     */
    private void updateFinalAmount() {
        if (salaryDTO == null) return;

        double bonus = parseAmount(bonusField.getText());
        double finalAmount = salaryDTO.getFinalAmount() + bonus;

        lblFinalAmount.setText(
                "Monto final: " + NumberFormatterUtil.format(finalAmount) + " Gs"
        );
    }

    // ============================================================
    // BUTTON HANDLERS
    // ============================================================

    @FXML
    private void onPay() {
        logger.info("{} Initiating payment for user: {}", PREFIX, user.getName());

        try {
            // Validate inputs
            validateInputs();

            // Parse bonus
            double bonus = parseAmount(bonusField.getText());

            // Get payment method ID
            int paymentMethodId = getPaymentMethodId();

            // Calculate or get manual salary
            Salary salary;
            if (user.getPaymentType() == 0) {
                // Manual payment: override the calculated amount
                salary = salariesService.calculateSalary(user, currentPeriodReference, 0);
                double manualAmount = parseAmount(manualAmountField.getText());
                salary.setAmountPaid(manualAmount);
            } else {
                // Automatic calculation
                salary = salariesService.calculateSalary(user, currentPeriodReference, 0);
            }

            // Process payment (this will add bonus, create expense, link everything)
            salariesService.paySalary(user, salary, paymentMethodId, bonus);

            logger.info("{} Salary paid successfully -> User: {}, Amount: {}, Method: {}",
                    PREFIX, user.getName(), salary.getAmountPaid(), paymentMethodId);

            // Show success message
            AlertUtil.showInfo(
                    "Pago Exitoso",
                    String.format("Se pagó %s Gs a %s correctamente.",
                            NumberFormatterUtil.format(salary.getAmountPaid()),
                            user.getName()
                    )
            );

            // Refresh parent table
            if (parentController != null) {
                parentController.reloadData();
            }

            // Close dialog
            closeDialog();

        } catch (IllegalArgumentException e) {
            logger.warn("{} Validation error: {}", PREFIX, e.getMessage());
            AlertUtil.showWarning("Validación", e.getMessage());

        } catch (IllegalStateException e) {
            logger.warn("{} Business logic error: {}", PREFIX, e.getMessage());
            AlertUtil.showWarning("Error", e.getMessage());

        } catch (Exception e) {
            logger.error("{} Error processing payment: {}", PREFIX, e.getMessage(), e);
            AlertUtil.showError(
                    "Error al Pagar",
                    "No se pudo procesar el pago: " + e.getMessage()
            );
        }
    }

    @FXML
    private void onCancel() {
        logger.info("{} Payment cancelled by user", PREFIX);
        closeDialog();
    }

    // ============================================================
    // VALIDATION
    // ============================================================

    private void validateInputs() {
        // Check if manual amount is required and provided
        if (user.getPaymentType() == 0) {
            if (manualAmountField.getText() == null || manualAmountField.getText().isBlank()) {
                throw new IllegalArgumentException("Debe ingresar un monto manual.");
            }

            double amount = parseAmount(manualAmountField.getText());
            if (amount <= 0) {
                throw new IllegalArgumentException("El monto manual debe ser mayor a 0.");
            }
        }

        // Validate bonus (optional, but if provided must be valid)
        if (bonusField.getText() != null && !bonusField.getText().isBlank()) {
            double bonus = parseAmount(bonusField.getText());
            if (bonus < 0) {
                throw new IllegalArgumentException("El bono no puede ser negativo.");
            }
        }

        // Payment method must be selected
        if (paymentTypeChoiceBox.getValue() == null) {
            throw new IllegalArgumentException("Debe seleccionar un método de pago.");
        }
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    /**
     * Gets the payment method ID based on the selected option.
     */
    private int getPaymentMethodId() {
        String selected = paymentTypeChoiceBox.getValue();
        return switch (selected) {
            case "Efectivo" -> 1;
            case "Transferencia" -> 2;
            default -> 1; // Default to cash
        };
    }

    /**
     * Creates a manual salary record for paymentType = 0.
     */
    private Salary createManualSalary(double amount) {
        var range = new app.barbman.core.service.salaries.period.SalaryPeriodResolver()
                .resolve(user, currentPeriodReference);

        return Salary.builder()
                .userId(user.getId())
                .startDate(range.getStart())
                .endDate(range.getEnd())
                .totalProduction(salaryDTO.getProduction())
                .amountPaid(amount)
                .payTypeSnapshot(user.getPaymentType())
                .build();
    }

    /**
     * Parses a formatted number string to double.
     */
    private double parseAmount(String value) {
        if (value == null || value.isBlank()) {
            return 0.0;
        }

        try {
            String clean = value.replace(".", "").replace(",", ".");
            return Double.parseDouble(clean);
        } catch (NumberFormatException e) {
            logger.warn("{} Invalid number format: {}", PREFIX, value);
            return 0.0;
        }
    }

    /**
     * Closes the dialog window.
     */
    private void closeDialog() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }
}
