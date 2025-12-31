package app.barbman.core.repositories.sales.services.servicedefinition;

import app.barbman.core.model.sales.services.ServiceDefinition;
import app.barbman.core.repositories.DbBootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository implementation for the `service_definitions` table.
 * Provides CRUD operations and filtered queries.
 */
public class ServiceDefinitionRepositoryImpl implements ServiceDefinitionRepository {

    private static final Logger logger = LogManager.getLogger(ServiceDefinitionRepositoryImpl.class);
    private static final String PREFIX = "[SERVICE-DEFS-REPO]";

    /** Base SELECT clause reused across queries */
    private static final String SELECT_BASE = """
        SELECT id, name, base_price, available
        FROM service_definitions
        """;

    @Override
    public ServiceDefinition findById(Integer id) {
        String sql = SELECT_BASE + " WHERE id = ?";

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }

        } catch (Exception e) {
            logger.error("{} Failed to fetch ServiceDefinition ID {}: {}", PREFIX, id, e.getMessage());
        }

        return null;
    }

    @Override
    public List<ServiceDefinition> findAll() {
        List<ServiceDefinition> list = new ArrayList<>();

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(SELECT_BASE);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));

            logger.debug("{} Loaded {} serviceheader definitions.", PREFIX, list.size());

        } catch (Exception e) {
            logger.error("{} Failed to fetch serviceheader definitions: {}", PREFIX, e.getMessage());
        }

        return list;
    }

    /**
     * Returns all serviceheader definitions where available = 1.
     */
    @Override
    public List<ServiceDefinition> findAllAvailable() {
        String sql = SELECT_BASE + " WHERE available = 1";
        List<ServiceDefinition> list = new ArrayList<>();

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));

            logger.debug("{} Loaded {} available serviceheader definitions.", PREFIX, list.size());

        } catch (Exception e) {
            logger.error("{} Failed to fetch available serviceheader definitions: {}", PREFIX, e.getMessage());
        }

        return list;
    }

    @Override
    public void save(ServiceDefinition service) {
        String sql = """
            INSERT INTO service_definitions (name, base_price, available)
            VALUES (?, ?, ?)
            """;

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, service.getName());
            ps.setDouble(2, service.getBasePrice());
            ps.setInt(3, service.isAvailable() ? 1 : 0);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) service.setId(keys.getInt(1));
            }

            logger.info("{} Inserted ServiceDefinition '{}', ID={}", PREFIX, service.getName(), service.getId());

        } catch (Exception e) {
            logger.error("{} Failed to save ServiceDefinition '{}': {}", PREFIX, service.getName(), e.getMessage());
        }
    }

    @Override
    public void update(ServiceDefinition service) {
        String sql = """
            UPDATE service_definitions
            SET name = ?, base_price = ?, available = ?
            WHERE id = ?
            """;

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setString(1, service.getName());
            ps.setDouble(2, service.getBasePrice());
            ps.setInt(3, service.isAvailable() ? 1 : 0);
            ps.setInt(4, service.getId());

            ps.executeUpdate();

            logger.info("{} Updated ServiceDefinition ID={} ({})", PREFIX, service.getId(), service.getName());

        } catch (Exception e) {
            logger.error("{} Failed to update ServiceDefinition ID {}: {}", PREFIX, service.getId(), e.getMessage());
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM service_definitions WHERE id = ?";

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

            logger.info("{} Deleted ServiceDefinition ID={}", PREFIX, id);

        } catch (Exception e) {
            logger.error("{} Failed to delete ServiceDefinition ID {}: {}", PREFIX, id, e.getMessage());
        }
    }


    private ServiceDefinition mapRow(ResultSet rs) throws SQLException {
        return new ServiceDefinition(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getDouble("base_price"),
                rs.getInt("available") == 1
        );
    }
}
