package app.barbman.core.repositories.sales.products.product;

import app.barbman.core.infrastructure.HibernateUtil;
import app.barbman.core.model.sales.products.Product;
import app.barbman.core.repositories.AbstractHibernateRepository;
import jakarta.persistence.EntityManager;

import java.util.List;

public class ProductRepositoryImpl extends AbstractHibernateRepository<Product, Integer>
        implements ProductRepository {

    public ProductRepositoryImpl() {
        super(Product.class);
    }

    @Override
    public Product findByName(String displayName) {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            return em.createQuery("FROM Product WHERE name = :name", Product.class)
                    .setParameter("name", displayName)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            logger.error("[ProductRepositoryImpl] Error fetching product by name '{}': {}",
                    displayName, e.getMessage());
            return null;
        }
    }

    @Override
    public List<Product> findAllInStock() {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            return em.createQuery("FROM Product WHERE stock > 0", Product.class).getResultList();
        } catch (Exception e) {
            logger.error("[ProductRepositoryImpl] Error fetching in-stock products: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public void decreaseStock(int productId, int quantity, EntityManager em) {
        int affected = em.createQuery(
                "UPDATE Product SET stock = stock - :qty WHERE id = :id AND stock >= :qty")
                .setParameter("qty", quantity)
                .setParameter("id", productId)
                .executeUpdate();
        if (affected == 0) {
            throw new RuntimeException("Insufficient stock for product ID " + productId);
        }
    }
}
