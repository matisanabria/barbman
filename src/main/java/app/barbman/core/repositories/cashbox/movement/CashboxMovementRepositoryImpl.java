package app.barbman.core.repositories.cashbox.movement;

import app.barbman.core.model.cashbox.CashboxMovement;
import app.barbman.core.repositories.DbBootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CashboxMovementRepositoryImpl implements CashboxMovementRepository {

    private static final Logger logger = LogManager.getLogger(CashboxMovementRepositoryImpl.class);
    private static final String PREFIX = "[CASHBOX-MOVEMENT-REPO]";

    private static final String SELECT_BASE = """
        SELECT *
        FROM cashbox_movements
        """;

    @Override
    public CashboxMovement findById(Integer id) {
        String sql = SELECT_BASE + " WHERE id = ?";
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }

        } catch (Exception e) {
            logger.warn("{} Error fetching movement by ID {}: {}", PREFIX, id, e.getMessage());
        }
        return null;
    }

    @Override
    public List<CashboxMovement> findAll() {
        List<CashboxMovement> list = new ArrayList<>();
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(SELECT_BASE);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));
            logger.debug("{} Retrieved {} movements.", PREFIX, list.size());

        } catch (Exception e) {
            logger.warn("{} Error fetching all movements: {}", PREFIX, e.getMessage());
        }
        return list;
    }

    @Override
    public void save(CashboxMovement movement) {
        String sql = """
            INSERT INTO cashbox_movements
            (movement_type, direction, amount, payment_method_id,
             reference_type, reference_id, description, user_id, occurred_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, movement.getMovementType());
            ps.setString(2, movement.getDirection());
            ps.setDouble(3, movement.getAmount());
            ps.setObject(4, movement.getPaymentMethodId());
            ps.setString(5, movement.getReferenceType());
            ps.setObject(6, movement.getReferenceId());
            ps.setString(7, movement.getDescription());
            ps.setObject(8, movement.getUserId());
            ps.setString(9, movement.getOccurredAt().toString());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) movement.setId(keys.getInt(1));
            }

            logger.info("{} Movement saved successfully (ID={})", PREFIX, movement.getId());

        } catch (Exception e) {
            logger.error("{} Failed to save movement: {}", PREFIX, e.getMessage());
        }
    }

    @Override
    public void update(CashboxMovement movement) {
        throw new UnsupportedOperationException("Cashbox movements must not be updated.");
    }

    @Override
    public void delete(Integer id) {
        throw new UnsupportedOperationException("Cashbox movements must not be deleted.");
    }

    @Override
    public List<CashboxMovement> findByDateRange(LocalDateTime start, LocalDateTime end) {
        String sql = SELECT_BASE + " WHERE occurred_at BETWEEN ? AND ? ORDER BY occurred_at";
        List<CashboxMovement> list = new ArrayList<>();

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setString(1, start.toString());
            ps.setString(2, end.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }

            logger.debug("{} Retrieved {} movements between {} and {}", PREFIX, list.size(), start, end);

        } catch (Exception e) {
            logger.warn("{} Error fetching movements by date range: {}", PREFIX, e.getMessage());
        }
        return list;
    }

    @Override
    public List<CashboxMovement> findByReference(String referenceType, Integer referenceId) {
        String sql = SELECT_BASE + " WHERE reference_type = ? AND reference_id = ?";
        List<CashboxMovement> list = new ArrayList<>();

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setString(1, referenceType);
            ps.setInt(2, referenceId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }

        } catch (Exception e) {
            logger.warn("{} Error fetching movements by reference {} {}: {}",
                    PREFIX, referenceType, referenceId, e.getMessage());
        }
        return list;
    }

    private CashboxMovement mapRow(ResultSet rs) throws SQLException {
        return new CashboxMovement(
                rs.getInt("id"),
                rs.getString("movement_type"),
                rs.getString("direction"),
                rs.getDouble("amount"),
                (Integer) rs.getObject("payment_method_id"),
                rs.getString("reference_type"),
                (Integer) rs.getObject("reference_id"),
                rs.getString("description"),
                (Integer) rs.getObject("user_id"),
                LocalDateTime.parse(rs.getString("occurred_at")),
                LocalDateTime.parse(rs.getString("created_at"))
        );
    }
}
