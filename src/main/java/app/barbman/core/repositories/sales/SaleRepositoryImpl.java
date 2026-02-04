package app.barbman.core.repositories.sales;

import app.barbman.core.dto.history.SaleDetailDTO;
import app.barbman.core.dto.history.SaleHistoryDTO;
import app.barbman.core.model.sales.Sale;
import app.barbman.core.repositories.DbBootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository implementation for the `sales` table.
 * Handles sales header persistence and shared-connection transactions.
 */
public class SaleRepositoryImpl implements SaleRepository {

    private static final Logger logger = LogManager.getLogger(SaleRepositoryImpl.class);
    private static final String PREFIX = "[SALES-REPO]";

    private static final String SELECT_BASE = """
        SELECT id, user_id, client_id, payment_method_id, date, total
        FROM sales
        """;

    @Override
    public Sale findById(Integer id) {
        String sql = SELECT_BASE + " WHERE id = ?";

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }

        } catch (Exception e) {
            logger.error("{} Failed to fetch Sale ID {}: {}", PREFIX, id, e.getMessage());
        }

        return null;
    }

    @Override
    public List<Sale> findAll() {
        List<Sale> list = new ArrayList<>();

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(SELECT_BASE);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (Exception e) {
            logger.error("{} Failed to load sales: {}", PREFIX, e.getMessage());
        }

        return list;
    }

    @Override
    public void save(Sale sale) {
        try (Connection conn = DbBootstrap.connect()) {
            save(sale, conn);
        } catch (Exception e) {
            logger.error("{} Failed to save salecart: {}", PREFIX, e.getMessage());
        }
    }

    @Override
    public void save(Sale sale, Connection conn) throws SQLException {

        String sql = """
            INSERT INTO sales (user_id, client_id, payment_method_id, date, total)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, sale.getUserId());
            ps.setObject(2, sale.getClientId());
            ps.setInt(3, sale.getPaymentMethodId());
            ps.setString(4, sale.getDate().toString());
            ps.setDouble(5, sale.getTotal());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) sale.setId(keys.getInt(1));
            }

            logger.info("{} Sale inserted successfully (ID={}) [shared]",
                    PREFIX, sale.getId());
        }
    }

    @Override
    public void update(Sale sale) {
        try (Connection conn = DbBootstrap.connect()) {
            update(sale, conn);
        } catch (Exception e) {
            logger.error("{} Failed to update salecart ID {}: {}",
                    PREFIX, sale.getId(), e.getMessage());
        }
    }

    @Override
    public void update(Sale sale, Connection conn) throws SQLException {

        String sql = """
            UPDATE sales
            SET user_id = ?, client_id = ?, payment_method_id = ?, date = ?, total = ?
            WHERE id = ?
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sale.getUserId());
            ps.setObject(2, sale.getClientId());
            ps.setInt(3, sale.getPaymentMethodId());
            ps.setString(4, sale.getDate().toString());
            ps.setDouble(5, sale.getTotal());
            ps.setInt(6, sale.getId());

            ps.executeUpdate();

            logger.info("{} Sale updated successfully (ID={}) [shared]",
                    PREFIX, sale.getId());
        }
    }

    @Override
    public void delete(Integer id) {
        try (Connection conn = DbBootstrap.connect()) {
            delete(id, conn);
        } catch (Exception e) {
            logger.error("{} Failed to delete salecart ID {}: {}", PREFIX, id, e.getMessage());
        }
    }

    @Override
    public void delete(Integer id, Connection conn) throws SQLException {

        String sql = "DELETE FROM sales WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();

            logger.info("{} Sale deleted (ID={}) [shared]", PREFIX, id);
        }
    }

    private Sale mapRow(ResultSet rs) throws SQLException {

        LocalDate date = LocalDate.parse(rs.getString("date"));

        Integer clientId = rs.getObject("client_id") != null
                ? rs.getInt("client_id")
                : null;

        return new Sale(
                rs.getInt("id"),
                rs.getInt("user_id"),
                clientId,
                rs.getInt("payment_method_id"),
                date,
                rs.getDouble("total")
        );
    }

    // ====
    // Methods for sales history
    // ====

    @Override
    public List<SaleHistoryDTO> findSalesHistory(LocalDate from, LocalDate to) {

        String sql = """
        SELECT
            s.id            AS sale_id,
            s.date          AS date,
            u.displayName   AS user_name,
            c.displayName   AS client_name,
            s.total         AS total,
            pm.displayName  AS payment_method
        FROM sales s
        JOIN users u ON u.id = s.user_id
        LEFT JOIN clients c ON c.id = s.client_id
        JOIN payment_methods pm ON pm.id = s.payment_method_id
        WHERE s.date BETWEEN ? AND ?
        ORDER BY s.date DESC, s.id DESC
    """;

        List<SaleHistoryDTO> list = new ArrayList<>();

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setString(1, from.toString());
            ps.setString(2, to.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SaleHistoryDTO dto = new SaleHistoryDTO();
                    dto.setSaleId(rs.getInt("sale_id"));
                    dto.setDate(LocalDate.parse(rs.getString("date")));
                    dto.setUserName(rs.getString("user_name"));
                    dto.setClientName(rs.getString("client_name"));
                    dto.setTotal(rs.getDouble("total"));
                    dto.setPaymentMethod(rs.getString("payment_method"));
                    dto.setPaid(true); // venta = siempre pagada

                    list.add(dto);
                }
            }

        } catch (Exception e) {
            logger.error("{} Error loading sales history: {}", PREFIX, e.getMessage());
        }

        return list;
    }

    @Override
    public double sumTotalByPaymentMethodAndPeriod(
            int paymentMethodId,
            LocalDate start,
            LocalDate end
    ) {

        String sql = """
        SELECT COALESCE(SUM(total), 0)
        FROM sales
        WHERE payment_method_id = ?
          AND date BETWEEN ? AND ?
        """;

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, paymentMethodId);
            ps.setString(2, start.toString());
            ps.setString(3, end.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double total = rs.getDouble(1);
                    logger.debug(
                            "{} SUM sales [pm={}, {} -> {}] = {}",
                            PREFIX, paymentMethodId, start, end, total
                    );
                    return total;
                }
            }

        } catch (Exception e) {
            logger.error(
                    "{} Error summing sales for pm={} between {} and {}: {}",
                    PREFIX, paymentMethodId, start, end, e.getMessage()
            );
        }

        return 0;
    }


    @Override
    public SaleDetailDTO findSaleHeaderDetail(int saleId) {

        String sql = """
        SELECT
            s.id            AS sale_id,
            s.date          AS date,
            u.displayName   AS user_name,
            c.displayName   AS client_name,
            pm.displayName  AS payment_method,
            s.total         AS total
        FROM sales s
        JOIN users u ON u.id = s.user_id
        LEFT JOIN clients c ON c.id = s.client_id
        JOIN payment_methods pm ON pm.id = s.payment_method_id
        WHERE s.id = ?
    """;

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, saleId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    SaleDetailDTO dto = new SaleDetailDTO();
                    dto.setSaleId(rs.getInt("sale_id"));
                    dto.setDate(LocalDate.parse(rs.getString("date")));
                    dto.setUserName(rs.getString("user_name"));
                    dto.setClientName(rs.getString("client_name"));
                    dto.setPaymentMethod(rs.getString("payment_method"));
                    dto.setTotal(rs.getDouble("total"));
                    return dto;
                }
            }

        } catch (Exception e) {
            logger.error("{} Error loading sale detail {}: {}", PREFIX, saleId, e.getMessage());
        }

        return null;
    }

    @Override
    public double sumTotalByPeriod(LocalDate start, LocalDate end) {

        String sql = """
    SELECT COALESCE(SUM(total), 0)
    FROM sales
    WHERE date BETWEEN ? AND ?
    """;

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setString(1, start.toString());
            ps.setString(2, end.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double total = rs.getDouble(1);
                    logger.debug(
                            "{} SUM sales [{} -> {}] = {}",
                            PREFIX, start, end, total
                    );
                    return total;
                }
            }

        } catch (Exception e) {
            logger.error(
                    "{} Error summing sales between {} and {}: {}",
                    PREFIX, start, end, e.getMessage()
            );
        }

        return 0;
    }
}
