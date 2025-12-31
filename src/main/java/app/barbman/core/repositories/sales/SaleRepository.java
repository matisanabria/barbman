package app.barbman.core.repositories.sales;

import app.barbman.core.model.sales.Sale;
import app.barbman.core.repositories.GenericRepository;

import java.sql.Connection;
import java.sql.SQLException;

public interface SaleRepository extends GenericRepository<Sale, Integer> {

    void save(Sale sale, Connection conn) throws SQLException;
    void update(Sale sale, Connection conn) throws SQLException;
    void delete(Integer id, Connection conn) throws SQLException;
}
