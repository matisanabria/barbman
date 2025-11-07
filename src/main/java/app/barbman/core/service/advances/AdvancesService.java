package app.barbman.core.service.advances;

import app.barbman.core.model.salaries.Advance;
import app.barbman.core.model.Expense;
import app.barbman.core.repositories.advance.AdvanceRepository;
import app.barbman.core.repositories.advance.AdvanceRepositoryImpl;
import app.barbman.core.repositories.expense.ExpenseRepository;
import app.barbman.core.repositories.expense.ExpenseRepositoryImpl;
import app.barbman.core.service.expenses.ExpensesService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.util.List;

/**
 * Handles creation, retrieval and total calculation of employee advances.
 * When a new advance is registered, it also creates a matching Expense record
 * for bookkeeping purposes (type = 'advance').
 */
public class AdvancesService {

    private static final Logger logger = LogManager.getLogger(AdvancesService.class);
    private static final String PREFIX = "[ADV-SERVICE]";

    private final AdvanceRepository advanceRepo = new AdvanceRepositoryImpl();
    private final ExpenseRepository expenseRepository = new ExpenseRepositoryImpl();
    private final ExpensesService expenseService = new ExpensesService(expenseRepository);

    /**
     * Registers a new advance for a specific user and automatically creates
     * an Expense record linked to it.
     *
     * @param userId ID of the barber receiving the advance
     * @param amount Amount of the advance
     * @param paymentMethodId Payment method used (cash, transfer, etc.)
     */
    public void saveAdvance(int userId, double amount, int paymentMethodId) {
        LocalDate date = LocalDate.now();
        String description = String.format("Adelanto | user_id: %d | fecha %s", userId, date);

        // Register the expense
        Expense expense = expenseService.registerAdvanceExpense(userId, amount, paymentMethodId);

        // Link advance to the expense
        Advance advance = new Advance(userId, amount, date, paymentMethodId, expense.getId());
        advanceRepo.save(advance);

        logger.info("{} Advance registered -> user={}, amount={}, method={}, expenseID={}",
                PREFIX, userId, amount, paymentMethodId, expense.getId());
    }

    /**
     * Returns all advances in the system.
     */
    public List<Advance> getAll() {
        return advanceRepo.findAll();
    }

    /**
     * Returns all advances for a given user within a date range.
     */
    public List<Advance> getByUserAndRange(int userId, LocalDate from, LocalDate to) {
        return advanceRepo.findByUserAndDateRange(userId, from, to);
    }

    /**
     * Calculates total advances for a user in a given period.
     */
    public double getTotalByUserAndRange(int userId, LocalDate from, LocalDate to) {
        return advanceRepo.getTotalByUserAndDateRange(userId, from, to);
    }
}
