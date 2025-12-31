package app.barbman.core.repositories.sales.products.product;

import app.barbman.core.model.sales.products.Product;
import app.barbman.core.repositories.DbBootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository implementation for the `products` table.
 * Provides CRUD operations plus additional queries for lookup and stock filtering.
 */
public class ProductRepositoryImpl implements ProductRepository {

    private static final Logger logger = LogManager.getLogger(ProductRepositoryImpl.class);
    private static final String PREFIX = "[PRODUCT-REPO]";

    private static final String SELECT_BASE = """
        SELECT id, name, cost_price, unit_price, stock, category, brand, image_path, notes
        FROM products
        """;

    @Override
    public Product findById(Integer id) {
        String sql = SELECT_BASE + " WHERE id = ?";

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return mapRow(rs);

        } catch (Exception e) {
            logger.error("{} Failed to fetch Product ID {}: {}", PREFIX, id, e.getMessage());
        }

        return null;
    }

    @Override
    public Product findByName(String name) {
        String sql = SELECT_BASE + " WHERE name = ?";

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return mapRow(rs);

        } catch (Exception e) {
            logger.error("{} Failed to fetch product '{}': {}", PREFIX, name, e.getMessage());
        }

        return null;
    }

    @Override
    public List<Product> findAll() {
        List<Product> list = new ArrayList<>();

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(SELECT_BASE);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));

            logger.debug("{} Loaded {} products.", PREFIX, list.size());

        } catch (Exception e) {
            logger.error("{} Failed to load products: {}", PREFIX, e.getMessage());
        }

        return list;
    }

    @Override
    public List<Product> findAllInStock() {
        List<Product> list = new ArrayList<>();
        String sql = SELECT_BASE + " WHERE stock > 0";

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));

            logger.debug("{} Loaded {} products with stock > 0.", PREFIX, list.size());

        } catch (Exception e) {
            logger.error("{} Failed to load in-stock products: {}", PREFIX, e.getMessage());
        }

        return list;
    }

    @Override
    public void save(Product p) {
        String sql = """
            INSERT INTO products
            (name, cost_price, unit_price, stock, category, brand, image_path, notes)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getName());
            ps.setDouble(2, p.getCostPrice());
            ps.setDouble(3, p.getUnitPrice());
            ps.setInt(4, p.getStock());
            ps.setString(5, p.getCategory());
            ps.setString(6, p.getBrand());
            ps.setString(7, p.getImagePath());
            ps.setString(8, p.getNotes());

            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) p.setId(keys.getInt(1));

            logger.info("{} Inserted Product ID={} ('{}')", PREFIX, p.getId(), p.getName());

        } catch (Exception e) {
            logger.error("{} Failed to insert product '{}': {}", PREFIX, p.getName(), e.getMessage());
        }
    }

    @Override
    public void update(Product p) {
        String sql = """
            UPDATE products
            SET name = ?, cost_price = ?, unit_price = ?, stock = ?,
                category = ?, brand = ?, image_path = ?, notes = ?
            WHERE id = ?
            """;

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setString(1, p.getName());
            ps.setDouble(2, p.getCostPrice());
            ps.setDouble(3, p.getUnitPrice());
            ps.setInt(4, p.getStock());
            ps.setString(5, p.getCategory());
            ps.setString(6, p.getBrand());
            ps.setString(7, p.getImagePath());
            ps.setString(8, p.getNotes());
            ps.setInt(9, p.getId());

            ps.executeUpdate();

            logger.info("{} Updated Product ID={} ('{}')", PREFIX, p.getId(), p.getName());

        } catch (Exception e) {
            logger.error("{} Failed to update product ID {}: {}", PREFIX, p.getId(), e.getMessage());
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM products WHERE id = ?";

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

            logger.info("{} Deleted Product ID={}", PREFIX, id);

        } catch (Exception e) {
            logger.error("{} Failed to delete Product ID {}: {}", PREFIX, id, e.getMessage());
        }
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getDouble("cost_price"),
                rs.getDouble("unit_price"),
                rs.getInt("stock"),
                rs.getString("category"),
                rs.getString("brand"),
                rs.getString("image_path"),
                rs.getString("notes")
        );
    }
}
