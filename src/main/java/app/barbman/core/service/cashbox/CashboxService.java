package app.barbman.core.service.cashbox;

import app.barbman.core.model.cashbox.*;
import app.barbman.core.repositories.cashbox.closure.CashboxClosureRepository;
import app.barbman.core.repositories.cashbox.movement.CashboxMovementRepository;
import app.barbman.core.repositories.cashbox.opening.CashboxOpeningRepository;
import app.barbman.core.repositories.expense.ExpenseRepository;
import app.barbman.core.repositories.expense.ExpenseRepositoryImpl;
import app.barbman.core.repositories.sales.SaleRepository;
import app.barbman.core.repositories.sales.SaleRepositoryImpl;
import app.barbman.core.service.expenses.ExpensesService;
import app.barbman.core.service.sales.SalesService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.*;
import java.util.List;

public class CashboxService {

    private static final Logger logger = LogManager.getLogger(CashboxService.class);
    private static final String PREFIX = "[CASHBOX-SERVICE]";

    private final CashboxOpeningRepository openingRepo;
    private final CashboxClosureRepository closureRepo;
    private final CashboxMovementRepository movementRepo;

    private final SaleRepository saleRepository = new SaleRepositoryImpl();
    private final SalesService salesService = new SalesService(saleRepository);
    private final ExpenseRepository expenseRepo = new ExpenseRepositoryImpl();

    public CashboxService(
            CashboxOpeningRepository openingRepo,
            CashboxClosureRepository closureRepo,
            CashboxMovementRepository movementRepo

    ) {
        this.openingRepo = openingRepo;
        this.closureRepo = closureRepo;
        this.movementRepo = movementRepo;
    }



    // ============================================================
    // PERIOD HELPERS
    // ============================================================

    public LocalDate getCurrentPeriodStart() {
        LocalDate today = LocalDate.now();
        return today.with(DayOfWeek.MONDAY);
    }

    public LocalDate getCurrentPeriodEnd() {
        return getCurrentPeriodStart().plusDays(6);
    }

    // ============================================================
    // OPENING
    // ============================================================

    public boolean isCurrentPeriodOpened() {
        return openingRepo.existsForPeriod(getCurrentPeriodStart());
    }

    public void openCashbox(
            double cashAmount,
            double bankAmount,
            Integer adminUserId,
            String notes
    ) {
        LocalDate periodStart = getCurrentPeriodStart();

        if (openingRepo.existsForPeriod(periodStart)) {
            throw new IllegalStateException("Cashbox is already opened for current period.");
        }

        CashboxOpening opening = new CashboxOpening(
                periodStart,
                LocalDateTime.now(),
                adminUserId,
                cashAmount,
                bankAmount,
                notes
        );

        openingRepo.save(opening);

        // Log movements
        if (cashAmount > 0) {
            movementRepo.save(new CashboxMovement(
                    "OPENING",
                    "IN",
                    cashAmount,
                    null,
                    "CASHBOX_OPENING",
                    opening.getId(),
                    "Initial cash opening",
                    adminUserId,
                    LocalDateTime.now()
            ));
        }

        if (bankAmount > 0) {
            movementRepo.save(new CashboxMovement(
                    "OPENING",
                    "IN",
                    bankAmount,
                    null,
                    "CASHBOX_OPENING",
                    opening.getId(),
                    "Initial bank opening",
                    adminUserId,
                    LocalDateTime.now()
            ));
        }

        logger.info("{} Cashbox opened for period {} (cash={}, bank={})",
                PREFIX, periodStart, cashAmount, bankAmount);
    }

    // ============================================================
    // GUARDS
    // ============================================================

    public void assertCashboxOpened() {
        if (!isCurrentPeriodOpened()) {
            throw new IllegalStateException(
                    "Cashbox is not opened for the current period."
            );
        }
    }

    // ============================================================
    // CLOSURE
    // ============================================================

    public CashboxClosure closeCurrentPeriod(
            Integer adminUserId,
            String notes
    ) {
        LocalDate periodStart = getCurrentPeriodStart();
        LocalDate periodEnd = getCurrentPeriodEnd();

        if (!openingRepo.existsForPeriod(periodStart)) {
            throw new IllegalStateException("Cannot close cashbox without opening.");
        }

        if (closureRepo.existsForPeriod(periodStart)) {
            throw new IllegalStateException("Cashbox already closed for this period.");
        }

        double expectedCash = calculateExpectedCash();
        double expectedBank = calculateExpectedBank();
        double expectedTotal = expectedCash + expectedBank;


        CashboxClosure closure = new CashboxClosure(
                periodStart,
                periodEnd,
                LocalDateTime.now(),
                adminUserId,
                expectedCash,
                expectedBank,
                expectedTotal,
                notes
        );

        closureRepo.save(closure);

        logger.info("{} Cashbox closed for period {} (expected={}, diff={})",
                PREFIX, periodStart, expectedTotal);

        return closure;
    }

    // ============================================================
    // CALCULATIONS
    // ============================================================

    private double calculateExpectedCash() {
        LocalDate start = getCurrentPeriodStart();
        LocalDate end = getCurrentPeriodEnd();

        double salesCash =
                salesService.getTotalForPaymentMethodInPeriod(0, start, end);

        double expensesCash = expenseRepo.sumTotalByPaymentMethodAndPeriod(
                        0, start, end
                );

        return salesCash - expensesCash;
    }

    private double calculateExpectedBank() {
        LocalDate start = getCurrentPeriodStart();
        LocalDate end = getCurrentPeriodEnd();

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
}
