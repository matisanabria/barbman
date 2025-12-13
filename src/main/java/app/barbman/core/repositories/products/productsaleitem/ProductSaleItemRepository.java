package app.barbman.core.repositories.products.productsaleitem;

import app.barbman.core.model.products.ProductSaleItem;
import app.barbman.core.repositories.GenericRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Repository interface for `product_sale_items`.
 * Supports standard CRUD plus shared-connection operations for transactional sales.
 */
public interface ProductSaleItemRepository extends GenericRepository<ProductSaleItem, Integer> {
    List<ProductSaleItem> findBySaleId(int saleId);

    void save(ProductSaleItem item, Connection conn) throws SQLException;
    void deleteBySaleId(int saleId, Connection conn) throws SQLException;
}
