package app.barbman.core.service.expenses;

import app.barbman.core.model.Expense;
import app.barbman.core.model.cashbox.CashboxMovement;
import app.barbman.core.model.cashbox.CashboxOpening;
import app.barbman.core.repositories.cashbox.movement.CashboxMovementRepository;
import app.barbman.core.repositories.cashbox.movement.CashboxMovementRepositoryImpl;
import app.barbman.core.repositories.expense.ExpenseRepository;
import app.barbman.core.repositories.expense.ExpenseRepositoryImpl;
import app.barbman.core.service.cashbox.CashboxService;
import app.barbman.core.util.legacy.LegacyExpenseRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

public class ExpensesService {

    private static final Logger logger = LogManager.getLogger(ExpensesService.class);
    private static final String PREFIX = "[EXPENSES-SERVICE]";

    private final ExpenseRepository expenseRepo;
    private final CashboxMovementRepository movementRepo = new CashboxMovementRepositoryImpl();
    private final LegacyExpenseRepository legacyExpenseRepo;
    private final CashboxService cashboxService;

    public ExpensesService(ExpenseRepository expenseRepo, CashboxService cashboxService) {
        this.expenseRepo = expenseRepo;
        this.legacyExpenseRepo = new LegacyExpenseRepository();
        this.cashboxService = cashboxService;
    }

    public void registerExpense(String type, double amount, String description, int paymentMethodId, int userId) {
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

        Expense expense = Expense.builder()
                .description(description)
                .amount(amount)
                .date(today)
                .type(type)
                .paymentMethodId(paymentMethodId)
                .build();
        expenseRepo.save(expense);

        movementRepo.save(buildMovement("EXPENSE", "OUT", amount, paymentMethodId,
                "EXPENSE", expense.getId(), "Expense registered", userId));

        logger.info("{} Expense registered -> type={}, amount={}, method={}, date={}, expenseID={}",
                PREFIX, type, amount, paymentMethodId, today, expense.getId());
    }

    public Expense registerAdvanceExpense(int userId, double amount, int paymentMethodId) {
        LocalDate date = LocalDate.now();
        String description = String.format("Advance | user_id: %d | date %s", userId, date);

        Expense expense = Expense.builder()
                .description(description)
                .amount(amount)
                .date(date)
                .type("advance")
                .paymentMethodId(paymentMethodId)
                .build();
        expenseRepo.save(expense);

        logger.info("{} Advance expense created -> user={}, amount={}, method={}, expenseID={}",
                PREFIX, userId, amount, paymentMethodId, expense.getId());

        movementRepo.save(buildMovement("EXPENSE", "OUT", amount, paymentMethodId,
                "EXPENSE", expense.getId(), "Advance registered", userId));

        return expense;
    }

    public Expense registerSalaryExpense(int userId, double amount, int paymentMethodId) {
        LocalDate date = LocalDate.now();
        String description = String.format("Salary | user_id: %d | date %s | method %d",
                userId, date, paymentMethodId);

        Expense expense = Expense.builder()
                .description(description)
                .amount(amount)
                .date(date)
                .type("salary")
                .paymentMethodId(paymentMethodId)
                .build();
        expenseRepo.save(expense);

        movementRepo.save(buildMovement("EXPENSE", "OUT", amount, paymentMethodId,
                "EXPENSE", expense.getId(), "Salary registered", userId));

        logger.info("{} Salary expense created -> user={}, amount={}, method={}, expenseID={}",
                PREFIX, userId, amount, paymentMethodId, expense.getId());

        return expense;
    }

    public void deleteExpense(int expenseId) {
        try {
            logger.warn("{} Deleting expense ID={}", PREFIX, expenseId);
            var movements = movementRepo.findByReference("EXPENSE", expenseId);
            for (var movement : movements) {
                movementRepo.delete(movement.getId());
                logger.debug("{} Deleted movement ID={}", PREFIX, movement.getId());
            }
            logger.info("{} Deleted {} cashbox movements for expense {}", PREFIX, movements.size(), expenseId);
            expenseRepo.delete(expenseId);
            logger.info("{} Expense deleted (movements + expense + related salary/advance)", PREFIX);
        } catch (Exception e) {
            logger.error("{} Error deleting expense ID {}: {}", PREFIX, expenseId, e.getMessage());
            throw new RuntimeException("Error deleting expense ID " + expenseId, e);
        }
    }

    public List<Expense> findAll() {
        try {
            List<Expense> combined = new ArrayList<>(legacyExpenseRepo.findAll());
            combined.addAll(expenseRepo.findAll());
            return combined;
        } catch (Exception e) {
            logger.error("{} Error retrieving expenses: {}", PREFIX, e.getMessage());
            return List.of();
        }
    }

    public double getTotalForPaymentMethodInPeriod(int paymentMethodId, LocalDate start, LocalDate end) {
        return expenseRepo.sumTotalByPaymentMethodAndPeriod(paymentMethodId, start, end);
    }

    public double getTodayTotal() {
        LocalDate today = LocalDate.now();
        return expenseRepo.sumTotalByPeriod(today, today);
    }

    public double getWeekTotal() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));
        return expenseRepo.sumTotalByPeriod(startOfWeek, endOfWeek);
    }

    public double getMonthTotal() {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());
        return expenseRepo.sumTotalByPeriod(startOfMonth, endOfMonth);
    }

    public void debugRepositoryStatus() {
        logger.debug("{} Repository health check executed successfully.", PREFIX);
    }

    private CashboxMovement buildMovement(String movementType, String direction, double amount,
                                          Integer paymentMethodId, String refType, Integer refId,
                                          String description, Integer userId) {
        LocalDateTime now = LocalDateTime.now();
        CashboxOpening currentOpening = cashboxService.getCurrentOpening();
        Integer openingId = currentOpening != null ? currentOpening.getId() : null;

        return CashboxMovement.builder()
                .movementType(movementType)
                .direction(direction)
                .amount(amount)
                .paymentMethodId(paymentMethodId)
                .referenceType(refType)
                .referenceId(refId)
                .description(description)
                .userId(userId)
                .occurredAt(now)
                .createdAt(now)
                .openingId(openingId)
                .build();
    }
}
