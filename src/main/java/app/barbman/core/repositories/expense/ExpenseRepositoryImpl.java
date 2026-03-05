package app.barbman.core.repositories.expense;

import app.barbman.core.infrastructure.HibernateUtil;
import app.barbman.core.model.Expense;
import app.barbman.core.repositories.AbstractHibernateRepository;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.util.List;

public class ExpenseRepositoryImpl extends AbstractHibernateRepository<Expense, Integer>
        implements ExpenseRepository {

    public ExpenseRepositoryImpl() {
        super(Expense.class);
    }

    @Override
    public List<Expense> searchByDateRange(LocalDate startDate, LocalDate endDate) {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            return em.createQuery(
                    "FROM Expense WHERE date BETWEEN :start AND :end ORDER BY date, id",
                    Expense.class)
                    .setParameter("start", startDate)
                    .setParameter("end", endDate)
                    .getResultList();
        } catch (Exception e) {
            logger.warn("[ExpenseRepositoryImpl] Error searching expenses by date range: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public double sumTotalByPaymentMethodAndPeriod(int paymentMethodId, LocalDate start, LocalDate end) {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            Double result = em.createQuery(
                    "SELECT SUM(e.amount) FROM Expense e WHERE e.paymentMethodId = :pm AND e.date BETWEEN :start AND :end",
                    Double.class)
                    .setParameter("pm", paymentMethodId)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getSingleResult();
            return result != null ? result : 0.0;
        } catch (Exception e) {
            logger.error("[ExpenseRepositoryImpl] Error summing expenses by payment method: {}", e.getMessage());
            return 0.0;
        }
    }

    @Override
    public double sumTotalByPeriod(LocalDate start, LocalDate end) {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            Double result = em.createQuery(
                    "SELECT SUM(e.amount) FROM Expense e WHERE e.date BETWEEN :start AND :end",
                    Double.class)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getSingleResult();
            return result != null ? result : 0.0;
        } catch (Exception e) {
            logger.error("[ExpenseRepositoryImpl] Error summing expenses by period: {}", e.getMessage());
            return 0.0;
        }
    }
}
