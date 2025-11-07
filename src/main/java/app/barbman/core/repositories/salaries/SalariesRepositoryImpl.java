package app.barbman.core.repositories.salaries;

import app.barbman.core.model.salaries.Salary;
import app.barbman.core.repositories.DbBootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SalariesRepositoryImpl implements SalariesRepository {
    private static final Logger logger = LogManager.getLogger(SalariesRepositoryImpl.class);
    public static final String PREFIX = "[SALARIES-REPO]";

    private static final String SELECT_BASE = """
        SELECT id, user_id, week_start, week_end,
               total_production, amount_paid, pay_type_snapshot,
               pay_date, payment_method_id, expense_id
        FROM salaries
        """;

    @Override
    public List<Salary> findAll() {
        List<Salary> list = new ArrayList<>();
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(SELECT_BASE);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));

            logger.debug("{} Retrieved {} salaries.", PREFIX, list.size());
            return list;

        } catch (Exception e) {
            logger.warn("{} Error listing salaries: {}", PREFIX, e.getMessage());
        }
        return List.of();
    }

    @Override
    public Salary findById(Integer id) {
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
            logger.warn("{} Error getting salary by id {}: {}", PREFIX, id, e.getMessage());
        }
        return null;
    }

    @Override
    public void save(Salary salary) {
        String sql = """
            INSERT INTO salaries (user_id, week_start, week_end,
               total_production, amount_paid, pay_type_snapshot,
               pay_date, payment_method_id, expense_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, salary.getUserId());
            ps.setString(2, salary.getWeekStartDate().toString());
            ps.setString(3, salary.getWeekEndDate().toString());
            ps.setDouble(4, salary.getTotalProduction());
            ps.setDouble(5, salary.getAmountPaid());
            ps.setInt(6, salary.getPayTypeSnapshot());
            ps.setString(7, salary.getPayDate() != null ? salary.getPayDate().toString() : null);
            ps.setInt(8, salary.getPaymentMethodId());
            ps.setObject(9, salary.getExpenseId() == 0 ? null : salary.getExpenseId());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    salary.setId(keys.getInt(1));
                }
            }

            logger.info("{} Salary saved successfully (ID={})", PREFIX, salary.getId());

        } catch (Exception e) {
            logger.warn("{} Error saving salary: {}", PREFIX, e.getMessage());
        }
    }

    @Override
    public void update(Salary salary) {
        String sql = """
            UPDATE salaries
            SET user_id = ?, week_start = ?, week_end = ?,
                total_production = ?, amount_paid = ?,
                pay_type_snapshot = ?, pay_date = ?, 
                payment_method_id = ?, expense_id = ?
            WHERE id = ?
            """;

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, salary.getUserId());
            ps.setString(2, salary.getWeekStartDate().toString());
            ps.setString(3, salary.getWeekEndDate().toString());
            ps.setDouble(4, salary.getTotalProduction());
            ps.setDouble(5, salary.getAmountPaid());
            ps.setInt(6, salary.getPayTypeSnapshot());
            ps.setString(7, salary.getPayDate() != null ? salary.getPayDate().toString() : null);
            ps.setInt(8, salary.getPaymentMethodId());
            ps.setObject(9, salary.getExpenseId() == 0 ? null : salary.getExpenseId());
            ps.setInt(10, salary.getId());

            ps.executeUpdate();

            logger.info("{} Salary updated successfully (ID={})", PREFIX, salary.getId());

        } catch (Exception e) {
            logger.warn("{} Error updating salary id {}: {}", PREFIX, salary.getId(), e.getMessage());
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM salaries WHERE id = ?";
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

            logger.info("{} Salary deleted (ID={})", PREFIX, id);

        } catch (Exception e) {
            logger.warn("{} Error deleting salary id {}: {}", PREFIX, id, e.getMessage());
        }
    }

    @Override
    public Salary findByBarberoAndFecha(int userId, LocalDate date) {
        String sql = """
            SELECT * FROM salaries
            WHERE user_id = ?
              AND week_start <= ?
              AND week_end >= ?
            LIMIT 1
        """;

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, date.toString());
            ps.setString(3, date.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }

        } catch (Exception e) {
            logger.warn("{} Error finding salary for user {} on date {}: {}", PREFIX, userId, date, e.getMessage());
        }

        return null;
    }

    private Salary mapRow(ResultSet rs) throws SQLException {
        Salary salary = new Salary();
        salary.setId(rs.getInt("id"));
        salary.setUserId(rs.getInt("user_id"));
        salary.setWeekStartDate(LocalDate.parse(rs.getString("week_start")));
        salary.setWeekEndDate(LocalDate.parse(rs.getString("week_end")));
        salary.setTotalProduction(rs.getDouble("total_production"));
        salary.setAmountPaid(rs.getDouble("amount_paid"));
        salary.setPayTypeSnapshot(rs.getInt("pay_type_snapshot"));
        salary.setPayDate(rs.getString("pay_date") != null ? LocalDate.parse(rs.getString("pay_date")) : null);
        salary.setPaymentMethodId(rs.getInt("payment_method_id"));
        salary.setExpenseId(rs.getInt("expense_id"));
        return salary;
    }
}
