package app.barbman.core.repositories.sales.products.productheader;

import app.barbman.core.model.sales.products.ProductHeader;
import app.barbman.core.repositories.GenericRepository;

import java.sql.Connection;
import java.sql.SQLException;

public interface ProductHeaderRepository extends GenericRepository<ProductHeader, Integer> {
    void save(ProductHeader sale, Connection conn) throws SQLException;
    void update(ProductHeader sale, Connection conn) throws SQLException;
    void delete(Integer id, Connection conn) throws SQLException;
}
