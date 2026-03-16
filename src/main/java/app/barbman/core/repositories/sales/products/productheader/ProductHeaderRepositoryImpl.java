package app.barbman.core.repositories.sales.products.productheader;

import app.barbman.core.infrastructure.HibernateUtil;
import app.barbman.core.model.sales.products.ProductHeader;
import app.barbman.core.repositories.AbstractHibernateRepository;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;

public class ProductHeaderRepositoryImpl extends AbstractHibernateRepository<ProductHeader, Integer>
        implements ProductHeaderRepository {

    public ProductHeaderRepositoryImpl() {
        super(ProductHeader.class);
    }

    @Override
    public ProductHeader findBySaleId(int saleId) {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            return em.createQuery("FROM ProductHeader WHERE saleId = :saleId", ProductHeader.class)
                    .setParameter("saleId", saleId)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            logger.error("[ProductHeaderRepositoryImpl] Error fetching product header by saleId {}: {}",
                    saleId, e.getMessage());
            return null;
        }
    }

    @Override
    public double sumProductTotalsByUserAndDateRange(int userId, LocalDate from, LocalDate to) {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            Double result = em.createQuery(
                    "SELECT COALESCE(SUM(ph.subtotal), 0) FROM ProductHeader ph " +
                            "JOIN Sale s ON ph.saleId = s.id " +
                            "WHERE s.userId = :userId AND s.date BETWEEN :from AND :to",
                    Double.class)
                    .setParameter("userId", userId)
                    .setParameter("from", from)
                    .setParameter("to", to)
                    .getSingleResult();
            return result != null ? result : 0.0;
        } catch (Exception e) {
            logger.error("[ProductHeaderRepositoryImpl] Error summing product totals: {}", e.getMessage());
            return 0.0;
        }
    }
}
