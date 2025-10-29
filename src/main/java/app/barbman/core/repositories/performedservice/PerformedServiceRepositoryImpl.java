package app.barbman.core.repositories.performedservice;

import app.barbman.core.model.PerformedService;
import app.barbman.core.repositories.DbBootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PerformedServiceRepositoryImpl implements PerformedServiceRepository {

    private static final Logger logger = LogManager.getLogger(PerformedServiceRepositoryImpl.class);
    private static final String PREFIX = "[PERFORMEDSERV-REPO]";

    // Base SELECT clause (used across multiple queries)
    private static final String SELECT_BASE = """
        SELECT id, user_id, service_type_id, price, date, payment_method_id, notes
        FROM servicios_realizados
        """;

    @Override
    public PerformedService findById(Integer id) {
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
            logger.warn("{} Error fetching performed service by id {}: {}", PREFIX, id, e.getMessage());
        }
        return null;
    }

    @Override
    public List<PerformedService> findAll() {
        List<PerformedService> list = new ArrayList<>();
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(SELECT_BASE);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            logger.warn("{} Error fetching all performed services: {}", PREFIX, e.getMessage());
        }
        return list;
    }

    @Override
    public void save(PerformedService s) {
        String sql = """
            INSERT INTO servicios_realizados
                (user_id, service_type_id, price, date, payment_method_id, notes)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, s.getUserId());
            ps.setInt(2, s.getServiceTypeId());
            ps.setDouble(3, s.getPrice());
            ps.setString(4, s.getDate().toString()); // LocalDate -> TEXT
            ps.setInt(5, s.getPaymentMethodId());

            if (s.getNotes() != null) {
                ps.setString(6, s.getNotes());
            } else {
                ps.setNull(6, Types.VARCHAR);
            }

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    s.setId(keys.getInt(1));
                }
            }

            logger.info("{} Performed service successfully saved (ID={})", PREFIX, s.getId());

        } catch (Exception e) {
            logger.error("{} Failed to save performed service: {}", PREFIX, e.getMessage());
        }
    }

    @Override
    public void update(PerformedService s) {
        String sql = """
            UPDATE servicios_realizados
            SET user_id=?, service_type_id=?, price=?, date=?, payment_method_id=?, notes=?
            WHERE id=?
            """;
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, s.getUserId());
            ps.setInt(2, s.getServiceTypeId());
            ps.setDouble(3, s.getPrice());
            ps.setString(4, s.getDate().toString());
            ps.setInt(5, s.getPaymentMethodId());
            ps.setString(6, s.getNotes());
            ps.setInt(7, s.getId());

            ps.executeUpdate();
            logger.info("{} Performed service updated (ID={})", PREFIX, s.getId());

        } catch (Exception e) {
            logger.error("{} Error updating performed service (ID={}): {}", PREFIX, s.getId(), e.getMessage());
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM servicios_realizados WHERE id = ?";
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

            logger.info("{} Performed service deleted (ID={})", PREFIX, id);
        } catch (Exception e) {
            logger.error("{} Error deleting performed service (ID={}): {}", PREFIX, id, e.getMessage());
        }
    }

    @Override
    public double getProduccionSemanalPorBarbero(int barberoId, LocalDate desde, LocalDate hasta) {
        double total = 0.0;
        String sql = """
            SELECT SUM(price)
            FROM servicios_realizados
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

    private PerformedService mapRow(ResultSet rs) throws SQLException {
        LocalDate date = LocalDate.parse(rs.getString("date"));
        return new PerformedService(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getInt("service_type_id"),
                rs.getDouble("price"),
                date,
                rs.getInt("payment_method_id"),
                rs.getString("notes")
        );
    }
}
