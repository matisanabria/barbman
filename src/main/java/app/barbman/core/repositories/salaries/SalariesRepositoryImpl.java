package app.barbman.core.repositories.salaries;

import app.barbman.core.model.Salary;
import app.barbman.core.repositories.DbBootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SalariesRepositoryImpl implements SalariesRepository {
    private static final Logger logger = LogManager.getLogger(SalariesRepositoryImpl.class);

    private static final String SELECT_BASE = """
        SELECT id, user_id, week_start, week_end,
               total_production, amount_paid, pay_type_snapshot,
               pay_date, payment_method_id
        FROM salaries
        """;

    public static final String PREFIX = "[SALARIES-REPO]";

    @Override
    public List<Salary> findAll() {
        List<Salary> list = new ArrayList<>();
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(SELECT_BASE);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
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
            INSERT INTO sueldos (user_id, week_start, week_end,
               total_production, amount_paid, pay_type_snapshot,
               pay_date, payment_method_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, salary.getUserId());
            ps.setString(2, salary.getWeekStartDate().toString());
            ps.setString(3, salary.getWeekEndDate().toString());
            ps.setDouble(4, salary.getTotalProduction());
            ps.setDouble(5, salary.getAmountPaid());
            ps.setInt(6, salary.getPayTypeSnapshot());
            ps.setString(7, salary.getPayDate().toString());
            ps.setInt(8, salary.getPaymentMethodId());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    salary.setId(keys.getInt(1));
                }
            }
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
                pay_type_snapshot = ?, pay_date = ?, payment_method_id = ?
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
            ps.setString(7, salary.getPayDate().toString());
            ps.setInt(8, salary.getPaymentMethodId());
            ps.setInt(9, salary.getId());

            ps.executeUpdate();
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

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
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
        salary.setPayDate(LocalDate.parse(rs.getString("pay_date")));
        salary.setPaymentMethodId(rs.getInt("payment_method_id"));
        return salary;
    }
}
