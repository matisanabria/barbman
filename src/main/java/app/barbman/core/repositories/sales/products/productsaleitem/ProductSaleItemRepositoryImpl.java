package app.barbman.core.repositories.sales.products.productsaleitem;

import app.barbman.core.model.sales.products.ProductSaleItem;
import app.barbman.core.repositories.DbBootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductSaleItemRepositoryImpl implements ProductSaleItemRepository {

    private static final Logger logger = LogManager.getLogger(ProductSaleItemRepositoryImpl.class);
    private static final String PREFIX = "[PRODUCT-SALE-ITEM-REPO]";

    private static final String SELECT_BASE = """
        SELECT id, product_header_id, product_id, quantity, unit_price, item_total
        FROM product_sale_items
        """;

    @Override
    public ProductSaleItem findById(Integer id) {
        String sql = SELECT_BASE + " WHERE id = ?";

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return mapRow(rs);

        } catch (Exception e) {
            logger.error("{} Failed to fetch ProductSaleItem ID {}: {}", PREFIX, id, e.getMessage());
        }

        return null;
    }

    @Override
    public List<ProductSaleItem> findAll() {
        List<ProductSaleItem> list = new ArrayList<>();

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(SELECT_BASE);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (Exception e) {
            logger.error("{} Failed to load ProductSaleItems: {}", PREFIX, e.getMessage());
        }

        return list;
    }

    @Override
    public List<ProductSaleItem> findBySaleId(int productHeaderId) {
        List<ProductSaleItem> list = new ArrayList<>();
        String sql = SELECT_BASE + " WHERE product_header_id = ?";

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, productHeaderId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) list.add(mapRow(rs));

        } catch (Exception e) {
            logger.error("{} Failed to fetch items for ProductHeaderID {}: {}",
                    PREFIX, productHeaderId, e.getMessage());
        }

        return list;
    }

    @Override
    public void save(ProductSaleItem item) {
        try (Connection conn = DbBootstrap.connect()) {
            save(item, conn);
        } catch (Exception e) {
            logger.error("{} Failed to insert ProductSaleItem: {}", PREFIX, e.getMessage());
        }
    }

    @Override
    public void save(ProductSaleItem item, Connection conn) throws SQLException {

        String sql = """
            INSERT INTO product_sale_items
            (product_header_id, product_id, quantity, unit_price, item_total)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, item.getProductHeaderId());
            ps.setInt(2, item.getProductId());
            ps.setInt(3, item.getQuantity());
            ps.setDouble(4, item.getUnitPrice());
            ps.setDouble(5, item.getItemTotal());

            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) item.setId(keys.getInt(1));
        }
    }
    @Override
    public void update(ProductSaleItem item) {

        String sql = """
        UPDATE product_sale_items
        SET product_header_id = ?, product_id = ?, quantity = ?, unit_price = ?, item_total = ?
        WHERE id = ?
        """;

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, item.getProductHeaderId());
            ps.setInt(2, item.getProductId());
            ps.setInt(3, item.getQuantity());
            ps.setDouble(4, item.getUnitPrice());
            ps.setDouble(5, item.getItemTotal());
            ps.setInt(6, item.getId());

            ps.executeUpdate();

            logger.info("{} Updated ProductSaleItem ID={}", PREFIX, item.getId());

        } catch (Exception e) {
            logger.error("{} Failed to update ProductSaleItem ID {}: {}",
                    PREFIX, item.getId(), e.getMessage());
        }
    }

    @Override
    public void deleteBySaleId(int productHeaderId, Connection conn) throws SQLException {
        String sql = "DELETE FROM product_sale_items WHERE product_header_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productHeaderId);
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM product_sale_items WHERE id = ?";

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (Exception e) {
            logger.error("{} Failed to delete ProductSaleItem ID {}: {}", PREFIX, id, e.getMessage());
        }
    }

    private ProductSaleItem mapRow(ResultSet rs) throws SQLException {
        return new ProductSaleItem(
                rs.getInt("id"),
                rs.getInt("product_header_id"),
                rs.getInt("product_id"),
                rs.getInt("quantity"),
                rs.getDouble("unit_price"),
                rs.getDouble("item_total")
        );
    }
}
