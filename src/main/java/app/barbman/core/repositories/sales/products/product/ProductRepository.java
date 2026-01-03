package app.barbman.core.repositories.sales.products.product;

import app.barbman.core.model.sales.products.Product;
import app.barbman.core.repositories.GenericRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Repository interface for Product records.
 */
public interface ProductRepository extends GenericRepository<Product, Integer> {

    Product findByName(String name);

    List<Product> findAllInStock();

    public void decreaseStock(int productId, int quantity, Connection conn) throws SQLException;
}