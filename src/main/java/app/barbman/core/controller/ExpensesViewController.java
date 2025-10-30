package app.barbman.core.controller;

import app.barbman.core.model.*;
import app.barbman.core.repositories.expense.ExpenseRepository;
import app.barbman.core.repositories.expense.ExpenseRepositoryImpl;
import app.barbman.core.repositories.paymentmethod.PaymentMethodRepository;
import app.barbman.core.repositories.paymentmethod.PaymentMethodRepositoryImpl;
import app.barbman.core.service.egresos.EgresosService;
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

    @FXML private ChoiceBox<String> expenseTypeBox;
    @FXML private ChoiceBox<PaymentMethod> paymentMethodBox;
    @FXML private TextField amountField;
    @FXML private TextField descriptionField;
    @FXML private Button saveButton;


    private final ExpenseRepository expenseRepo = new ExpenseRepositoryImpl();
    private final PaymentMethodRepository paymentRepo = new PaymentMethodRepositoryImpl();
    private final EgresosService expenseService = new EgresosService(expenseRepo);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logger.info("{} Initializing expenses view...", PREFIX);

        setupTable();
        loadPaymentMethods();
        loadExpenseTypes();

        displayExpenses();

        expenseTypeBox.setItems(FXCollections.observableArrayList("supply", "service", "purchase", "tax", "other", "salary", "advance"));
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
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colAmount.setCellValueFactory(cd ->
                new SimpleStringProperty(NumberFormatterUtil.format(cd.getValue().getAmount()) + " Gs"));
        colPaymentMethod.setCellValueFactory(cd -> {
            PaymentMethod p = paymentRepo.findById(cd.getValue().getPaymentMethodId());
            String name = (p != null && p.getName() != null)
                    ? TextFormatterUtil.capitalizeFirstLetter(p.getName())
                    : "Unknown";
            return new SimpleStringProperty(name);
        });
        colDescription.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getDescription()));
    }

    private void loadPaymentMethods() {
        List<PaymentMethod> payments = paymentRepo.findAll();
        paymentMethodBox.setItems(FXCollections.observableArrayList(payments));
        paymentMethodBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(PaymentMethod p) {
                return p != null ? TextFormatterUtil.capitalizeFirstLetter(p.getName()) : "";
            }
            @Override
            public PaymentMethod fromString(String s) { return null; }
        });
        logger.info("{} {} payment methods loaded into ChoiceBox.", PREFIX, payments.size());
    }

    private void loadExpenseTypes() {
        List<String> expenseTypes = List.of(
                "supply", "service", "purchase", "tax", "other", "salary", "advance"
        );

        expenseTypeBox.setItems(FXCollections.observableArrayList(expenseTypes));

        // The converter capitalizes the first letter for display but keeps the original value internally
        expenseTypeBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(String type) {
                return type != null ? TextFormatterUtil.capitalizeFirstLetter(type) : "";
            }

            @Override
            public String fromString(String s) { // Not using this rn, but must be implemented
                return s != null ? s.toLowerCase() : "";
            }
        });

        // Set default value
        expenseTypeBox.setValue(expenseTypes.get(0));

        logger.info("{} {} expense types loaded into ChoiceBox.", PREFIX, expenseTypes.size());
    }

    private void displayExpenses() {
        logger.info("{} Loading expenses list...", PREFIX);
        List<Expense> expenses = expenseRepo.findAll();
        Collections.reverse(expenses);
        expensesTable.setItems(FXCollections.observableArrayList(expenses));
        logger.info("{} {} expenses loaded in table.", PREFIX, expenses.size());
    }

    private void saveExpense() {
        String type = expenseTypeBox.getValue();
        String desc = TextFormatterUtil.capitalizeFirstLetter(descriptionField.getText().trim());
        String amountStr = amountField.getText().replace(".", "").trim();
        PaymentMethod payment = paymentMethodBox.getValue();

        if (type == null || type.isEmpty()) {
            showAlert("You must select an expense type.");
            logger.warn("{} Validation failed: type not selected.", PREFIX);
            return;
        }
        if (desc.isEmpty()) {
            showAlert("The 'Description' field cannot be empty.");
            logger.warn("{} Validation failed: description empty.", PREFIX);
            return;
        }
        if (amountStr.isEmpty()) {
            showAlert("The 'Amount' field cannot be empty.");
            logger.warn("{} Validation failed: amount empty.", PREFIX);
            return;
        }
        if (payment == null) {
            showAlert("You must select a payment method.");
            logger.warn("{} Validation failed: payment method empty.", PREFIX);
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);

            expenseService.addEgreso(
                    type,
                    amount,
                    desc,
                    payment.getId()
            );

            logger.info("{} Expense added -> Type: {}, Amount: {}, Payment: {}, Desc: {}",
                    PREFIX, type, amount, payment.getName(), desc);

            displayExpenses();
            clearFields();

        } catch (NumberFormatException e) {
            showAlert("The 'Amount' field must contain a valid number.");
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
            // service.getDate() is already LocalDate
            LocalDate serviceDate = expense.getDate();
            LocalDate today = LocalDate.now();
            canDelete = serviceDate.isEqual(today);
        }

        if (!canDelete) {
            showAlert("You do not have permission to delete this expense.");
            logger.warn("{} User '{}' tried to delete expense ID {} but lacks permission.",
                    PREFIX, activeUser.getName(), expense.getId());
            return;
        }

        // First alert: show expense info
        Alert firstAlert = new Alert(Alert.AlertType.CONFIRMATION);
        firstAlert.setTitle("Confirm Deletion");
        firstAlert.setHeaderText("Do you want to delete this expense?");
        firstAlert.setContentText(
                "Expense ID: " + expense.getId() +
                        "\nType: " + TextFormatterUtil.capitalizeFirstLetter(expense.getType()) +
                        "\nDescription: " + (expense.getDescription() != null ? expense.getDescription() : "") +
                        "\nAmount: " + NumberFormatterUtil.format(expense.getAmount()) + " Gs" +
                        "\nDate: " + expense.getDate()
        );

        firstAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Second alert: final confirmation
                Alert secondAlert = new Alert(Alert.AlertType.CONFIRMATION);
                secondAlert.setTitle("Are you sure?");
                secondAlert.setHeaderText("This action cannot be undone.");
                secondAlert.setContentText("Do you really want to delete this expense?");
                secondAlert.showAndWait().ifPresent(secondResponse -> {
                    if (secondResponse == ButtonType.OK) {
                        expenseRepo.delete(expense.getId());
                        displayExpenses();
                        logger.info("{} Expense deleted -> ID: {}, Type: {}, Amount: {}, Date: {}",
                                PREFIX,
                                expense.getId(),
                                expense.getType(),
                                expense.getAmount(),
                                expense.getDate());
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
        if (!paymentMethodBox.getItems().isEmpty())
            paymentMethodBox.setValue(paymentMethodBox.getItems().get(0));
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
