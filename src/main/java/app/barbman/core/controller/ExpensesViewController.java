package app.barbman.core.controller;

import app.barbman.core.model.*;
import app.barbman.core.model.human.User;
import app.barbman.core.repositories.expense.ExpenseRepository;
import app.barbman.core.repositories.expense.ExpenseRepositoryImpl;
import app.barbman.core.repositories.paymentmethod.PaymentMethodRepositoryImpl;
import app.barbman.core.service.expenses.ExpensesService;
import app.barbman.core.service.paymentmethods.PaymentMethodsService;
import app.barbman.core.util.NumberFormatterUtil;
import app.barbman.core.util.SessionManager;
import app.barbman.core.util.TextFormatterUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

public class ExpensesViewController implements Initializable {

    private static final Logger logger = LogManager.getLogger(ExpensesViewController.class);
    private static final String PREFIX = "[EXP-VIEW]";

    @FXML private TableView<Expense> expensesTable;
    @FXML private TableColumn<Expense, String> colDate;
    @FXML private TableColumn<Expense, String> colType;
    @FXML private TableColumn<Expense, String> colAmount;
    @FXML private TableColumn<Expense, String> colPaymentMethod;
    @FXML private TableColumn<Expense, String> colDescription;

    @FXML private ComboBox<String> expenseTypeBox;
    @FXML private TextField amountField;
    @FXML private TextArea descriptionField;
    @FXML private Button saveButton;

    // Payment toggles (ahora son ToggleButtons individuales en lugar de HBox dinámico)
    @FXML private ToggleButton cashToggle;
    @FXML private ToggleButton transferToggle;

    @FXML private Label todayTotalLabel;
    @FXML private Label weekTotalLabel;
    @FXML private Label monthTotalLabel;
    @FXML private Label totalExpensesLabel;

    private final ToggleGroup paymentGroup = new ToggleGroup();
    private final PaymentMethodsService paymentMethodsService = new PaymentMethodsService(new PaymentMethodRepositoryImpl());
    private final ExpenseRepository expenseRepo = new ExpenseRepositoryImpl();
    private final ExpensesService expenseService = new ExpensesService(expenseRepo,
            new app.barbman.core.service.cashbox.CashboxService(
                    new app.barbman.core.repositories.cashbox.opening.CashboxOpeningRepositoryImpl(),
                    new app.barbman.core.repositories.cashbox.closure.CashboxClosureRepositoryImpl(),
                    new app.barbman.core.repositories.cashbox.movement.CashboxMovementRepositoryImpl()
            ));

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logger.info("{} Initializing expenses view...", PREFIX);

        setupTable();
        setupPaymentToggles();
        loadExpenseTypes();
        displayExpenses();
        updateStats();

        saveButton.setOnAction(event -> saveExpense());

