package app.barbman.core.repositories.sales.services.serviceheader;

import app.barbman.core.infrastructure.HibernateUtil;
import app.barbman.core.model.sales.services.ServiceHeader;
import app.barbman.core.repositories.AbstractHibernateRepository;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;

public class ServiceHeaderRepositoryImpl extends AbstractHibernateRepository<ServiceHeader, Integer>
        implements ServiceHeaderRepository {

    public ServiceHeaderRepositoryImpl() {
        super(ServiceHeader.class);
    }

    @Override
    public double sumServiceTotalsByUserAndDateRange(int barberId, LocalDate from, LocalDate to) {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            Double result = em.createQuery(
                    "SELECT SUM(s.subtotal) FROM ServiceHeader s WHERE s.userId = :userId AND s.date BETWEEN :from AND :to",
                    Double.class)
                    .setParameter("userId", barberId)
                    .setParameter("from", from)
                    .setParameter("to", to)
                    .getSingleResult();
            return result != null ? result : 0.0;
        } catch (Exception e) {
            logger.error("[ServiceHeaderRepositoryImpl] Error summing service totals: {}", e.getMessage());
            return 0.0;
        }
    }

    @Override
    public ServiceHeader findBySaleId(int saleId) {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            return em.createQuery("FROM ServiceHeader WHERE saleId = :saleId", ServiceHeader.class)
                    .setParameter("saleId", saleId)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            logger.error("[ServiceHeaderRepositoryImpl] Error fetching service header by saleId {}: {}",
                    saleId, e.getMessage());
            return null;
        }
    }
}
