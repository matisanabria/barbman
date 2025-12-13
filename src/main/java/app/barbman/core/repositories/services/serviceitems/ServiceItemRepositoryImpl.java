package app.barbman.core.repositories.services.serviceitems;

import app.barbman.core.model.services.ServiceItem;
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
        SELECT id, service_id, service_type_id, price
        FROM service_items
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
    public List<ServiceItem> findByServiceId(int serviceId) {
        List<ServiceItem> list = new ArrayList<>();
        String sql = SELECT_BASE + " WHERE service_id = ?";

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, serviceId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) list.add(mapRow(rs));

            logger.debug("{} Loaded {} items for ServiceID={}.", PREFIX, list.size(), serviceId);

        } catch (Exception e) {
            logger.error("{} Failed to fetch items for ServiceID {}: {}", PREFIX, serviceId, e.getMessage());
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
            INSERT INTO service_items (service_id, service_type_id, price)
            VALUES (?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, item.getServiceId());
            ps.setInt(2, item.getServiceTypeId());
            ps.setDouble(3, item.getPrice());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) item.setId(keys.getInt(1));
            }

            logger.info("{} Inserted ServiceItem ID={} (ServiceID={}) [shared]",
                    PREFIX, item.getId(), item.getServiceId());
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
            UPDATE service_items
            SET service_id = ?, service_type_id = ?, price = ?
            WHERE id = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, item.getServiceId());
            ps.setInt(2, item.getServiceTypeId());
            ps.setDouble(3, item.getPrice());
            ps.setInt(4, item.getId());

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
        String sql = "DELETE FROM service_items WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();

            logger.info("{} Deleted ServiceItem ID={} [shared]", PREFIX, id);
        }
    }

    private ServiceItem mapRow(ResultSet rs) throws SQLException {
        return new ServiceItem(
                rs.getInt("id"),
                rs.getInt("service_id"),
                rs.getInt("service_type_id"),
                rs.getDouble("price")
        );
    }
}
