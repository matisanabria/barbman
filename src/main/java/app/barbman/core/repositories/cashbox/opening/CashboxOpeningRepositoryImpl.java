package app.barbman.core.repositories.cashbox.opening;

import app.barbman.core.model.cashbox.CashboxOpening;
import app.barbman.core.repositories.DbBootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CashboxOpeningRepositoryImpl implements CashboxOpeningRepository {

    private static final Logger logger = LogManager.getLogger(CashboxOpeningRepositoryImpl.class);
    private static final String PREFIX = "[CASHBOX-OPENING-REPO]";

    private static final String SELECT_BASE = """
        SELECT id, period_start_date, opened_at, opened_by_user_id,
               cash_amount, bank_amount, notes
        FROM cashbox_openings
        """;

    @Override
    public CashboxOpening findById(Integer id) {
        String sql = SELECT_BASE + " WHERE id = ?";
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }

        } catch (Exception e) {
            logger.warn("{} Error fetching opening by ID {}: {}", PREFIX, id, e.getMessage());
        }
        return null;
    }

    @Override
    public CashboxOpening findByPeriodStart(LocalDate periodStartDate) {
        String sql = SELECT_BASE + " WHERE period_start_date = ?";
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setString(1, periodStartDate.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }

        } catch (Exception e) {
            logger.warn("{} Error fetching opening for period {}: {}", PREFIX, periodStartDate, e.getMessage());
        }
        return null;
    }

    @Override
    public boolean existsForPeriod(LocalDate periodStartDate) {
        String sql = "SELECT 1 FROM cashbox_openings WHERE period_start_date = ?";
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setString(1, periodStartDate.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {
            logger.warn("{} Error checking opening existence for period {}: {}", PREFIX, periodStartDate, e.getMessage());
            return false;
        }
    }

    @Override
    public List<CashboxOpening> findAll() {
        List<CashboxOpening> list = new ArrayList<>();
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(SELECT_BASE);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));
            logger.debug("{} Retrieved {} openings.", PREFIX, list.size());

        } catch (Exception e) {
            logger.warn("{} Error fetching all openings: {}", PREFIX, e.getMessage());
        }
        return list;
    }

    @Override
    public void save(CashboxOpening opening) {
        String sql = """
            INSERT INTO cashbox_openings
            (period_start_date, opened_at, opened_by_user_id, cash_amount, bank_amount, notes)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, opening.getPeriodStartDate().toString());
            ps.setString(2, opening.getOpenedAt().toString());
            ps.setInt(3, opening.getOpenedByUserId());
            ps.setDouble(4, opening.getCashAmount());
            ps.setDouble(5, opening.getBankAmount());
            ps.setString(6, opening.getNotes());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) opening.setId(keys.getInt(1));
            }

            logger.info("{} Opening saved successfully (ID={})", PREFIX, opening.getId());

        } catch (Exception e) {
            logger.error("{} Failed to save opening: {}", PREFIX, e.getMessage());
        }
    }

    @Override
    public void update(CashboxOpening opening) {
        throw new UnsupportedOperationException("Cashbox openings must not be updated.");
    }

    @Override
    public void delete(Integer id) {
        throw new UnsupportedOperationException("Cashbox openings must not be deleted.");
    }

    private CashboxOpening mapRow(ResultSet rs) throws SQLException {
        return new CashboxOpening(
                rs.getInt("id"),
                LocalDate.parse(rs.getString("period_start_date")),
                LocalDateTime.parse(rs.getString("opened_at")),
                rs.getInt("opened_by_user_id"),
                rs.getDouble("cash_amount"),
                rs.getDouble("bank_amount"),
                rs.getString("notes")
        );
    }
}
