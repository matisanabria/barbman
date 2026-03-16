package app.barbman.core.repositories.cashbox.opening;

import app.barbman.core.model.cashbox.CashboxOpening;

import java.time.LocalDate;

public interface CashboxOpeningRepository {

    CashboxOpening findById(Integer id);

    CashboxOpening findByPeriodStart(LocalDate periodStartDate);

    boolean existsForPeriod(LocalDate periodStartDate);

    CashboxOpening findCurrentOpen();

    boolean hasOpenCashbox();

    void save(CashboxOpening opening);

    void update(CashboxOpening opening);

    java.util.List<CashboxOpening> findAll();
}
