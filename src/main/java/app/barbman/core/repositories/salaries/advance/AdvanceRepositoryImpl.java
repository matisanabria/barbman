package app.barbman.core.repositories.salaries.advance;

import app.barbman.core.infrastructure.HibernateUtil;
import app.barbman.core.model.salaries.Advance;
import app.barbman.core.repositories.AbstractHibernateRepository;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.util.List;

public class AdvanceRepositoryImpl extends AbstractHibernateRepository<Advance, Integer>
        implements AdvanceRepository {

    public AdvanceRepositoryImpl() {
        super(Advance.class);
    }

    @Override
    public List<Advance> findAll() {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            return em.createQuery("FROM Advance ORDER BY date DESC", Advance.class).getResultList();
        } catch (Exception e) {
            logger.error("[AdvanceRepositoryImpl] Error fetching all advances: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<Advance> findByUserAndDateRange(int userId, LocalDate from, LocalDate to) {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            return em.createQuery(
                    "FROM Advance WHERE userId = :userId AND date BETWEEN :from AND :to ORDER BY date",
                    Advance.class)
                    .setParameter("userId", userId)
                    .setParameter("from", from)
                    .setParameter("to", to)
                    .getResultList();
        } catch (Exception e) {
            logger.warn("[AdvanceRepositoryImpl] Error filtering advances: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public double getTotalByUserAndDateRange(int userId, LocalDate from, LocalDate to) {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            Double result = em.createQuery(
                    "SELECT SUM(a.amount) FROM Advance a WHERE a.userId = :userId AND a.date BETWEEN :from AND :to",
                    Double.class)
                    .setParameter("userId", userId)
                    .setParameter("from", from)
                    .setParameter("to", to)
                    .getSingleResult();
            return result != null ? result : 0.0;
        } catch (Exception e) {
            logger.warn("[AdvanceRepositoryImpl] Error calculating total advances: {}", e.getMessage());
            return 0.0;
        }
    }
}
