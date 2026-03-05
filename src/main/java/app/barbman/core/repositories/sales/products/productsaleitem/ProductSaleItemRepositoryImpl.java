package app.barbman.core.repositories.sales.products.productsaleitem;

import app.barbman.core.infrastructure.HibernateUtil;
import app.barbman.core.model.sales.products.ProductSaleItem;
import app.barbman.core.repositories.AbstractHibernateRepository;
import jakarta.persistence.EntityManager;

import java.util.List;

public class ProductSaleItemRepositoryImpl extends AbstractHibernateRepository<ProductSaleItem, Integer>
        implements ProductSaleItemRepository {

    public ProductSaleItemRepositoryImpl() {
        super(ProductSaleItem.class);
    }

    @Override
    public List<ProductSaleItem> findBySaleId(int productHeaderId) {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            return em.createQuery(
                    "FROM ProductSaleItem WHERE productHeaderId = :headerId",
                    ProductSaleItem.class)
                    .setParameter("headerId", productHeaderId)
                    .getResultList();
        } catch (Exception e) {
            logger.error("[ProductSaleItemRepositoryImpl] Error fetching items for header {}: {}",
                    productHeaderId, e.getMessage());
            return List.of();
        }
    }

    @Override
    public void deleteBySaleId(int productHeaderId, EntityManager em) {
        em.createQuery("DELETE FROM ProductSaleItem WHERE productHeaderId = :headerId")
                .setParameter("headerId", productHeaderId)
                .executeUpdate();
    }
}
