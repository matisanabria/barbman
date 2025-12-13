package app.barbman.core.repositories.products.productsaleitem;

import app.barbman.core.model.products.ProductSaleItem;
import app.barbman.core.repositories.DbBootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductSaleItemRepositoryImpl implements ProductSaleItemRepository  {
    private static final Logger logger = LogManager.getLogger(ProductSaleItemRepositoryImpl.class);
    private static final String PREFIX = "[PRODUCT-SALE-ITEM-REPO]";

    private static final String SELECT_BASE = """
        SELECT id, sale_id, product_id, quantity, unit_price
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

            logger.debug("{} Loaded {} ProductSaleItems.", PREFIX, list.size());

        } catch (Exception e) {
            logger.error("{} Failed to load ProductSaleItems: {}", PREFIX, e.getMessage());
        }

        return list;
    }

    @Override
    public List<ProductSaleItem> findBySaleId(int saleId) {
        List<ProductSaleItem> list = new ArrayList<>();
        String sql = SELECT_BASE + " WHERE sale_id = ?";

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, saleId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) list.add(mapRow(rs));

            logger.debug("{} Loaded {} items for ProductSale ID {}.", PREFIX, list.size(), saleId);

        } catch (Exception e) {
            logger.error("{} Failed to fetch items for sale {}: {}", PREFIX, saleId, e.getMessage());
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
            INSERT INTO product_sale_items (sale_id, product_id, quantity, unit_price)
            VALUES (?, ?, ?, ?)
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, item.getSaleId());
            ps.setInt(2, item.getProductId());
            ps.setInt(3, item.getQuantity());
            ps.setDouble(4, item.getUnitPrice());

            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) item.setId(keys.getInt(1));

            logger.info("{} Inserted ProductSaleItem ID={} (SaleID={})",
                    PREFIX, item.getId(), item.getSaleId());
        }
    }

    @Override
    public void update(ProductSaleItem item) {
        String sql = """
            UPDATE product_sale_items
            SET sale_id = ?, product_id = ?, quantity = ?, unit_price = ?
            WHERE id = ?
            """;

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, item.getSaleId());
            ps.setInt(2, item.getProductId());
            ps.setInt(3, item.getQuantity());
            ps.setDouble(4, item.getUnitPrice());
            ps.setInt(5, item.getId());

            ps.executeUpdate();

            logger.info("{} Updated ProductSaleItem ID={}", PREFIX, item.getId());

        } catch (Exception e) {
            logger.error("{} Failed to update ProductSaleItem ID {}: {}", PREFIX, item.getId(), e.getMessage());
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM product_sale_items WHERE id = ?";

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

            logger.info("{} Deleted ProductSaleItem ID={}", PREFIX, id);

        } catch (Exception e) {
            logger.error("{} Failed to delete ProductSaleItem ID {}: {}", PREFIX, id, e.getMessage());
        }
    }

    @Override
    public void deleteBySaleId(int saleId, Connection conn) throws SQLException {
        String sql = "DELETE FROM product_sale_items WHERE sale_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, saleId);
            ps.executeUpdate();

            logger.info("{} Deleted all ProductSaleItems for SaleID={}", PREFIX, saleId);
        }
    }

    private ProductSaleItem mapRow(ResultSet rs) throws SQLException {
        return new ProductSaleItem(
                rs.getInt("id"),
                rs.getInt("sale_id"),
                rs.getInt("product_id"),
                rs.getInt("quantity"),
                rs.getDouble("unit_price")
        );
    }
}
