package app.barbman.core.repositories.serviciodefinido;

import app.barbman.core.model.ServicioDefinido;
import app.barbman.core.repositories.DbBootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicioDefinidoRepositoryImpl implements ServicioDefinidoRepository{

    private static final Logger logger = LogManager.getLogger(ServicioDefinidoRepositoryImpl.class);

    private static final String SELECT_BASE = """
        SELECT id, nombre, precio_base
        FROM servicios_definidos
        """;

    /**
     * Busca un servicio definido por su ID.
     * @param id Identificador del servicio definido
     * @return ServicioDefinido si se encuentra, null si no existe
     */
    @Override
    public ServicioDefinido findById(int id) {
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
            logger.warn("Error al obtener servicio definido por id {}: {}", id, e.getMessage());
        }
        return null;
    }

    /**
     * Obtiene la lista de todos los servicios definidos.
     * @return Lista de ServicioDefinido, lista vacía si ocurre un error
     */
    @Override
    public List<ServicioDefinido> findAll() {
        List<ServicioDefinido> list = new ArrayList<>();
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(SELECT_BASE);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (Exception e) {
            logger.warn("Error al listar servicios definidos: {}", e.getMessage());
        }
        return List.of(); // lista vacía, evita null-check
    }

    /**
     * Guarda un nuevo servicio definido en la base de datos.
     * @param servicio Objeto ServicioDefinido a guardar
     */
    @Override
    public void save(ServicioDefinido servicio) {
        String sql = """
            INSERT INTO servicios_definidos (nombre, precio_base)
            VALUES (?, ?)
            """;
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, servicio.getNombre());
            ps.setDouble(2, servicio.getPrecioBase());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    servicio.setId(keys.getInt(1));
                }
            }
        } catch (Exception e) {
            logger.warn("Error al guardar servicio definido: {}", e.getMessage());
        }
    }

    /**
     * Elimina un servicio definido por su ID.
     * @param id Identificador del servicio definido a eliminar
     */
    @Override
    public void delete(int id) {
        String sql = "DELETE FROM servicios_definidos WHERE id = ?";
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            logger.warn("Error al borrar servicio definido id {}: {}", id, e.getMessage());
        }
    }

    private ServicioDefinido mapRow(ResultSet rs) throws SQLException {
        return new ServicioDefinido(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getDouble("precio_base")
        );
    }

}
