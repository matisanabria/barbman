package app.barbman.core.repositories.cashbox.closure;

import app.barbman.core.model.cashbox.CashboxClosure;
import app.barbman.core.repositories.DbBootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CashboxClosureRepositoryImpl implements CashboxClosureRepository {

    private static final Logger logger = LogManager.getLogger(CashboxClosureRepositoryImpl.class);
    private static final String PREFIX = "[CASHBOX-CLOSURE-REPO]";

    private static final String SELECT_BASE = """
        SELECT *
        FROM cashbox_closures
        """;

    @Override
    public CashboxClosure findById(Integer id) {
        String sql = SELECT_BASE + " WHERE id = ?";
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }

        } catch (Exception e) {
            logger.warn("{} Error fetching closure by ID {}: {}", PREFIX, id, e.getMessage());
        }
        return null;
    }

    @Override
    public CashboxClosure findByPeriodStart(LocalDate periodStartDate) {
        String sql = SELECT_BASE + " WHERE period_start_date = ?";
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setString(1, periodStartDate.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }

        } catch (Exception e) {
            logger.warn("{} Error fetching closure for period {}: {}", PREFIX, periodStartDate, e.getMessage());
        }
        return null;
    }

    @Override
    public boolean existsForPeriod(LocalDate periodStartDate) {
        String sql = "SELECT 1 FROM cashbox_closures WHERE period_start_date = ?";
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setString(1, periodStartDate.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {
            logger.warn("{} Error checking closure existence for period {}: {}", PREFIX, periodStartDate, e.getMessage());
            return false;
        }
    }

    @Override
    public List<CashboxClosure> findAll() {
        List<CashboxClosure> list = new ArrayList<>();
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(SELECT_BASE);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }

            logger.debug("{} Retrieved {} closures.", PREFIX, list.size());

        } catch (Exception e) {
            logger.warn("{} Error fetching all closures: {}", PREFIX, e.getMessage());
        }
        return list;
    }

    @Override
    public void save(CashboxClosure closure) {
        String sql = """
            INSERT INTO cashbox_closures
            (period_start_date,
             period_end_date,
             closed_at,
             closed_by_user_id,
             expected_cash,
             expected_bank,
             expected_total,
             notes)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, closure.getPeriodStartDate().toString());
            ps.setString(2, closure.getPeriodEndDate().toString());
            ps.setString(3, closure.getClosedAt().toString());
            ps.setInt(4, closure.getClosedByUserId());
            ps.setDouble(5, closure.getExpectedCash());
            ps.setDouble(6, closure.getExpectedBank());
            ps.setDouble(7, closure.getExpectedTotal());
            ps.setString(8, closure.getNotes());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    closure.setId(keys.getInt(1));
                }
            }

            logger.info("{} Closure saved successfully (ID={})", PREFIX, closure.getId());

        } catch (Exception e) {
            logger.error("{} Failed to save closure: {}", PREFIX, e.getMessage(), e);
        }
    }

    @Override
    public void update(CashboxClosure closure) {
        throw new UnsupportedOperationException("Cashbox closures must not be updated.");
    }

    @Override
    public void delete(Integer id) {
        throw new UnsupportedOperationException("Cashbox closures must not be deleted.");
    }

    private CashboxClosure mapRow(ResultSet rs) throws SQLException {
        return new CashboxClosure(
                rs.getInt("id"),
                LocalDate.parse(rs.getString("period_start_date")),
                LocalDate.parse(rs.getString("period_end_date")),
                LocalDateTime.parse(rs.getString("closed_at")),
                rs.getInt("closed_by_user_id"),
                rs.getDouble("expected_cash"),
                rs.getDouble("expected_bank"),
                rs.getDouble("expected_total"),
                rs.getString("notes")
        );
    }
}
