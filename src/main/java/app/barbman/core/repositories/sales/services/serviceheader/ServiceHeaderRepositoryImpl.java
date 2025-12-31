package app.barbman.core.repositories.sales.services.serviceheader;

import app.barbman.core.model.sales.services.ServiceHeader;
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
public class ServiceHeaderRepositoryImpl implements ServiceHeaderRepository {

    private static final Logger logger = LogManager.getLogger(ServiceHeaderRepositoryImpl.class);
    private static final String PREFIX = "[SERVICE-REPO]";

    /** Base SELECT clause used in all read queries */
    private static final String SELECT_BASE = """
            SELECT id, user_id, sale_id, date, subtotal
            FROM service_header
            """;

    @Override
    public ServiceHeader findById(Integer id) {
        String sql = SELECT_BASE + " WHERE id = ?";

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }

        } catch (Exception e) {
            logger.error("{} Error fetching serviceheader ID {}: {}", PREFIX, id, e.getMessage());
        }

        return null;
    }

    @Override
    public List<ServiceHeader> findAll() {
        List<ServiceHeader> list = new ArrayList<>();

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
     * Saves a serviceheader record using a shared connection.
     * Enables ServicesService to run multiple operations under a single transaction
     * (e.g. inserting serviceheader + service_items + movements).
     */
    @Override
    public void save(ServiceHeader s, Connection conn) throws SQLException {

        String sql = """
            INSERT INTO service_header (user_id, sale_id, date, subtotal)
            VALUES (?, ?, ?, ?)
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, s.getUserId());
            ps.setInt(2, s.getSaleId());
            ps.setString(3, s.getDate().toString());
            ps.setDouble(4, s.getSubtotal());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) s.setId(keys.getInt(1));
            }

            logger.info("{} ServiceHeader inserted successfully (ID={}) [shared]", PREFIX, s.getId());
        }
    }

    /**
     * Updates an existing serviceheader using a shared connection.
     */
    @Override
    public void update(ServiceHeader s, Connection conn) throws SQLException {

        String sql = """
            UPDATE service_header
            SET user_id = ?, sale_id = ?, date = ?, subtotal = ?
            WHERE id = ?
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, s.getUserId());
            ps.setInt(2, s.getSaleId());
            ps.setString(3, s.getDate().toString());
            ps.setDouble(4, s.getSubtotal());
            ps.setInt(5, s.getId());

            ps.executeUpdate();

            logger.info("{} ServiceHeader updated successfully (ID={}) [shared]", PREFIX, s.getId());
        }
    }

    /**
     * Deletes a serviceheader using a shared connection.
     */
    @Override
    public void delete(Integer id, Connection conn) throws SQLException {
        String sql = "DELETE FROM service_header WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();

            logger.info("{} ServiceHeader deleted successfully (ID={}) [shared]", PREFIX, id);
        }
    }

    @Override
    public void save(ServiceHeader s) {
        try (Connection conn = DbBootstrap.connect()) {
            save(s, conn);
        } catch (Exception e) {
            logger.error("{} Failed to save serviceheader: {}", PREFIX, e.getMessage());
        }
    }

    @Override
    public void update(ServiceHeader s) {
        try (Connection conn = DbBootstrap.connect()) {
            update(s, conn);
        } catch (Exception e) {
            logger.error("{} Failed to update serviceheader ID {}: {}", PREFIX, s.getId(), e.getMessage());
        }
    }

    @Override
    public void delete(Integer id) {
        try (Connection conn = DbBootstrap.connect()) {
            delete(id, conn);
        } catch (Exception e) {
            logger.error("{} Failed to delete serviceheader ID {}: {}", PREFIX, id, e.getMessage());
        }
    }

    /**
     * Calculates the sum of serviceheader totals for a user within a date range.
     */
    @Override
    public double sumServiceTotalsByUserAndDateRange(int barberId, LocalDate from, LocalDate to) {
        double total = 0.0;

        String sql = """
            SELECT SUM(subtotal)
            FROM service_header
            WHERE user_id = ? AND date BETWEEN ? AND ?
            """;

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, barberId);
            ps.setString(2, from.toString());
            ps.setString(3, to.toString());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) total = rs.getDouble(1);

            logger.info("{} Production for user {} = {}", PREFIX, barberId, total);

        } catch (SQLException e) {
            logger.error("{} Error getting production: {}", PREFIX, e.getMessage());
        }

        return total;
    }

    private ServiceHeader mapRow(ResultSet rs) throws SQLException {

        LocalDate date = LocalDate.parse(rs.getString("date"));

        return new ServiceHeader(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getInt("sale_id"),
                date,
                rs.getDouble("subtotal")
        );
    }
}
