package app.barbman.core.repositories.products.productsale;

import app.barbman.core.model.products.ProductSale;
import app.barbman.core.repositories.GenericRepository;

import java.sql.Connection;
import java.sql.SQLException;

public interface ProductSaleRepository extends GenericRepository<ProductSale, Integer> {
    void save(ProductSale sale, Connection conn) throws SQLException;
    void update(ProductSale sale, Connection conn) throws SQLException;
    void delete(Integer id, Connection conn) throws SQLException;
}
