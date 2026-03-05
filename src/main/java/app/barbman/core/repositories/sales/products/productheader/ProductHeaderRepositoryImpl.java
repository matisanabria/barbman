package app.barbman.core.repositories.sales.products.productheader;

import app.barbman.core.infrastructure.HibernateUtil;
import app.barbman.core.model.sales.products.ProductHeader;
import app.barbman.core.repositories.AbstractHibernateRepository;
import jakarta.persistence.EntityManager;

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
}
