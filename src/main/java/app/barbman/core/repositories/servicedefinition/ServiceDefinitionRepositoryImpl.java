package app.barbman.core.repositories.servicedefinition;

import app.barbman.core.model.ServiceDefinition;
import app.barbman.core.repositories.DbBootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDefinitionRepositoryImpl implements ServiceDefinitionRepository {

    private static final Logger logger = LogManager.getLogger(ServiceDefinitionRepositoryImpl.class);
    private static final String PREFIX = "[SERVDEFINITIONS-REPO]";

    private static final String SELECT_BASE = """
        SELECT id, name, base_price
        FROM service_definitions
        """;

    @Override
    public ServiceDefinition findById(Integer id) {
        String sql = SELECT_BASE + " WHERE id = ?";
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (Exception e) {
            logger.warn("{} Error finding ServiceDefinition by id {}: {}", PREFIX, id, e.getMessage());
        }
        return null;
    }

    @Override
    public List<ServiceDefinition> findAll() {
        List<ServiceDefinition> list = new ArrayList<>();
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(SELECT_BASE);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            logger.warn("{} Error fetching all ServiceDefinitions: {}", PREFIX, e.getMessage());
        }
        return list;
    }

    @Override
    public void save(ServiceDefinition service) {
        String sql = """
            INSERT INTO service_definitions (name, base_price)
            VALUES (?, ?)
            """;
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, service.getName());
            ps.setDouble(2, service.getBasePrice());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    service.setId(keys.getInt(1));
                }
            }
        } catch (Exception e) {
            logger.warn("{} Error saving ServiceDefinition: {}", PREFIX, e.getMessage());
        }
    }

    @Override
    public void update(ServiceDefinition service) {
        String sql = """
            UPDATE service_definitions
            SET name = ?, base_price = ?
            WHERE id = ?
            """;
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setString(1, service.getName());
            ps.setDouble(2, service.getBasePrice());
            ps.setInt(3, service.getId());

            ps.executeUpdate();
        } catch (Exception e) {
            logger.warn("{} Error updating ServiceDefinition id {}: {}", PREFIX, service.getId(), e.getMessage());
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM service_definitions WHERE id = ?";
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            logger.warn("{} Error deleting ServiceDefinition id {}: {}", PREFIX, id, e.getMessage());
        }
    }

    private ServiceDefinition mapRow(ResultSet rs) throws SQLException {
        return new ServiceDefinition(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getDouble("base_price")
        );
    }
}
