package app.barbman.core.controller.salary;

import app.barbman.core.model.human.User;
import app.barbman.core.repositories.expense.ExpenseRepositoryImpl;
import app.barbman.core.repositories.salaries.advance.AdvanceRepositoryImpl;
import app.barbman.core.repositories.users.UsersRepositoryImpl;
import app.barbman.core.service.expenses.ExpensesService;
import app.barbman.core.service.salaries.advances.AdvancesService;
import app.barbman.core.service.users.UsersService;
import app.barbman.core.util.AlertUtil;
import app.barbman.core.util.NumberFormatterUtil;
import app.barbman.core.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class AdvancesController implements Initializable {
    @FXML private ComboBox<String> employeeComboBox;
    @FXML private TextField amountField;
    @FXML private ChoiceBox<String> paymentMethodChoice;
    @FXML private TextArea descriptionArea;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    private static final Logger logger = LogManager.getLogger(AdvancesController.class);
    private static final String PREFIX = "[ADV-CONTROLLER]";

    private final AdvancesService advancesService;
    private final UsersService usersService;

    private List<User> employees;
    private SalaryController parentController;

    public AdvancesController() {
        var advanceRepo = new AdvanceRepositoryImpl();
        var expenseRepo = new ExpenseRepositoryImpl();
        var userRepo = new UsersRepositoryImpl();

        var cashboxService = new app.barbman.core.service.cashbox.CashboxService(
                new app.barbman.core.repositories.cashbox.opening.CashboxOpeningRepositoryImpl(),
                new app.barbman.core.repositories.cashbox.closure.CashboxClosureRepositoryImpl(),
                new app.barbman.core.repositories.cashbox.movement.CashboxMovementRepositoryImpl()
        );
        var expensesService = new ExpensesService(expenseRepo, cashboxService);
        this.advancesService = new AdvancesService();
        this.usersService = new UsersService(userRepo);
    }


    /**
     * Inicializa la vista, cargando la lista de barberos y las formas de pago disponibles.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupPaymentMethods();
        setupAmountField();
        loadEmployees();

        logger.info("{} Advances dialog initialized", PREFIX);
    }

    private void setupPaymentMethods() {
        paymentMethodChoice.setItems(FXCollections.observableArrayList(
                "Efectivo",
                "Transferencia"
        ));
        paymentMethodChoice.setValue("Efectivo");
    }

    private void setupAmountField() {
        NumberFormatterUtil.applyToTextField(amountField);
    }

    private void loadEmployees() {
        try {
            // Load all employees (admins and users)
            employees = usersService.getAllUsers().stream()
                    .filter(u -> Objects.equals(u.getRole(), "user") || Objects.equals(u.getRole(), "admin"))
                    .toList();

            // Populate ComboBox with employee names
            List<String> employeeNames = employees.stream()
                    .map(User::getName)
                    .toList();

            employeeComboBox.setItems(FXCollections.observableArrayList(employeeNames));

            logger.info("{} Loaded {} employees", PREFIX, employees.size());

        } catch (Exception e) {
            logger.error("{} Error loading employees: {}", PREFIX, e.getMessage(), e);
            AlertUtil.showError("Error", "No se pudieron cargar los empleados.");
        }
    }

    /**
     * Maneja el evento del botón Cancelar.
     * Cierra la ventana actual.
     */
    @FXML
    private void onCancel() {
        logger.info("{} Advance registration cancelled", PREFIX);
        closeDialog();
    }

    // Y agregar método helper:
    private void closeDialog() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    /**
     * Carga y guarda un nuevo adelanto en la base de datos.
     * Se valida la entrada desde los campos de la vista.
     */
    @FXML
    private void onSave() {
        logger.info("{} Attempting to save advance...", PREFIX);

        try {
            // Validate inputs
            validateInputs();

            // Get selected employee
            String selectedName = employeeComboBox.getValue();
            User employee = employees.stream()
                    .filter(u -> u.getName().equals(selectedName))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));

            // Parse amount
            double amount = parseAmount(amountField.getText());

            // Get payment method ID
            int paymentMethodId = getPaymentMethodId();

            // Get description (optional)
            String description = descriptionArea.getText();
            if (description == null || description.isBlank()) {
                description = "Adelanto de sueldo";
            }

            // Save advance
            advancesService.saveAdvance(
                    employee.getId(),
                    amount,
                    paymentMethodId,
                    description
            );

            logger.info("{} Advance saved successfully -> user={}, amount={}, method={}",
                    PREFIX, employee.getName(), amount, paymentMethodId);

            // Show success
            AlertUtil.showInfo(
                    "Adelanto Registrado",
                    String.format("Se registró un adelanto de %s Gs para %s.",
                            NumberFormatterUtil.format(amount),
                            employee.getName())
            );

            // Refresh parent table if exists
            if (parentController != null) {
                parentController.reloadData();
            }

            // Close dialog
            closeDialog();

        } catch (IllegalArgumentException e) {
            logger.warn("{} Validation error: {}", PREFIX, e.getMessage());
            AlertUtil.showWarning("Validación", e.getMessage());

        } catch (Exception e) {
            logger.error("{} Error saving advance: {}", PREFIX, e.getMessage(), e);
            AlertUtil.showError(
                    "Error",
                    "No se pudo registrar el adelanto: " + e.getMessage()
            );
        }
    }

    public void setParentController(SalaryController parent) {
        this.parentController = parent;
    }

    private void validateInputs() {
        if (employeeComboBox.getValue() == null || employeeComboBox.getValue().isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar un empleado.");
        }

        if (amountField.getText() == null || amountField.getText().isBlank()) {
            throw new IllegalArgumentException("Debe ingresar un monto.");
        }

        double amount = parseAmount(amountField.getText());
        if (amount <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a 0.");
        }

        if (paymentMethodChoice.getValue() == null) {
            throw new IllegalArgumentException("Debe seleccionar un método de pago.");
        }
    }

    private int getPaymentMethodId() {
        String selected = paymentMethodChoice.getValue();
        return switch (selected) {
            case "Efectivo" -> 1;
            case "Transferencia" -> 2;
            case "Tarjeta" -> 3;
            case "QR" -> 4;
            default -> 1;
        };
    }

    private double parseAmount(String value) {
        if (value == null || value.isBlank()) {
            return 0.0;
        }

        try {
            String clean = value.replace(".", "").replace(",", ".");
            return Double.parseDouble(clean);
        } catch (NumberFormatException e) {
            logger.warn("{} Invalid number format: {}", PREFIX, value);
            throw new IllegalArgumentException("El monto ingresado no es válido.");
        }
    }
}
