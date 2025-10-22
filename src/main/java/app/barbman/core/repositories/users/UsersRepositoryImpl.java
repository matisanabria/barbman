package app.barbman.core.repositories.users;

import app.barbman.core.model.User;
import app.barbman.core.repositories.DbBootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación de UsersRepository para gestionar operaciones CRUD en la tabla 'users'.
 */
public class UsersRepositoryImpl implements UsersRepository {
    List<User> listaUsers = new ArrayList<>();
    private static final Logger logger = LogManager.getLogger(UsersRepositoryImpl.class);

    private static final String SELECT_BASE = """
        SELECT id, name, role, pin, payment_type, param_1, param_2
        FROM users
        """;
    public static final String PREFIX = "[USERS-REPO]";

    /**
     * Searches for a user by its ID.
     * @param id The ID of the user to search for.
     * @return The found user, or null if not found.
     */
    @Override
    public User findById(Integer id) {
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
            logger.warn("{} Can't find user by id {}: {}", PREFIX, id, e.getMessage());
        }
        return null;
    }

    /**
     * Put all users from the database into a list.
     * @return User list.
     */
    @Override
    public List<User> findAll() {
        List<User> lista = new ArrayList<>();
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(SELECT_BASE);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapRow(rs));
            }
            return lista;
        } catch (Exception e) {
            logger.warn("{} Error creating user list: {}", PREFIX, e.getMessage());
        }
        return List.of();
    }

    /**
     * Saves a new user to the database.
     * @param user The user to save.
     */
    @Override
    public void save(User user) {
        String sql = """
            INSERT INTO users (name, role, pin, payment_type, param_1, param_2)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getRole());
            ps.setString(3, user.getPin());
            ps.setInt(4, user.getPaymentType());
            ps.setDouble(5, user.getParam1());
            ps.setDouble(6, user.getParam2());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getInt(1));
                }
            }
        } catch (Exception e) {
            logger.warn("{} Error saving new user {}: {}", PREFIX, user.getName(), e.getMessage());
        }
    }

    @Override
    public void update(User user){
        String sql = """
            UPDATE users
            SET name = ?, role = ?, pin = ?, payment_type = ?, param_1 = ?, param_2 = ?
            WHERE id = ?
            """;
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getRole());
            ps.setString(3, user.getPin());
            ps.setInt(4, user.getPaymentType());
            ps.setDouble(5, user.getParam1());
            ps.setDouble(6, user.getParam2());
            ps.setInt(7, user.getId());

            ps.executeUpdate();
        } catch (Exception e) {
            logger.warn("{} Error while updating user id {}: {}", PREFIX, user.getId(), e.getMessage());
        }

    }

    /**
     * Borra un barbero de la base de datos.
     *
     * @param id El barbero a borrar.
     */
    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            logger.warn("{} Error while deleting user id {}: {}", PREFIX, id, e.getMessage());
        }
    }

    /**
     * Busca un barbero por su PIN.
     *
     * @param pin El PIN del barbero a buscar.
     * @return El barbero encontrado o null si no existe.
     */
    @Override
    public User findByPin(String pin) {
        String sql = SELECT_BASE + " WHERE pin = ?";
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setString(1, pin);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (Exception e) {
            logger.warn("{} Can't find user by pin: {}", PREFIX, e.getMessage());
        }
        return null;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("role"),
                rs.getString("pin"),
                rs.getInt("payment_type"),
                rs.getDouble("param_1"),
                rs.getDouble("param_2")
        );
    }

}
