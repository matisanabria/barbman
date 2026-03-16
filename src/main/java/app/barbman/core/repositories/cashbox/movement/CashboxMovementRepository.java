package app.barbman.core.repositories.cashbox.movement;

import app.barbman.core.model.cashbox.CashboxMovement;

import java.time.LocalDateTime;
import java.util.List;

public interface CashboxMovementRepository {

    List<CashboxMovement> findByDateRange(
            LocalDateTime start,
            LocalDateTime end
    );

    List<CashboxMovement> findByReference(
            String referenceType,
            Integer referenceId
    );

    List<CashboxMovement> findByOpeningId(Integer openingId);

    double sumByOpeningIdAndDirection(Integer openingId, String direction, boolean isCash);

    void save(CashboxMovement movement);

    void delete(Integer id);

    List<CashboxMovement> findAll();
}