        // Double-click to delete
        expensesTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && !expensesTable.getSelectionModel().isEmpty()) {
                Expense selected = expensesTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    confirmAndDelete(selected);
                }
            }
        });

        NumberFormatterUtil.applyToTextField(amountField);

        logger.info("{} View initialized successfully.", PREFIX);
    }

    private void setupTable() {
        expensesTable.getColumns().forEach(c -> c.setReorderable(false));
        expensesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colType.setCellValueFactory(cd ->
                new SimpleStringProperty(translateExpenseType(cd.getValue().getType())));
        colAmount.setCellValueFactory(cd ->
                new SimpleStringProperty(NumberFormatterUtil.format(cd.getValue().getAmount()) + " Gs"));
        colPaymentMethod.setCellValueFactory(cd -> {
            PaymentMethod p = paymentMethodsService.getPaymentMethodById(cd.getValue().getPaymentMethodId());
            String name = (p != null && p.getName() != null)
                    ? translatePaymentMethod(p.getName())
                    : "Desconocido";
            return new SimpleStringProperty(name);
        });
        colDescription.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getDescription()));
    }

    private void setupPaymentToggles() {
        cashToggle.setToggleGroup(paymentGroup);
        transferToggle.setToggleGroup(paymentGroup);

        // Seleccionar efectivo por defecto
        paymentGroup.selectToggle(cashToggle);

        logger.info("{} Payment toggles configured (cash + transfer only).", PREFIX);
    }

    private void loadExpenseTypes() {
        List<String> expenseTypes = List.of(
                "supply", "service", "purchase", "tax", "other"
        );

        expenseTypeBox.setItems(FXCollections.observableArrayList(expenseTypes));

        // Converter para mostrar traducido pero guardar en inglés
        expenseTypeBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(String type) {
                return type != null ? translateExpenseType(type) : "";
            }

            @Override
            public String fromString(String s) {
                return s != null ? s.toLowerCase() : "";
            }
        });

        // Set default value
        expenseTypeBox.setValue(expenseTypes.get(0));

        logger.info("{} {} expense types loaded.", PREFIX, expenseTypes.size());
    }

    private void displayExpenses() {
        logger.info("{} Loading expenses list...", PREFIX);
        List<Expense> expenses = expenseService.findAll();
        Collections.reverse(expenses);
        expensesTable.setItems(FXCollections.observableArrayList(expenses));
        logger.info("{} {} expenses loaded in table.", PREFIX, expenses.size());
    }

    private void updateStats() {
        try {
            double todayTotal = expenseService.getTodayTotal();
            double weekTotal = expenseService.getWeekTotal();
            double monthTotal = expenseService.getMonthTotal();

            todayTotalLabel.setText(NumberFormatterUtil.format(todayTotal) + " Gs");
            weekTotalLabel.setText(NumberFormatterUtil.format(weekTotal) + " Gs");
            monthTotalLabel.setText(NumberFormatterUtil.format(monthTotal) + " Gs");

            logger.debug("{} Stats updated -> Today: {}, Week: {}, Month: {}",
                    PREFIX, todayTotal, weekTotal, monthTotal);
        } catch (Exception e) {
            logger.error("{} Error loading expense statistics", e);
            // Mantener los valores en 0 si hay error
            todayTotalLabel.setText("0 Gs");
            weekTotalLabel.setText("0 Gs");
            monthTotalLabel.setText("0 Gs");
        }
    }

    private int getSelectedPaymentMethod() {
        Toggle selected = paymentGroup.getSelectedToggle();

        if (selected == null) {
            showAlert("Selecciona un método de pago");
            return -1;
        }

        if (selected == cashToggle) return 0;       // cash
        if (selected == transferToggle) return 1;   // transfer (banco)

        return -1;
    }

    private void saveExpense() {
        String type = expenseTypeBox.getValue();
        String desc = descriptionField.getText().trim();
        String amountStr = amountField.getText().replace(".", "").trim();
        int paymentMethodId = getSelectedPaymentMethod();

        if (type == null || type.isEmpty()) {
            showAlert("Debes seleccionar un tipo de egreso.");
            logger.warn("{} Validation failed: type not selected.", PREFIX);
            return;
        }
        if (desc.isEmpty()) {
            showAlert("El campo 'Descripción' no puede estar vacío.");
            logger.warn("{} Validation failed: description empty.", PREFIX);
            return;
        }
        if (amountStr.isEmpty()) {
            showAlert("El campo 'Monto' no puede estar vacío.");
            logger.warn("{} Validation failed: amount empty.", PREFIX);
            return;
        }
        if (paymentMethodId == -1) {
            showAlert("Debes seleccionar un método de pago.");
            logger.warn("{} Validation failed: payment method not selected.", PREFIX);
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);

            expenseService.registerExpense(
                    type,
                    amount,
                    desc,
                    paymentMethodId,
                    SessionManager.getActiveUser().getId()
            );

            logger.info("{} Expense added -> Type: {}, Amount: {}, Payment ID: {}, Desc: {}",
                    PREFIX, type, amount, paymentMethodId, desc);

            displayExpenses();
            updateStats(); // Actualizar stats después de guardar
            clearFields();

        } catch (NumberFormatException e) {
            showAlert("El campo 'Monto' debe contener un número válido.");
            logger.error("{} Error parsing amount: {}", PREFIX, e.getMessage());
        }
    }

    private void confirmAndDelete(Expense expense) {
        User activeUser = SessionManager.getActiveUser();
        String role = activeUser != null ? activeUser.getRole() : "";

        boolean canDelete = false;

        if ("admin".equalsIgnoreCase(role)) {
            canDelete = true;
        } else if ("user".equalsIgnoreCase(role)) {
            LocalDate expenseDate = expense.getDate();
            LocalDate today = LocalDate.now();
            canDelete = expenseDate.isEqual(today);
        }

        if (!canDelete) {
            showAlert("No tienes permiso para eliminar este egreso.");
            logger.warn("{} User '{}' tried to delete expense ID {} but lacks permission.",
                    PREFIX, activeUser.getName(), expense.getId());
            return;
        }

        // First confirmation
        Alert firstAlert = new Alert(Alert.AlertType.CONFIRMATION);
        firstAlert.setTitle("Confirmar eliminación");
        firstAlert.setHeaderText("¿Quieres eliminar este egreso?");
        firstAlert.setContentText(
                "ID: " + expense.getId() +
                        "\nTipo: " + translateExpenseType(expense.getType()) +
                        "\nDescripción: " + (expense.getDescription() != null ? expense.getDescription() : "") +
                        "\nMonto: " + NumberFormatterUtil.format(expense.getAmount()) + " Gs" +
                        "\nFecha: " + expense.getDate()
        );

        firstAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Second confirmation
                Alert secondAlert = new Alert(Alert.AlertType.CONFIRMATION);
                secondAlert.setTitle("¿Estás seguro?");
                secondAlert.setHeaderText("Esta acción no se puede deshacer.");
                secondAlert.setContentText("¿Realmente quieres eliminar este egreso?");
                secondAlert.showAndWait().ifPresent(secondResponse -> {
                    if (secondResponse == ButtonType.OK) {
                        expenseService.deleteExpense(expense.getId());
                        displayExpenses();
                        updateStats(); // Actualizar stats después de eliminar
                        logger.info("{} Expense deleted -> ID: {}, Type: {}, Amount: {}, Date: {}",
                                PREFIX, expense.getId(), expense.getType(), expense.getAmount(), expense.getDate());
                    } else {
                        logger.info("{} Deletion cancelled at final confirmation -> Expense ID: {}",
                                PREFIX, expense.getId());
                    }
                });
            } else {
                logger.info("{} Deletion cancelled -> Expense ID: {}", PREFIX, expense.getId());
            }
        });
    }

    private void clearFields() {
        descriptionField.clear();
        amountField.clear();
        expenseTypeBox.getSelectionModel().selectFirst();
        paymentGroup.selectToggle(cashToggle); // Reset a efectivo
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validación");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Traducción de payment methods (hardcoded, después con i18n)
    private String translatePaymentMethod(String key) {
        return switch (key) {
            case "cash" -> "Efectivo";
            case "transfer" -> "Banco";
            case "card" -> "Tarjeta";
            case "qr" -> "QR";
            default -> "Desconocido";
        };
    }

    // Traducción de expense types (hardcoded, después con i18n)
    private String translateExpenseType(String key) {
        return switch (key) {
            case "supply" -> "Insumos";
            case "service" -> "Servicio";
            case "purchase" -> "Compra";
            case "tax" -> "Impuesto";
            case "other" -> "Otro";
            default -> TextFormatterUtil.capitalizeFirstLetter(key);
        };
    }
}