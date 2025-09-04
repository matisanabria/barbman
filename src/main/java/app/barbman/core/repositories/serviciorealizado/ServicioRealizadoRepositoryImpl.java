package app.barbman.core.repositories.serviciorealizado;

import app.barbman.core.model.ServicioRealizado;
import app.barbman.core.repositories.DbBootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;


/**
 * Implementación del repositorio para la entidad ServicioRealizado.
 * Proporciona métodos para realizar operaciones CRUD y consultas específicas
 * en la tabla servicios_realizados de la base de datos.
 * Usa LocalDate para el campo fecha (formato ISO 'YYYY-MM-DD' almacenado como TEXT en SQLite).
 */
public class ServicioRealizadoRepositoryImpl implements ServicioRealizadoRepository {

    private static final Logger logger = LogManager.getLogger(ServicioRealizadoRepositoryImpl.class);

    // SELECT base reutilizable
    private static final String SELECT_BASE = """
        SELECT id, barbero_id, tipo_servicio, precio, fecha, forma_pago, observaciones
        FROM servicios_realizados
        """;

    /**
     * Busca un servicio realizado por su ID.
     * @param id Identificador del servicio realizado
     * @return ServicioRealizado si se encuentra, null si no existe
     */
    @Override
    public ServicioRealizado findById(int id) {
        String sql = SELECT_BASE + " WHERE id = ?";
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs); // centralizamos el mapeo
                }
            }
        } catch (Exception e) {
            logger.warn("Error al obtener servicio realizado por id {}: {}", id, e.getMessage());
            // logger.warn("Error al obtener servicio realizado por id " + id, e); // para stacktrace completo
        }
        return null;
    }

    /**
     * Obtiene la lista de todos los servicios realizados.
     * @return Lista de ServicioRealizado, null si ocurre un error
     */
    @Override
    public List<ServicioRealizado> findAll() {
        List<ServicioRealizado> list = new ArrayList<>();
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(SELECT_BASE);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (Exception e) {
            logger.warn("Error al listar servicios realizados: {}", e.getMessage());
        }
        return List.of(); // lista vacía, evita null-check
    }

    /**
     * Guarda un nuevo servicio realizado en la base de datos.
     * @param s Objeto ServicioRealizado a guardar
     */
    @Override
    public void save(ServicioRealizado s) {
        String sql = """
            INSERT INTO servicios_realizados
                (barbero_id, tipo_servicio, precio, fecha, forma_pago, observaciones)
            VALUES (?,?,?,?,?,?)
            """;
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, s.getBarberoId());
            ps.setInt(2, s.getTipoServicio());
            ps.setDouble(3, s.getPrecio());
            ps.setString(4, s.getFecha().toString());  // LocalDate -> 'YYYY-MM-DD'
            ps.setString(5, s.getFormaPago());

            if (s.getObservaciones() != null) {
                ps.setString(6, s.getObservaciones());
            } else {
                ps.setNull(6, Types.VARCHAR);
            }

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    s.setId(keys.getInt(1)); // asignamos el id generado a la entidad
                }
            }
        } catch (Exception e) {
            logger.warn("Error al guardar servicio realizado: {}", e.getMessage());
        }
    }



    /**
     * Elimina un servicio realizado por su ID.
     * @param id Identificador del servicio realizado a eliminar
     */
    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM servicios_realizados WHERE id = ?";
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            logger.warn("Error al eliminar servicio realizado id {}: {}", id, e.getMessage());
        }
    }

    /**
     * (Próximamente) Busca servicios realizados en un rango de fechas.
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @return Lista de ServicioRealizado en el rango de fechas
     * @throws UnsupportedOperationException funcionalidad pendiente de implementación
     */
    @Override
    public List<ServicioRealizado> searchByDateRange(LocalDate startDate, LocalDate endDate) {
        throw new UnsupportedOperationException("Funcionalidad próximamente disponible");
    }

    /**
     * Busca servicios realizados por el ID del barbero.
     * @param barberoId Identificador del barbero
     * @return Lista de ServicioRealizado asociados al barbero
     */
    @Override
    public List<ServicioRealizado> searchByBarberoId(int barberoId) {
        String sql = SELECT_BASE + " WHERE barbero_id = ? ORDER BY fecha, id";
        List<ServicioRealizado> list = new ArrayList<>();
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, barberoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (Exception e) {
            logger.warn("Error al buscar servicios por barbero_id {}: {}", barberoId, e.getMessage());
        }
        return list;
    }

    /**
     * Busca servicios realizados por el tipo de servicio.
     * @param tipoServicio Identificador del tipo de servicio
     * @return Lista de ServicioRealizado asociados al tipo de servicio
     */
    @Override
    public List<ServicioRealizado> searchByTipoServicio(int tipoServicio) {
        String sql = SELECT_BASE + " WHERE tipo_servicio = ? ORDER BY fecha, id";
        List<ServicioRealizado> list = new ArrayList<>();
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, tipoServicio);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (Exception e) {
            logger.warn("Error al buscar servicios por tipo_servicio {}: {}", tipoServicio, e.getMessage());
        }
        return list;
    }

    private ServicioRealizado mapRow(ResultSet rs) throws SQLException {
        String rawFecha = rs.getString("fecha"); // viene como TEXT
        LocalDate fecha = LocalDate.parse(rawFecha); // lanza excepción si formato inválido
        return new ServicioRealizado(
                rs.getInt("id"),
                rs.getInt("barbero_id"),
                rs.getInt("tipo_servicio"),
                rs.getDouble("precio"),
                fecha,
                rs.getString("forma_pago"),
                rs.getString("observaciones")
        );
    }

}
