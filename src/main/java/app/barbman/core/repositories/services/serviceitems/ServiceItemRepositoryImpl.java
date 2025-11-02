package app.barbman.core.repositories.services.serviceitems;

import app.barbman.core.model.services.ServiceItem;
import app.barbman.core.repositories.DbBootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceItemRepositoryImpl implements ServiceItemRepository{
    private static final Logger logger = LogManager.getLogger(ServiceItemRepositoryImpl.class);
    private static final String PREFIX = "[SERVICEITEMS-REPO]";

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

            if (rs.next()) {
                return mapRow(rs);
            }

        } catch (Exception e) {
            logger.error("{} Error finding ServiceItem by id {}: {}", PREFIX, id, e.getMessage());
        }
        return null;
    }

    @Override
    public List<ServiceItem> findAll() {
        List<ServiceItem> list = new ArrayList<>();
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(SELECT_BASE);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (Exception e) {
            logger.error("{} Error fetching all ServiceItems: {}", PREFIX, e.getMessage());
        }
        return list;
    }

    /**
     * Fetches all ServiceItems associated with a specific service ID.
     */
    @Override
    public List<ServiceItem> findByServiceId(int serviceId) {
        List<ServiceItem> list = new ArrayList<>();
        String sql = SELECT_BASE + " WHERE service_id = ?";

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, serviceId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (Exception e) {
            logger.error("{} Error fetching items for service {}: {}", PREFIX, serviceId, e.getMessage());
        }
        return list;
    }

    @Override
    public void save(ServiceItem item) {
        String sql = """
            INSERT INTO service_items (service_id, service_type_id, price)
            VALUES (?, ?, ?)
            """;

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, item.getServiceId());
            ps.setInt(2, item.getServiceTypeId());
            ps.setDouble(3, item.getPrice());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) item.setId(keys.getInt(1));
            }

            logger.info("{} ServiceItem saved (ID={}, ServiceID={})", PREFIX, item.getId(), item.getServiceId());

        } catch (Exception e) {
            logger.error("{} Error saving ServiceItem: {}", PREFIX, e.getMessage());
        }
    }

    @Override
    public void update(ServiceItem item) {
        String sql = """
            UPDATE service_items
            SET service_id = ?, service_type_id = ?, price = ?
            WHERE id = ?
            """;

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, item.getServiceId());
            ps.setInt(2, item.getServiceTypeId());
            ps.setDouble(3, item.getPrice());
            ps.setInt(4, item.getId());
            ps.executeUpdate();

            logger.info("{} ServiceItem updated (ID={})", PREFIX, item.getId());

        } catch (Exception e) {
            logger.error("{} Error updating ServiceItem (ID={}): {}", PREFIX, item.getId(), e.getMessage());
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM service_items WHERE id = ?";
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

            logger.info("{} ServiceItem deleted (ID={})", PREFIX, id);

        } catch (Exception e) {
            logger.error("{} Error deleting ServiceItem (ID={}): {}", PREFIX, id, e.getMessage());
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
