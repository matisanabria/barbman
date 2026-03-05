package app.barbman.core.repositories.cashbox.closure;

import app.barbman.core.infrastructure.HibernateUtil;
import app.barbman.core.model.cashbox.CashboxClosure;
import app.barbman.core.repositories.AbstractHibernateRepository;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;

public class CashboxClosureRepositoryImpl extends AbstractHibernateRepository<CashboxClosure, Integer>
        implements CashboxClosureRepository {

    public CashboxClosureRepositoryImpl() {
        super(CashboxClosure.class);
    }

    @Override
    public CashboxClosure findByPeriodStart(LocalDate periodStartDate) {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            return em.createQuery(
                    "FROM CashboxClosure WHERE periodStartDate = :date", CashboxClosure.class)
                    .setParameter("date", periodStartDate)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            logger.warn("[CashboxClosureRepositoryImpl] Error fetching closure for period {}: {}",
                    periodStartDate, e.getMessage());
            return null;
        }
    }

    @Override
    public boolean existsForPeriod(LocalDate periodStartDate) {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            Long count = em.createQuery(
                    "SELECT COUNT(c) FROM CashboxClosure c WHERE c.periodStartDate = :date", Long.class)
                    .setParameter("date", periodStartDate)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            logger.warn("[CashboxClosureRepositoryImpl] Error checking closure existence for {}: {}",
                    periodStartDate, e.getMessage());
            return false;
        }
    }

    @Override
    public CashboxClosure findLast() {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            return em.createQuery(
                    "FROM CashboxClosure ORDER BY closedAt DESC", CashboxClosure.class)
                    .setMaxResults(1)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            logger.warn("[CashboxClosureRepositoryImpl] Error fetching last closure: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public void update(CashboxClosure closure) {
        throw new UnsupportedOperationException("Cashbox closures must not be updated.");
    }

    @Override
    public void delete(Integer id) {
        throw new UnsupportedOperationException("Cashbox closures must not be deleted.");
    }
}
