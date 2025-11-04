package app.barbman.core.repositories.services.service;

import app.barbman.core.model.services.Service;
import app.barbman.core.repositories.DbBootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServiceRepositoryImpl implements ServiceRepository {

    private static final Logger logger = LogManager.getLogger(ServiceRepositoryImpl.class);
    private static final String PREFIX = "[SERVICES-REPO]";

    // Base SELECT clause (used across multiple queries)
    private static final String SELECT_BASE = """
            SELECT id, user_id, date, payment_method_id, total, notes
            FROM services
            """;

    @Override
    public Service findById(Integer id) {
        String sql = SELECT_BASE + " WHERE id = ?";
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapRow(rs);
            }

        } catch (Exception e) {
            logger.error("{} Error finding Service by id {}: {}", PREFIX, id, e.getMessage());
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
        } catch (Exception e) {
            logger.error("{} Error fetching all services: {}", PREFIX, e.getMessage());
        }
        return list;
    }

    /**
     * Uses a shared connection.
     * Used on ServicesService to manage transactions for preparing statements and adds possibility of rollbacks
     * and multiple operations in a single transaction.
     */
    @Override
    public void save(Service s, Connection conn) throws SQLException {
        String sql = """
        INSERT INTO services (user_id, date, payment_method_id, total, notes)
        VALUES (?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getUserId());
            ps.setString(2, s.getDate().toString());
            ps.setInt(3, s.getPaymentMethodId());
            ps.setDouble(4, s.getTotal());
            ps.setString(5, s.getNotes());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) s.setId(keys.getInt(1));
            }

            logger.info("{} Service saved successfully (ID={}) [shared connection]", PREFIX, s.getId());
        }
    }
    @Override
    public void update(Service s, Connection conn) throws SQLException {
        String sql = """
        UPDATE services
        SET user_id = ?, date = ?, payment_method_id = ?, total = ?, notes = ?
        WHERE id = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, s.getUserId());
            ps.setString(2, s.getDate().toString());
            ps.setInt(3, s.getPaymentMethodId());
            ps.setDouble(4, s.getTotal());
            ps.setString(5, s.getNotes());
            ps.setInt(6, s.getId());
            ps.executeUpdate();

            logger.info("{} Service updated (ID={}) [shared connection]", PREFIX, s.getId());
        }
    }
    @Override
    public void delete(Integer id, Connection conn) throws SQLException {
        String sql = "DELETE FROM services WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            logger.info("{} Service deleted (ID={}) [shared connection]", PREFIX, id);
        }
    }


    /**
     * Uses their own connection.
     * Only for compatibility with the superinterface.
     */
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
            logger.error("{} Error updating service (ID={}): {}", PREFIX, s.getId(), e.getMessage());
        }
    }

    @Override
    public void delete(Integer id) {
        try (Connection conn = DbBootstrap.connect()) {
            delete(id, conn);
        } catch (Exception e) {
            logger.error("{} Error deleting service (ID={}): {}", PREFIX, id, e.getMessage());
        }
    }


    @Override
    public double getProduccionSemanalPorBarbero(int barberoId, LocalDate desde, LocalDate hasta) {
        double total = 0.0;
        String sql = """
            SELECT SUM(total)
            FROM services
            WHERE user_id = ? AND date BETWEEN ? AND ?
            """;

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, barberoId);
            ps.setString(2, desde.toString());
            ps.setString(3, hasta.toString());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                total = rs.getDouble(1);
            }

            logger.info("{} Weekly production for barber {}: {} Gs", PREFIX, barberoId, total);

        } catch (SQLException e) {
            logger.error("{} Error calculating weekly production: {}", PREFIX, e.getMessage());
        }

        return total;
    }

    private Service mapRow(ResultSet rs) throws SQLException {
        LocalDate date = LocalDate.parse(rs.getString("date"));
        return new Service(
                rs.getInt("id"),
                rs.getInt("user_id"),
                date,
                rs.getInt("payment_method_id"),
                rs.getDouble("total"),
                rs.getString("notes")
        );
    }
}
