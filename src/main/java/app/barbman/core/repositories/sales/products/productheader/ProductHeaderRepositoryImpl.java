package app.barbman.core.repositories.sales.products.productheader;

import app.barbman.core.model.sales.products.ProductHeader;
import app.barbman.core.repositories.DbBootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository implementation for the `product_sales` table.
 * Provides CRUD operations and support for shared-connection transactions.
 */
public class ProductHeaderRepositoryImpl implements ProductHeaderRepository {

    private static final Logger logger = LogManager.getLogger(ProductHeaderRepositoryImpl.class);
    private static final String PREFIX = "[PRODUCT-SALES-REPO]";

    private static final String SELECT_BASE = """
        SELECT id, sale_id, subtotal
        FROM product_sales
        """;

    @Override
    public ProductHeader findById(Integer id) {
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
    public List<ProductHeader> findAll() {
        List<ProductHeader> list = new ArrayList<>();

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
    public void save(ProductHeader header, Connection conn) throws SQLException {

        String sql = """
            INSERT INTO product_sales (sale_id, subtotal)
            VALUES (?, ?)
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, header.getSaleId());
            ps.setDouble(2, header.getSubtotal());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) header.setId(keys.getInt(1));
            }

            logger.info("{} ProductHeader inserted successfully (ID={}) [shared]",
                    PREFIX, header.getId());
        }
    }

    @Override
    public void update(ProductHeader header, Connection conn) throws SQLException {

        String sql = """
            UPDATE product_sales
            SET sale_id = ?, subtotal = ?
            WHERE id = ?
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, header.getSaleId());
            ps.setDouble(2, header.getSubtotal());
            ps.setInt(3, header.getId());

            ps.executeUpdate();

            logger.info("{} ProductHeader updated successfully (ID={}) [shared]",
                    PREFIX, header.getId());
        }
    }

    @Override
    public void delete(Integer id, Connection conn) throws SQLException {

        String sql = "DELETE FROM product_sales WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();

            logger.info("{} ProductHeader deleted (ID={}) [shared]", PREFIX, id);
        }
    }

    @Override
    public void save(ProductHeader header) {
        try (Connection conn = DbBootstrap.connect()) {
            save(header, conn);
        } catch (Exception e) {
            logger.error("{} Failed to save product sale: {}", PREFIX, e.getMessage());
        }
    }

    @Override
    public void update(ProductHeader header) {
        try (Connection conn = DbBootstrap.connect()) {
            update(header, conn);
        } catch (Exception e) {
            logger.error("{} Failed to update product sale ID {}: {}",
                    PREFIX, header.getId(), e.getMessage());
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

    private ProductHeader mapRow(ResultSet rs) throws SQLException {
        return new ProductHeader(
                rs.getInt("id"),
                rs.getInt("sale_id"),
                rs.getDouble("subtotal")
        );
    }
}
