package app.barbman.core.repositories.cashbox.closure;

import app.barbman.core.model.cashbox.CashboxClosure;
import app.barbman.core.repositories.GenericRepository;

import java.time.LocalDate;

public interface CashboxClosureRepository
        extends GenericRepository<CashboxClosure, Integer> {

    CashboxClosure findByPeriodStart(LocalDate periodStartDate);

    boolean existsForPeriod(LocalDate periodStartDate);
    CashboxClosure findLast();
}
