package app.barbman.core.repositories.client;

import app.barbman.core.model.human.Client;
import app.barbman.core.repositories.DbBootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientRepositoryImpl implements ClientRepository {

    private static final Logger logger = LogManager.getLogger(ClientRepositoryImpl.class);
    private static final String PREFIX = "[CLIENT-REPO]";

    private static final String SELECT_BASE = """
        SELECT id, displayName, document, phone, email, notes, active
        FROM clients
        """;

    @Override
    public List<Client> findAll() {
        List<Client> list = new ArrayList<>();

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(SELECT_BASE);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));

            logger.debug("{} Retrieved {} clients.", PREFIX, list.size());
            return list;

        } catch (Exception e) {
            logger.error("{} Error fetching clients: {}", PREFIX, e.getMessage());
            return List.of();
        }
    }

    @Override
    public Client findById(Integer id) {
        String sql = SELECT_BASE + " WHERE id = ?";

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }

        } catch (Exception e) {
            logger.error("{} Error fetching client ID {}: {}", PREFIX, id, e.getMessage());
        }
        return null;
    }

    @Override
    public void save(Client client) {
        String sql = """
            INSERT INTO clients (displayName, document, phone, email, notes, active)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, client.getName());
            ps.setString(2, client.getDocument());
            ps.setString(3, client.getPhone());
            ps.setString(4, client.getEmail());
            ps.setString(5, client.getNotes());
            ps.setInt(6, client.isActive() ? 1 : 0);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) client.setId(keys.getInt(1));
            }

            logger.info("{} Client saved successfully (ID={})", PREFIX, client.getId());

        } catch (Exception e) {
            logger.error("{} Error saving client: {}", PREFIX, e.getMessage());
        }
    }

    @Override
    public void update(Client client) {
        String sql = """
            UPDATE clients
            SET displayName=?, document=?, phone=?, email=?, notes=?, active=?
            WHERE id=?
            """;

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setString(1, client.getName());
            ps.setString(2, client.getDocument());
            ps.setString(3, client.getPhone());
            ps.setString(4, client.getEmail());
            ps.setString(5, client.getNotes());
            ps.setInt(6, client.isActive() ? 1 : 0);
            ps.setInt(7, client.getId());

            ps.executeUpdate();
            logger.info("{} Client updated successfully (ID={})", PREFIX, client.getId());

        } catch (Exception e) {
            logger.error("{} Error updating client: {}", PREFIX, e.getMessage());
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM clients WHERE id = ?";

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

            logger.info("{} Client deleted (ID={})", PREFIX, id);

        } catch (Exception e) {
            logger.error("{} Error deleting client: {}", PREFIX, id, e.getMessage());
        }
    }

    private Client mapRow(ResultSet rs) throws SQLException {
        return new Client(
                rs.getInt("id"),
                rs.getString("displayName"),
                rs.getString("document"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("notes"),
                rs.getInt("active") == 1
        );
    }
}
