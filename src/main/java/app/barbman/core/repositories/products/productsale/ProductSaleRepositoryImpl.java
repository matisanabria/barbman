package app.barbman.core.repositories.products.productsale;

import app.barbman.core.model.products.ProductSale;
import app.barbman.core.repositories.DbBootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository implementation for the `product_sales` table.
 * Provides CRUD operations and support for shared-connection transactions.
 */
public class ProductSaleRepositoryImpl implements ProductSaleRepository {
    private static final Logger logger = LogManager.getLogger(ProductSaleRepositoryImpl.class);
    private static final String PREFIX = "[PRODUCT-SALES-REPO]";

    private static final String SELECT_BASE = """
        SELECT id, date, total, payment_method_id, client_id
        FROM product_sales
        """;

    @Override
    public ProductSale findById(Integer id) {
        String sql = SELECT_BASE + " WHERE id = ?";

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }

        } catch (Exception e) {
            logger.error("{} Error fetching product sale ID {}: {}", PREFIX, id, e.getMessage());
        }

        return null;
    }

    @Override
    public List<ProductSale> findAll() {
        List<ProductSale> list = new ArrayList<>();

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(SELECT_BASE);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }

            logger.debug("{} Retrieved {} product sales.", PREFIX, list.size());

        } catch (Exception e) {
            logger.error("{} Error fetching all product sales: {}", PREFIX, e.getMessage());
        }

        return list;
    }

    /** Saves a product sale using a shared connection. */
    @Override
    public void save(ProductSale sale, Connection conn) throws SQLException {

        String sql = """
            INSERT INTO product_sales (date, total, payment_method_id, client_id)
            VALUES (?, ?, ?, ?)
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, sale.getDate().toString());
            ps.setDouble(2, sale.getTotal());
            ps.setInt(3, sale.getPaymentMethodId());
            ps.setObject(4, sale.getClientId());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) sale.setId(keys.getInt(1));
            }

            logger.info("{} ProductSale inserted successfully (ID={}) [shared]", PREFIX, sale.getId());
        }
    }

    @Override
    public void update(ProductSale sale, Connection conn) throws SQLException {

        String sql = """
            UPDATE product_sales
            SET date = ?, total = ?, payment_method_id = ?, client_id = ?
            WHERE id = ?
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, sale.getDate().toString());
            ps.setDouble(2, sale.getTotal());
            ps.setInt(3, sale.getPaymentMethodId());
            ps.setObject(4, sale.getClientId());
            ps.setInt(5, sale.getId());

            ps.executeUpdate();

            logger.info("{} ProductSale updated successfully (ID={}) [shared]", PREFIX, sale.getId());
        }
    }

    @Override
    public void delete(Integer id, Connection conn) throws SQLException {

        String sql = "DELETE FROM product_sales WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();

            logger.info("{} ProductSale deleted (ID={}) [shared]", PREFIX, id);
        }
    }

    @Override
    public void save(ProductSale sale) {
        try (Connection conn = DbBootstrap.connect()) {
            save(sale, conn);
        } catch (Exception e) {
            logger.error("{} Failed to save product sale: {}", PREFIX, e.getMessage());
        }
    }

    @Override
    public void update(ProductSale sale) {
        try (Connection conn = DbBootstrap.connect()) {
            update(sale, conn);
        } catch (Exception e) {
            logger.error("{} Failed to update product sale ID {}: {}", PREFIX, sale.getId(), e.getMessage());
        }
    }

    @Override
    public void delete(Integer id) {
        try (Connection conn = DbBootstrap.connect()) {
            delete(id, conn);
        } catch (Exception e) {
            logger.error("{} Failed to delete product sale ID {}: {}", PREFIX, id, e.getMessage());
        }
    }

    private ProductSale mapRow(ResultSet rs) throws SQLException {

        LocalDate date = LocalDate.parse(rs.getString("date"));

        Integer clientId = rs.getObject("client_id") != null
                ? rs.getInt("client_id")
                : null;

        return new ProductSale(
                rs.getInt("id"),
                date,
                rs.getDouble("total"),
                rs.getInt("payment_method_id"),
                clientId
        );
    }
}
