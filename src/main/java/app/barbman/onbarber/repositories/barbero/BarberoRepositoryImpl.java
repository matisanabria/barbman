package app.barbman.onbarber.repositories.barbero;

import app.barbman.onbarber.model.Barbero;
import app.barbman.onbarber.repositories.DbBootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación de BarberoRepository para gestionar operaciones CRUD en la tabla 'barberos'.
 */
public class BarberoRepositoryImpl implements BarberoRepository {
    List<Barbero> listaBarberos = new ArrayList<>();
    private static final Logger logger = LogManager.getLogger(BarberoRepositoryImpl.class);

    private static final String SELECT_BASE = """
        SELECT id, nombre, rol, pin, tipo_cobro, param_1, param_2
        FROM barberos
        """;

    /**
     * Busca un barbero por su ID.
     *
     * @param id El ID del barbero a buscar.
     * @return El barbero encontrado o null si no existe.
     */
    @Override
    public Barbero findById(int id) {
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
     * Devuelve una lista de todos los barberos en la base de datos.
     *
     * @return Lista de barberos.
     */
    @Override
    public List<Barbero> findAll() {
        List<Barbero> lista = new ArrayList<>();
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(SELECT_BASE);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapRow(rs));
            }
            return lista;
        } catch (Exception e) {
            logger.warn("Error al listar barberos: {}", e.getMessage());
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
    public Barbero findByPin(String pin) {
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
     * Guarda un nuevo barbero en la base de datos.
     *
     * @param barbero El barbero a guardar.
     */
    @Override
    public void save(Barbero barbero) {
        String sql = """
            INSERT INTO barberos (nombre, rol, pin, tipo_cobro, param_1, param_2)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, barbero.getNombre());
            ps.setString(2, barbero.getRol());
            ps.setString(3, barbero.getPin());
            ps.setInt(4, barbero.getTipoCobro());
            ps.setDouble(5, barbero.getParam1());
            ps.setDouble(6, barbero.getParam2());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    barbero.setId(keys.getInt(1));
                }
            }
        } catch (Exception e) {
            logger.warn("Error al guardar barbero: {}", e.getMessage());
        }
    }

    /**
     * Borra un barbero de la base de datos.
     *
     * @param id El barbero a borrar.
     */
    @Override
    public void delete(int id) {
        String sql = "DELETE FROM barberos WHERE id = ?";
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            logger.warn("Error al borrar barbero id {}: {}", id, e.getMessage());
        }
    }

    private Barbero mapRow(ResultSet rs) throws SQLException {
        return new Barbero(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("rol"),
                rs.getString("pin"),
                rs.getInt("tipo_cobro"),
                rs.getDouble("param_1"),
                rs.getDouble("param_2")
        );
    }

}
