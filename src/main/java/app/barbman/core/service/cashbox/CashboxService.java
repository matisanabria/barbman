package app.barbman.core.service.cashbox;

import app.barbman.core.model.cashbox.*;
import app.barbman.core.repositories.cashbox.closure.CashboxClosureRepository;
import app.barbman.core.repositories.cashbox.movement.CashboxMovementRepository;
import app.barbman.core.repositories.cashbox.opening.CashboxOpeningRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CashboxService {

    private static final Logger logger = LogManager.getLogger(CashboxService.class);
    private static final String PREFIX = "[CASHBOX-SERVICE]";

    private final CashboxOpeningRepository openingRepo;
    private final CashboxClosureRepository closureRepo;
    private final CashboxMovementRepository movementRepo;

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
    // QUERY
    // ============================================================

    public boolean isCashboxOpen() {
        return openingRepo.hasOpenCashbox();
    }

    public CashboxOpening getCurrentOpening() {
        return openingRepo.findCurrentOpen();
    }

    // ============================================================
    // GUARDS
    // ============================================================

    public void assertCashboxOpened() {
        if (!isCashboxOpen()) {
            throw new IllegalStateException(
                    "Cashbox is not opened for the current period."
            );
        }
    }

    // ============================================================
    // OPENING
    // ============================================================

    public void openCashbox(
            double cashAmount,
            double bankAmount,
            Integer adminUserId,
            String notes
    ) {
        if (isCashboxOpen()) {
            throw new IllegalStateException("Cashbox is already open.");
        }

        LocalDateTime now = LocalDateTime.now();

        CashboxOpening opening = CashboxOpening.builder()
                .periodStartDate(now.toLocalDate())
                .openedAt(now)
                .openedByUserId(adminUserId)
                .cashAmount(cashAmount)
                .bankAmount(bankAmount)
                .notes(notes)
                .closed(false)
                .build();

        openingRepo.save(opening);

        // Log opening movements with correct payment method IDs
        if (cashAmount > 0) {
            movementRepo.save(buildMovement("OPENING", "IN", cashAmount, 0,
                    "CASHBOX_OPENING", opening.getId(), "Initial cash opening", adminUserId, opening.getId()));
        }

        if (bankAmount > 0) {
            movementRepo.save(buildMovement("OPENING", "IN", bankAmount, 1,
                    "CASHBOX_OPENING", opening.getId(), "Initial bank opening", adminUserId, opening.getId()));
        }

        logger.info("{} Cashbox opened (id={}, cash={}, bank={})",
                PREFIX, opening.getId(), cashAmount, bankAmount);
    }

    // ============================================================
    // CLOSURE
    // ============================================================

    public CashboxClosure closeCashbox(
            double actualCash,
            double actualBank,
            Integer adminUserId,
            String notes
    ) {
        CashboxOpening opening = getCurrentOpening();
        if (opening == null) {
            throw new IllegalStateException("Cannot close cashbox: no open cashbox found.");
        }

        double expectedCash = getExpectedCash(opening.getId());
        double expectedBank = getExpectedBank(opening.getId());

        double cashDiscrepancy = actualCash - expectedCash;
        double bankDiscrepancy = actualBank - expectedBank;

        CashboxClosure closure = CashboxClosure.builder()
                .openingId(opening.getId())
                .closedAt(LocalDateTime.now())
                .closedByUserId(adminUserId)
                .expectedCash(expectedCash)
                .expectedBank(expectedBank)
                .actualCash(actualCash)
                .actualBank(actualBank)
                .cashDiscrepancy(cashDiscrepancy)
                .bankDiscrepancy(bankDiscrepancy)
                .notes(notes)
                .build();

        closureRepo.save(closure);

        // Mark opening as closed
        opening.setClosed(true);
        openingRepo.update(opening);

        logger.info("{} Cashbox closed (openingId={}, expectedCash={}, expectedBank={}, actualCash={}, actualBank={})",
                PREFIX, opening.getId(), expectedCash, expectedBank, actualCash, actualBank);

        return closure;
    }

    public CashboxClosure getLastClosure() {
        return closureRepo.findLast();
    }

    // ============================================================
    // BALANCE CALCULATIONS (movement-based ledger)
    // ============================================================

    public double getExpectedCash(Integer openingId) {
        CashboxOpening opening = openingRepo.findById(openingId);
        if (opening == null) return 0;

        double cashIn = movementRepo.sumByOpeningIdAndDirection(openingId, "IN", true);
        double cashOut = movementRepo.sumByOpeningIdAndDirection(openingId, "OUT", true);

        return opening.getCashAmount() + cashIn - cashOut;
    }

    public double getExpectedBank(Integer openingId) {
        CashboxOpening opening = openingRepo.findById(openingId);
        if (opening == null) return 0;

        double bankIn = movementRepo.sumByOpeningIdAndDirection(openingId, "IN", false);
        double bankOut = movementRepo.sumByOpeningIdAndDirection(openingId, "OUT", false);

        return opening.getBankAmount() + bankIn - bankOut;
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private CashboxMovement buildMovement(String movementType, String direction, double amount,
                                          Integer paymentMethodId, String refType, Integer refId,
                                          String description, Integer userId, Integer openingId) {
        LocalDateTime now = LocalDateTime.now();
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
