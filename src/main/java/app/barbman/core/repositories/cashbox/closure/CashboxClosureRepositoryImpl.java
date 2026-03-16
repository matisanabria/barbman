package app.barbman.core.repositories.cashbox.closure;

import app.barbman.core.infrastructure.HibernateUtil;
import app.barbman.core.model.cashbox.CashboxClosure;
import app.barbman.core.repositories.AbstractHibernateRepository;
import jakarta.persistence.EntityManager;

public class CashboxClosureRepositoryImpl extends AbstractHibernateRepository<CashboxClosure, Integer>
        implements CashboxClosureRepository {

    public CashboxClosureRepositoryImpl() {
        super(CashboxClosure.class);
    }

    @Override
    public CashboxClosure findByOpeningId(Integer openingId) {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            return em.createQuery(
                    "FROM CashboxClosure WHERE openingId = :openingId", CashboxClosure.class)
                    .setParameter("openingId", openingId)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            logger.warn("[CashboxClosureRepositoryImpl] Error fetching closure for opening {}: {}",
                    openingId, e.getMessage());
            return null;
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
