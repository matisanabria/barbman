package app.barbman.core.repositories.paymentmethod;

import app.barbman.core.model.PaymentMethod;
import app.barbman.core.repositories.DbBootstrap;
import app.barbman.core.repositories.expense.ExpenseRepositoryImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentMethodRepositoryImpl implements PaymentMethodRepository{
    private static final Logger logger = LogManager.getLogger(PaymentMethodRepositoryImpl.class);
    private static final String PREFIX = "[PAYMENTMETHOD-REPO]";

    private static final String SELECT_BASE = """
        SELECT id, name
        FROM payment_methods
        """;

    @Override
    public List<PaymentMethod> findAll() {
        List<PaymentMethod> list = new ArrayList<>();
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(SELECT_BASE);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));
            logger.debug("{} Retrieved {} payment methods.", PREFIX, list.size());
            return list;

        } catch (Exception e) {
            logger.warn("{} Error fetching all payment methods: {}", PREFIX, e.getMessage());
            return List.of();
        }
    }

    @Override
    public PaymentMethod findById(Integer id) {
        String sql = SELECT_BASE + " WHERE id = ?";
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    logger.info("{} Payment method found with ID={}", PREFIX, id);
                    return mapRow(rs);
                }
            }

        } catch (Exception e) {
            logger.warn("{} Error fetching payment method by ID {}: {}", PREFIX, id, e.getMessage());
        }
        return null;
    }

    @Override
    public PaymentMethod findByName(String name) {
        String sql = SELECT_BASE + " WHERE name = ?";
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setString(1, name);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    logger.debug("{} Payment method found with name={}", PREFIX, name);
                    return mapRow(rs);
                }
            }

        } catch (Exception e) {
            logger.warn("{} Error fetching payment method by name '{}': {}", PREFIX, name, e.getMessage());
        }
        return null;
    }

    private PaymentMethod mapRow(ResultSet rs) throws SQLException {
        return new PaymentMethod(
                rs.getInt("id"),
                rs.getString("name")
        );
    }
}
