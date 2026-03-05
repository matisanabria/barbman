package app.barbman.core.repositories.sales.products.productsaleitem;

import app.barbman.core.model.sales.products.ProductSaleItem;
import app.barbman.core.repositories.GenericRepository;
import jakarta.persistence.EntityManager;

import java.util.List;

public interface ProductSaleItemRepository extends GenericRepository<ProductSaleItem, Integer> {
    List<ProductSaleItem> findBySaleId(int productHeaderId);

    void save(ProductSaleItem item, EntityManager em);
    void deleteBySaleId(int productHeaderId, EntityManager em);
}
