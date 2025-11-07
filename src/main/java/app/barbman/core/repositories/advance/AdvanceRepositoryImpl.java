package app.barbman.core.repositories.advance;

import app.barbman.core.model.salaries.Advance;
import app.barbman.core.repositories.DbBootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AdvanceRepositoryImpl implements AdvanceRepository {

    private static final Logger logger = LogManager.getLogger(AdvanceRepositoryImpl.class);
    private static final String PREFIX = "[ADV-REPO]";
    private static final String SELECT_BASE = "SELECT * FROM advances";

    @Override
    public Advance findById(Integer id) {
        String sql = SELECT_BASE + " WHERE id = ?";
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (Exception e) {
            logger.warn("{} Error fetching advance ID {}: {}", PREFIX, id, e.getMessage());
        }
        return null;
    }

    @Override
    public List<Advance> findAll() {
        List<Advance> list = new ArrayList<>();
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(SELECT_BASE + " ORDER BY date DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
            logger.debug("{} Retrieved {} advances", PREFIX, list.size());
        } catch (Exception e) {
            logger.warn("{} Error fetching all advances: {}", PREFIX, e.getMessage());
        }
        return list;
    }

    @Override
    public void save(Advance advance) {
        String sql = """
            INSERT INTO advances (user_id, amount, date, payment_method_id, expense_id)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, advance.getUserId());
            ps.setDouble(2, advance.getAmount());
            ps.setString(3, advance.getDate().toString());
            ps.setInt(4, advance.getPaymentMethodId());
            ps.setInt(5, advance.getExpenseId());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) advance.setId(rs.getInt(1));
            }
            logger.info("{} Advance saved successfully (ID={})", PREFIX, advance.getId());
        } catch (Exception e) {
            logger.error("{} Error saving advance: {}", PREFIX, e.getMessage());
        }
    }

    @Override
    public void update(Advance advance) {
        String sql = """
            UPDATE advances SET user_id=?, amount=?, date=?, payment_method_id=?, expense_id=? WHERE id=?
        """;
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setInt(1, advance.getUserId());
            ps.setDouble(2, advance.getAmount());
            ps.setString(3, advance.getDate().toString());
            ps.setInt(4, advance.getPaymentMethodId());
            ps.setInt(5, advance.getExpenseId());
            ps.setInt(6, advance.getId());
            ps.executeUpdate();
            logger.info("{} Advance updated (ID={})", PREFIX, advance.getId());
        } catch (Exception e) {
            logger.error("{} Error updating advance ID {}: {}", PREFIX, advance.getId(), e.getMessage());
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM advances WHERE id=?";
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            logger.info("{} Advance deleted (ID={})", PREFIX, id);
        } catch (Exception e) {
            logger.error("{} Error deleting advance ID {}: {}", PREFIX, id, e.getMessage());
        }
    }

    @Override
    public List<Advance> findByUserAndDateRange(int userId, LocalDate from, LocalDate to) {
        String sql = SELECT_BASE + " WHERE user_id=? AND date BETWEEN ? AND ? ORDER BY date";
        List<Advance> list = new ArrayList<>();
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, from.toString());
            ps.setString(3, to.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
            logger.debug("{} Found {} advances for user {} between {} and {}", PREFIX, list.size(), userId, from, to);
        } catch (Exception e) {
            logger.warn("{} Error filtering advances: {}", PREFIX, e.getMessage());
        }
        return list;
    }

    @Override
    public double getTotalByUserAndDateRange(int userId, LocalDate from, LocalDate to) {
        double total = 0.0;
        String sql = "SELECT SUM(amount) FROM advances WHERE user_id=? AND date BETWEEN ? AND ?";
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, from.toString());
            ps.setString(3, to.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) total = rs.getDouble(1);
            }
            logger.info("{} Total advances for user {} between {} and {} = {}", PREFIX, userId, from, to, total);
        } catch (Exception e) {
            logger.warn("{} Error calculating total advances: {}", PREFIX, e.getMessage());
        }
        return total;
    }

    private Advance mapRow(ResultSet rs) throws SQLException {
        return new Advance(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getDouble("amount"),
                LocalDate.parse(rs.getString("date")),
                rs.getInt("payment_method_id"),
                rs.getInt("expense_id")
        );
    }
}
