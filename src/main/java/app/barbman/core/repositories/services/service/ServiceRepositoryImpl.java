package app.barbman.core.repositories.services.service;

import app.barbman.core.model.services.Service;
import app.barbman.core.repositories.DbBootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository implementation for the `services` table.
 * Provides CRUD operations and shared-connection transaction support.
 */
public class ServiceRepositoryImpl implements ServiceRepository {

    private static final Logger logger = LogManager.getLogger(ServiceRepositoryImpl.class);
    private static final String PREFIX = "[SERVICE-REPO]";

    /** Base SELECT clause used in all read queries */
    private static final String SELECT_BASE = """
            SELECT id, user_id, client_id, quantity, date, payment_method_id, total, notes
            FROM services
            """;

    @Override
    public Service findById(Integer id) {
        String sql = SELECT_BASE + " WHERE id = ?";

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }

        } catch (Exception e) {
            logger.error("{} Error fetching service ID {}: {}", PREFIX, id, e.getMessage());
        }

        return null;
    }

    @Override
    public List<Service> findAll() {
        List<Service> list = new ArrayList<>();

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(SELECT_BASE);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }

            logger.debug("{} Retrieved {} services.", PREFIX, list.size());

        } catch (Exception e) {
            logger.error("{} Error fetching all services: {}", PREFIX, e.getMessage());
        }

        return list;
    }

    /**
     * Saves a service record using a shared connection.
     * Enables ServicesService to run multiple operations under a single transaction
     * (e.g. inserting service + service_items + movements).
     */
    @Override
    public void save(Service s, Connection conn) throws SQLException {

        String sql = """
            INSERT INTO services (user_id, client_id, quantity, date, payment_method_id, total, notes)
            VALUES (?, ?, ?, ?, ?, ?. ?)
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, s.getUserId());
            ps.setObject(2, s.getClientId()); // may be NULL
            ps.setInt(3, s.getQuantity());
            ps.setString(4, s.getDate().toString());
            ps.setInt(5, s.getPaymentMethodId());
            ps.setDouble(6, s.getTotal());
            ps.setString(7, s.getNotes());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) s.setId(keys.getInt(1));
            }

            logger.info("{} Service inserted successfully (ID={}) [shared]", PREFIX, s.getId());
        }
    }

    /**
     * Updates an existing service using a shared connection.
     */
    @Override
    public void update(Service s, Connection conn) throws SQLException {

        String sql = """
            UPDATE services
            SET user_id = ?, client_id = ?, quantity = ?, date = ?, payment_method_id = ?, total = ?, notes = ?
            WHERE id = ?
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, s.getUserId());
            ps.setObject(2, s.getClientId());
            ps.setInt(3, s.getQuantity());
            ps.setString(4, s.getDate().toString());
            ps.setInt(5, s.getPaymentMethodId());
            ps.setDouble(6, s.getTotal());
            ps.setString(7, s.getNotes());
            ps.setInt(8, s.getId());

            ps.executeUpdate();

            logger.info("{} Service updated successfully (ID={}) [shared]", PREFIX, s.getId());
        }
    }

    /**
     * Deletes a service using a shared connection.
     */
    @Override
    public void delete(Integer id, Connection conn) throws SQLException {
        String sql = "DELETE FROM services WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();

            logger.info("{} Service deleted successfully (ID={}) [shared]", PREFIX, id);
        }
    }

    @Override
    public void save(Service s) {
        try (Connection conn = DbBootstrap.connect()) {
            save(s, conn);
        } catch (Exception e) {
            logger.error("{} Failed to save service: {}", PREFIX, e.getMessage());
        }
    }

    @Override
    public void update(Service s) {
        try (Connection conn = DbBootstrap.connect()) {
            update(s, conn);
        } catch (Exception e) {
            logger.error("{} Failed to update service ID {}: {}", PREFIX, s.getId(), e.getMessage());
        }
    }

    @Override
    public void delete(Integer id) {
        try (Connection conn = DbBootstrap.connect()) {
            delete(id, conn);
        } catch (Exception e) {
            logger.error("{} Failed to delete service ID {}: {}", PREFIX, id, e.getMessage());
        }
    }

    /**
     * Calculates the sum of service totals for a user within a date range.
     */
    @Override
    public double sumServiceTotalsByUserAndDateRange(int barberId, LocalDate from, LocalDate to) {
        double total = 0.0;

        String sql = """
            SELECT SUM(total)
            FROM services
            WHERE user_id = ? AND date BETWEEN ? AND ?
            """;

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, barberId);
            ps.setString(2, from.toString());
            ps.setString(3, to.toString());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) total = rs.getDouble(1);

            logger.info("{} Weekly production for user {} = {} Gs", PREFIX, barberId, total);

        } catch (SQLException e) {
            logger.error("{} Error getting weekly production: {}", PREFIX, e.getMessage());
        }

        return total;
    }

    private Service mapRow(ResultSet rs) throws SQLException {

        LocalDate date = LocalDate.parse(rs.getString("date"));

        Integer clientId = rs.getObject("client_id") != null
                ? rs.getInt("client_id")
                : null;

        return new Service(
                rs.getInt("id"),
                rs.getInt("user_id"),
                date,
                rs.getInt("payment_method_id"),
                rs.getDouble("total"),
                rs.getString("notes"),
                clientId,
                rs.getInt("quantity")
        );
    }
}
