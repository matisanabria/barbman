package app.barbman.core.repositories.sales.products.productsaleitem;

import app.barbman.core.model.sales.products.ProductSaleItem;
import jakarta.persistence.EntityManager;

import java.util.List;

public interface ProductSaleItemRepository {
    List<ProductSaleItem> findBySaleId(int productHeaderId);

    void save(ProductSaleItem item, EntityManager em);
    void deleteBySaleId(int productHeaderId, EntityManager em);

    void delete(Integer id);
    List<ProductSaleItem> findAll();
}
