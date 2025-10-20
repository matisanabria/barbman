package app.barbman.core.repositories.barbero;

import app.barbman.core.model.User;
import app.barbman.core.repositories.DbBootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación de BarberoRepository para gestionar operaciones CRUD en la tabla 'users'.
 */
public class BarberoRepositoryImpl implements BarberoRepository {
    List<User> listaUsers = new ArrayList<>();
    private static final Logger logger = LogManager.getLogger(BarberoRepositoryImpl.class);

    private static final String SELECT_BASE = """
        SELECT id, name, role, pin, tipo_cobro, param_1, param_2
        FROM users
        """;

    /**
     * Busca un barbero por su ID.
     *
     * @param id El ID del barbero a buscar.
     * @return El barbero encontrado o null si no existe.
     */
    @Override
    public User findById(int id) {
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
            logger.warn("Error al obtener barbero por id {}: {}", id, e.getMessage());
        }
        return null;
    }

    /**
     * Devuelve una lista de todos los users en la base de datos.
     *
     * @return Lista de users.
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
            logger.warn("Error al listar users: {}", e.getMessage());
        }
        return List.of();
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
            logger.warn("Error al obtener barbero por pin {}: {}", pin, e.getMessage());
        }
        return null;
    }

    /**
     * Guarda un nuevo user en la base de datos.
     *
     * @param user El user a guardar.
     */
    @Override
    public void save(User user) {
        String sql = """
            INSERT INTO users (name, role, pin, tipo_cobro, param_1, param_2)
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
            logger.warn("Error al guardar user: {}", e.getMessage());
        }
    }

    /**
     * Borra un barbero de la base de datos.
     *
     * @param id El barbero a borrar.
     */
    @Override
    public void delete(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            logger.warn("Error al borrar barbero id {}: {}", id, e.getMessage());
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("role"),
                rs.getString("pin"),
                rs.getInt("tipo_cobro"),
                rs.getDouble("param_1"),
                rs.getDouble("param_2")
        );
    }

}
