package app.barbman.core.repositories.cashbox.movement;

import app.barbman.core.model.cashbox.CashboxMovement;
import app.barbman.core.repositories.GenericRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CashboxMovementRepository
        extends GenericRepository<CashboxMovement, Integer> {

    List<CashboxMovement> findByDateRange(
            LocalDateTime start,
            LocalDateTime end
    );

    List<CashboxMovement> findByReference(
            String referenceType,
            Integer referenceId
    );
}
