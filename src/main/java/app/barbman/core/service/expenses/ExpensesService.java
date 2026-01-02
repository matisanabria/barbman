package app.barbman.core.service.expenses;

import app.barbman.core.model.Expense;
import app.barbman.core.repositories.expense.ExpenseRepository;
import app.barbman.core.repositories.expense.ExpenseRepositoryImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.util.List;

/**
 * Handles creation, validation, and registration of expense records.
 * Provides specific methods for salary, advance, and generic expenses.
 */
public class ExpensesService {

    private static final Logger logger = LogManager.getLogger(ExpensesService.class);
    private static final String PREFIX = "[EXPENSES-SERVICE]";

    private final ExpenseRepository expenseRepo;

    public ExpensesService(ExpenseRepository expenseRepo) {
        this.expenseRepo = expenseRepo;
    }

    /**
     * Registers a general expense record.
     *
     * @param type Expense type (e.g., supply, purchase, tax, service, other)
     * @param amount Expense amount (must be greater than zero)
     * @param description Optional description (max length: 500)
     * @param paymentMethodId Payment method ID (cash, transfer, etc.)
     */
    public void registerExpense(String type, double amount, String description, int paymentMethodId) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Expense type must not be empty.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Expense amount must be greater than zero.");
        }
        if (description != null && description.length() > 500) {
            throw new IllegalArgumentException("Description must not exceed 500 characters.");
        }

        LocalDate today = LocalDate.now();

        Expense expense = new Expense(description, amount, today, type, paymentMethodId);
        expenseRepo.save(expense);

        logger.info("{} Expense registered -> type={}, amount={}, method={}, date={}, expenseID={}",
                PREFIX, type, amount, paymentMethodId, today, expense.getId());
    }

    /**
     * Registers an expense record for an advance payment.
     *
     * @param userId User receiving the advance
     * @param amount Advance amount
     * @param paymentMethodId Payment method (cash, transfer, etc.)
     * @return The Expense created and saved in the database
     */
    public Expense registerAdvanceExpense(int userId, double amount, int paymentMethodId) {
        LocalDate date = LocalDate.now();
        String description = String.format("Advance | user_id: %d | date %s", userId, date);

        Expense expense = new Expense(description, amount, date, "advance", paymentMethodId);
        expenseRepo.save(expense);

        logger.info("{} Advance expense created -> user={}, amount={}, method={}, expenseID={}",
                PREFIX, userId, amount, paymentMethodId, expense.getId());

        return expense;
    }

    /**
     * Registers an expense record for a salary payment.
     *
     * @param userId User receiving the salary
     * @param amount Salary amount
     * @param paymentMethodId Payment method (cash, transfer, etc.)
     * @return The Expense created and saved in the database
     */
    public Expense registerSalaryExpense(int userId, double amount, int paymentMethodId) {
        LocalDate date = LocalDate.now();
        String description = String.format(
                "Salary | user_id: %d | date %s | method %d",
                userId, date, paymentMethodId
        );

        Expense expense = new Expense(description, amount, date, "salary", paymentMethodId);
        expenseRepo.save(expense);

        logger.info("{} Salary expense created -> user={}, amount={}, method={}, expenseID={}",
                PREFIX, userId, amount, paymentMethodId, expense.getId());

        return expense;
    }

    /**
     * Logs internal repository-level errors or debugging info when needed.
     * Currently serves as a placeholder for future filtering methods.
     * TODO: Implement real database check to verify table existence and accessibility.
     */
    public void debugRepositoryStatus() {
        logger.debug("{} Repository health check executed successfully.", PREFIX);
    }

    /**
     * Fetch all expenses from the repository.
     */
    public List<Expense> findAll() {
        try {
            List<Expense> expenses = expenseRepo.findAll();
            logger.debug("{} Retrieved {} total expenses from repository.", PREFIX, expenses.size());
            return expenses;
        } catch (Exception e) {
            logger.error("{} Error retrieving expenses: {}", PREFIX, e.getMessage());
            return List.of();
        }
    }
    /**
     * Delete an expense by ID.
     */
    public void deleteExpense(int expenseId) {
        try {
            expenseRepo.delete(expenseId);
            logger.info("{} Expense deleted successfully -> ID={}", PREFIX, expenseId);
        } catch (Exception e) {
            logger.error("{} Error deleting expense ID {}: {}", PREFIX, expenseId, e.getMessage());
            throw new RuntimeException("Error deleting expense ID " + expenseId, e);
        }
    }
}
