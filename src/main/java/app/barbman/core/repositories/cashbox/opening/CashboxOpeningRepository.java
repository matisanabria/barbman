package app.barbman.core.repositories.cashbox.opening;

import app.barbman.core.model.cashbox.CashboxOpening;
import app.barbman.core.repositories.GenericRepository;

import java.time.LocalDate;

public interface CashboxOpeningRepository
        extends GenericRepository<CashboxOpening, Integer> {

    CashboxOpening findByPeriodStart(LocalDate periodStartDate);

    boolean existsForPeriod(LocalDate periodStartDate);
}
