package app.barbman.core.repositories.cashbox.opening;

import app.barbman.core.infrastructure.HibernateUtil;
import app.barbman.core.model.cashbox.CashboxOpening;
import app.barbman.core.repositories.AbstractHibernateRepository;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;

public class CashboxOpeningRepositoryImpl extends AbstractHibernateRepository<CashboxOpening, Integer>
        implements CashboxOpeningRepository {

    public CashboxOpeningRepositoryImpl() {
        super(CashboxOpening.class);
    }

    @Override
    public CashboxOpening findByPeriodStart(LocalDate periodStartDate) {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            return em.createQuery(
                    "FROM CashboxOpening WHERE periodStartDate = :date", CashboxOpening.class)
                    .setParameter("date", periodStartDate)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            logger.warn("[CashboxOpeningRepositoryImpl] Error fetching opening for period {}: {}",
                    periodStartDate, e.getMessage());
            return null;
        }
    }

    @Override
    public boolean existsForPeriod(LocalDate periodStartDate) {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            Long count = em.createQuery(
                    "SELECT COUNT(o) FROM CashboxOpening o WHERE o.periodStartDate = :date", Long.class)
                    .setParameter("date", periodStartDate)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            logger.warn("[CashboxOpeningRepositoryImpl] Error checking opening existence for {}: {}",
                    periodStartDate, e.getMessage());
            return false;
        }
    }

    @Override
    public void update(CashboxOpening opening) {
        throw new UnsupportedOperationException("Cashbox openings must not be updated.");
    }

    @Override
    public void delete(Integer id) {
        throw new UnsupportedOperationException("Cashbox openings must not be deleted.");
    }
}
