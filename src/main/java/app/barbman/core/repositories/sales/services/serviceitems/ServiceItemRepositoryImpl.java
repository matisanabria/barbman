package app.barbman.core.repositories.sales.services.serviceitems;

import app.barbman.core.model.sales.services.ServiceItem;
import app.barbman.core.repositories.DbBootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository implementation for the `service_items` table.
 * Supports normal CRUD operations as well as transactional
 * operations using a shared JDBC connection.
 */
public class ServiceItemRepositoryImpl implements ServiceItemRepository {

    private static final Logger logger = LogManager.getLogger(ServiceItemRepositoryImpl.class);
    private static final String PREFIX = "[SERVICE-ITEMS-REPO]";

    private static final String SELECT_BASE = """
        SELECT id, service_header_id, service_definition_id, quantity, unit_price, item_total
        FROM service_item
        """;

    @Override
    public ServiceItem findById(Integer id) {
        String sql = SELECT_BASE + " WHERE id = ?";

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return mapRow(rs);

        } catch (Exception e) {
            logger.error("{} Failed to fetch ServiceItem ID {}: {}", PREFIX, id, e.getMessage());
        }
        return null;
    }

    @Override
    public List<ServiceItem> findAll() {
        List<ServiceItem> list = new ArrayList<>();

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(SELECT_BASE);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));

            logger.debug("{} Loaded {} ServiceItems.", PREFIX, list.size());

        } catch (Exception e) {
            logger.error("{} Failed to load ServiceItems: {}", PREFIX, e.getMessage());
        }

        return list;
    }

    @Override
    public List<ServiceItem> findByServiceId(int serviceHeaderId) {
        List<ServiceItem> list = new ArrayList<>();
        String sql = SELECT_BASE + " WHERE service_header_id = ?";

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, serviceHeaderId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) list.add(mapRow(rs));

            logger.debug("{} Loaded {} items for ServiceHeaderID={}.",
                    PREFIX, list.size(), serviceHeaderId);

        } catch (Exception e) {
            logger.error("{} Failed to fetch items for ServiceHeaderID {}: {}",
                    PREFIX, serviceHeaderId, e.getMessage());
        }

        return list;
    }

    @Override
    public void save(ServiceItem item) {
        try (Connection conn = DbBootstrap.connect()) {
            save(item, conn);
        } catch (Exception e) {
            logger.error("{} Failed to insert ServiceItem: {}", PREFIX, e.getMessage());
        }
    }

    @Override
    public void save(ServiceItem item, Connection conn) throws SQLException {

        String sql = """
            INSERT INTO service_item
            (service_header_id, service_definition_id, quantity, unit_price, item_total)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, item.getServiceHeaderId());
            ps.setInt(2, item.getServiceDefinitionId());
            ps.setInt(3, item.getQuantity());
            ps.setDouble(4, item.getUnitPrice());
            ps.setDouble(5, item.getItemTotal());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) item.setId(keys.getInt(1));
            }

            logger.info("{} Inserted ServiceItem ID={} (ServiceHeaderID={}) [shared]",
                    PREFIX, item.getId(), item.getServiceHeaderId());
        }
    }

    @Override
    public void update(ServiceItem item) {
        try (Connection conn = DbBootstrap.connect()) {
            update(item, conn);
        } catch (Exception e) {
            logger.error("{} Failed to update ServiceItem ID {}: {}",
                    PREFIX, item.getId(), e.getMessage());
        }
    }

    @Override
    public void update(ServiceItem item, Connection conn) throws SQLException {

        String sql = """
            UPDATE service_item
            SET service_header_id = ?, service_definition_id = ?, quantity = ?, unit_price = ?, item_total = ?
            WHERE id = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, item.getServiceHeaderId());
            ps.setInt(2, item.getServiceDefinitionId());
            ps.setInt(3, item.getQuantity());
            ps.setDouble(4, item.getUnitPrice());
            ps.setDouble(5, item.getItemTotal());
            ps.setInt(6, item.getId());

            ps.executeUpdate();

            logger.info("{} Updated ServiceItem ID={} [shared]", PREFIX, item.getId());
        }
    }

    @Override
    public void delete(Integer id) {
        try (Connection conn = DbBootstrap.connect()) {
            delete(id, conn);
        } catch (Exception e) {
            logger.error("{} Failed to delete ServiceItem ID {}: {}", PREFIX, id, e.getMessage());
        }
    }

    @Override
    public void delete(Integer id, Connection conn) throws SQLException {

        String sql = "DELETE FROM service_item WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();

            logger.info("{} Deleted ServiceItem ID={} [shared]", PREFIX, id);
        }
    }

    private ServiceItem mapRow(ResultSet rs) throws SQLException {
        return new ServiceItem(
                rs.getInt("id"),
                rs.getInt("service_header_id"),
                rs.getInt("service_definition_id"),
                rs.getInt("quantity"),
                rs.getDouble("unit_price"),
                rs.getDouble("item_total")
        );
    }
}
