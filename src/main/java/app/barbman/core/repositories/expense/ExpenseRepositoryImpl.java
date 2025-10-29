package app.barbman.core.repositories.expense;

import app.barbman.core.model.Expense;
import app.barbman.core.repositories.DbBootstrap;
import app.barbman.core.repositories.expense.ExpenseRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ExpenseRepositoryImpl implements ExpenseRepository {

    private static final Logger logger = LogManager.getLogger(ExpenseRepositoryImpl.class);
    private static final String PREFIX = "[EXPENSE-REPO]";

    private static final String SELECT_BASE = """
        SELECT id, description, amount, date, type, payment_method_id
        FROM expenses
        """;

    @Override
    public List<Expense> findAll() {
        List<Expense> list = new ArrayList<>();
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(SELECT_BASE);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));
            logger.debug("{} Retrieved {} expenses.", PREFIX, list.size());
            return list;

        } catch (Exception e) {
            logger.warn("{} Error fetching all expenses: {}", PREFIX, e.getMessage());
            return List.of();
        }
    }

    @Override
    public Expense findById(Integer id) {
        String sql = SELECT_BASE + " WHERE id = ?";
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    logger.debug("{} Expense found with ID={}", PREFIX, id);
                    return mapRow(rs);
                }
            }
        } catch (Exception e) {
            logger.warn("{} Error fetching expense by ID {}: {}", PREFIX, id, e.getMessage());
        }
        return null;
    }

    @Override
    public void save(Expense expense) {
        String sql = """
            INSERT INTO expenses (description, amount, date, type, payment_method_id)
            VALUES (?, ?, ?, ?, ?)
            """;
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, expense.getDescription());
            ps.setDouble(2, expense.getAmount());
            ps.setString(3, expense.getDate().toString());
            ps.setString(4, expense.getType());
            ps.setInt(5, expense.getPaymentMethodId());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) expense.setId(keys.getInt(1));
            }

            logger.info("{} Expense saved successfully (ID={})", PREFIX, expense.getId());

        } catch (Exception e) {
            logger.error("{} Failed to save expense: {}", PREFIX, e.getMessage());
        }
    }

    @Override
    public void update(Expense expense) {
        String sql = """
            UPDATE expenses
            SET description=?, amount=?, date=?, type=?, payment_method_id=?
            WHERE id=?
            """;
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setString(1, expense.getDescription());
            ps.setDouble(2, expense.getAmount());
            ps.setString(3, expense.getDate().toString());
            ps.setString(4, expense.getType());
            ps.setInt(5, expense.getPaymentMethodId());
            ps.setInt(6, expense.getId());

            ps.executeUpdate();
            logger.info("{} Expense updated successfully (ID={})", PREFIX, expense.getId());

        } catch (Exception e) {
            logger.error("{} Error updating expense ID {}: {}", PREFIX, expense.getId(), e.getMessage());
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM expenses WHERE id = ?";
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

            logger.info("{} Expense deleted successfully (ID={})", PREFIX, id);

        } catch (Exception e) {
            logger.error("{} Error deleting expense ID {}: {}", PREFIX, id, e.getMessage());
        }
    }

    @Override
    public double getTotalAdelantos(int barberoId, LocalDate desde, LocalDate hasta) {
        double total = 0.0;
        String sql = """
            SELECT SUM(amount)
            FROM expenses
            WHERE type = 'advance'
              AND description LIKE ?
              AND date BETWEEN ? AND ?
            """;

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setString(1, "%barber ID " + barberoId + "%");
            ps.setString(2, desde.toString());
            ps.setString(3, hasta.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) total = rs.getDouble(1);
            }

            logger.info("{} Total advances for barber ID={} between {} and {}: {} Gs",
                    PREFIX, barberoId, desde, hasta, total);

        } catch (Exception e) {
            logger.warn("{} Error calculating total advances for barber {}: {}", PREFIX, barberoId, e.getMessage());
        }

        return total;
    }

    public List<Expense> searchByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = SELECT_BASE + " WHERE date BETWEEN ? AND ? ORDER BY date, id";
        List<Expense> list = new ArrayList<>();

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setString(1, startDate.toString());
            ps.setString(2, endDate.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }

            logger.debug("{} Retrieved {} expenses between {} and {}", PREFIX, list.size(), startDate, endDate);

        } catch (Exception e) {
            logger.warn("{} Error searching expenses between {} and {}: {}", PREFIX, startDate, endDate, e.getMessage());
        }

        return list;
    }

    private Expense mapRow(ResultSet rs) throws SQLException {
        return new Expense(
                rs.getInt("id"),
                rs.getString("description"),
                rs.getDouble("amount"),
                LocalDate.parse(rs.getString("date")),
                rs.getString("type"),
                rs.getInt("payment_method_id")
        );
    }
}
