package app.barbman.core.repositories.cashbox.closure;

import app.barbman.core.model.cashbox.CashboxClosure;

public interface CashboxClosureRepository {

    CashboxClosure findByOpeningId(Integer openingId);

    CashboxClosure findLast();

    void save(CashboxClosure closure);
}
