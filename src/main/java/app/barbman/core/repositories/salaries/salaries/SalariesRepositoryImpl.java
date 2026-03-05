package app.barbman.core.repositories.salaries.salaries;

import app.barbman.core.infrastructure.HibernateUtil;
import app.barbman.core.model.salaries.Salary;
import app.barbman.core.repositories.AbstractHibernateRepository;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;

public class SalariesRepositoryImpl extends AbstractHibernateRepository<Salary, Integer>
        implements SalariesRepository {

    public SalariesRepositoryImpl() {
        super(Salary.class);
    }

    @Override
    public Salary findByUserAndDateWithinPeriod(int userId, LocalDate date) {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            return em.createQuery(
                    "FROM Salary WHERE userId = :userId AND startDate <= :date AND endDate >= :date",
                    Salary.class)
                    .setParameter("userId", userId)
                    .setParameter("date", date)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            logger.warn("[SalariesRepositoryImpl] Error finding salary for user {} on {}: {}",
                    userId, date, e.getMessage());
            return null;
        }
    }
}
