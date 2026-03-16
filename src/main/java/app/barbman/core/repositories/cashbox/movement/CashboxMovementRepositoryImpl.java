package app.barbman.core.repositories.cashbox.movement;

import app.barbman.core.infrastructure.HibernateUtil;
import app.barbman.core.model.cashbox.CashboxMovement;
import app.barbman.core.repositories.AbstractHibernateRepository;
import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.List;

public class CashboxMovementRepositoryImpl extends AbstractHibernateRepository<CashboxMovement, Integer>
        implements CashboxMovementRepository {

    public CashboxMovementRepositoryImpl() {
        super(CashboxMovement.class);
    }

    @Override
    public void update(CashboxMovement movement) {
        throw new UnsupportedOperationException("Cashbox movements must not be updated.");
    }

    @Override
    public List<CashboxMovement> findByDateRange(LocalDateTime start, LocalDateTime end) {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            return em.createQuery(
                    "FROM CashboxMovement WHERE occurredAt BETWEEN :start AND :end ORDER BY occurredAt",
                    CashboxMovement.class)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getResultList();
        } catch (Exception e) {
            logger.warn("[CashboxMovementRepositoryImpl] Error fetching movements by date range: {}",
                    e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<CashboxMovement> findByReference(String referenceType, Integer referenceId) {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            return em.createQuery(
                    "FROM CashboxMovement WHERE referenceType = :type AND referenceId = :refId",
                    CashboxMovement.class)
                    .setParameter("type", referenceType)
                    .setParameter("refId", referenceId)
                    .getResultList();
        } catch (Exception e) {
            logger.warn("[CashboxMovementRepositoryImpl] Error fetching movements by reference: {}",
                    e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<CashboxMovement> findByOpeningId(Integer openingId) {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            return em.createQuery(
                    "FROM CashboxMovement WHERE openingId = :openingId ORDER BY occurredAt",
                    CashboxMovement.class)
                    .setParameter("openingId", openingId)
                    .getResultList();
        } catch (Exception e) {
            logger.warn("[CashboxMovementRepositoryImpl] Error fetching movements by opening {}: {}",
                    openingId, e.getMessage());
            return List.of();
        }
    }

    @Override
    public double sumByOpeningIdAndDirection(Integer openingId, String direction, boolean isCash) {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            String paymentFilter = isCash
                    ? "AND m.paymentMethodId = 0"
                    : "AND m.paymentMethodId IN (1, 2, 3)";

            Double result = em.createQuery(
                    "SELECT COALESCE(SUM(m.amount), 0) FROM CashboxMovement m " +
                            "WHERE m.openingId = :openingId " +
                            "AND m.direction = :direction " +
                            "AND m.movementType <> 'OPENING' " +
                            paymentFilter,
                    Double.class)
                    .setParameter("openingId", openingId)
                    .setParameter("direction", direction)
                    .getSingleResult();
            return result != null ? result : 0.0;
        } catch (Exception e) {
            logger.warn("[CashboxMovementRepositoryImpl] Error summing movements for opening {}: {}",
                    openingId, e.getMessage());
            return 0.0;
        }
    }
}
