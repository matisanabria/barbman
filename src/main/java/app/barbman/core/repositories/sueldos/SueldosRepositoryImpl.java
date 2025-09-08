package app.barbman.core.repositories.sueldos;

import app.barbman.core.model.Sueldo;
import app.barbman.core.repositories.DbBootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SueldosRepositoryImpl implements SueldosRepository{
    private static final Logger logger = LogManager.getLogger(SueldosRepositoryImpl.class);

    private static final String SELECT_BASE = """
        SELECT id, barbero_id, fecha_inicio_semana, fecha_fin_semana,
               produccion_total, monto_liquidado, tipo_cobro_snapshot,
               fecha_pago, forma_pago
        FROM sueldos
        """;

    /**
     * Obtiene la lista de todos los sueldos.
     * @return Lista de sueldos, lista vacía si ocurre un error
     */
    @Override
    public List<Sueldo> findAll() {
        List<Sueldo> list = new ArrayList<>();
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(SELECT_BASE);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (Exception e) {
            logger.warn("Error al listar sueldos: {}", e.getMessage());
        }
        return List.of();
    }


    /**
     * Busca un sueldo por su ID.
     * @param id Identificador del sueldo
     * @return Sueldo si se encuentra, null si no existe
     */
    @Override
    public Sueldo findById(int id) {
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
            logger.warn("Error al obtener sueldo por id {}: {}", id, e.getMessage());
        }
        return null;
    }

    /**
     * Guarda un nuevo sueldo en la base de datos.
     * @param sueldo Objeto Sueldo a guardar
     */
    @Override
    public void save(Sueldo sueldo) {
        String sql = """
            INSERT INTO sueldos (barbero_id, fecha_inicio_semana, fecha_fin_semana,
                                 produccion_total, monto_liquidado,
                                 tipo_cobro_snapshot, fecha_pago, forma_pago)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, sueldo.getBarberoId());
            ps.setString(2, sueldo.getFechaInicioSemana().toString());
            ps.setString(3, sueldo.getFechaFinSemana().toString());
            ps.setDouble(4, sueldo.getProduccionTotal());
            ps.setDouble(5, sueldo.getMontoLiquidado());
            ps.setInt(6, sueldo.getTipoCobroSnapshot());
            ps.setString(7, sueldo.getFechaPago().toString());
            ps.setString(8, sueldo.getFormaPago());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    sueldo.setId(keys.getInt(1));
                }
            }
        } catch (Exception e) {
            logger.warn("Error al guardar sueldo: {}", e.getMessage());
        }
    }


    @Override
    public void update(Sueldo sueldo) {
        String sql = """
            UPDATE sueldos
            SET barbero_id = ?, fecha_inicio_semana = ?, fecha_fin_semana = ?,
                produccion_total = ?, monto_liquidado = ?,
                tipo_cobro_snapshot = ?, fecha_pago = ?, forma_pago = ?
            WHERE id = ?
            """;
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, sueldo.getBarberoId());
            ps.setString(2, sueldo.getFechaInicioSemana().toString());
            ps.setString(3, sueldo.getFechaFinSemana().toString());
            ps.setDouble(4, sueldo.getProduccionTotal());
            ps.setDouble(5, sueldo.getMontoLiquidado());
            ps.setInt(6, sueldo.getTipoCobroSnapshot());
            ps.setString(7, sueldo.getFechaPago().toString());
            ps.setString(8, sueldo.getFormaPago());
            ps.setInt(9, sueldo.getId());

            ps.executeUpdate();
        } catch (Exception e) {
            logger.warn("Error al actualizar sueldo id {}: {}", sueldo.getId(), e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM sueldos WHERE id = ?";
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            logger.warn("Error al borrar sueldo id {}: {}", id, e.getMessage());
        }
    }

    @Override
    public Sueldo findByBarberoAndFecha(int barberoId, LocalDate fecha) {
        String sql = """
        SELECT * FROM sueldos
        WHERE barbero_id = ?
        AND fecha_inicio_semana <= ?
        AND fecha_fin_semana >= ?
        LIMIT 1
    """;

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, barberoId);
            ps.setString(2, fecha.toString());
            ps.setString(3, fecha.toString());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }

        } catch (Exception e) {
            logger.warn("Error al buscar sueldo para barbero {} en fecha {}: {}", barberoId, fecha, e.getMessage());
        }

        return null;
    }


    private Sueldo mapRow(ResultSet rs) throws SQLException {
        Sueldo sueldo = new Sueldo();
        sueldo.setId(rs.getInt("id"));
        sueldo.setBarberoId(rs.getInt("barbero_id"));
        sueldo.setFechaInicioSemana(LocalDate.parse(rs.getString("fecha_inicio_semana")));
        sueldo.setFechaFinSemana(LocalDate.parse(rs.getString("fecha_fin_semana")));
        sueldo.setProduccionTotal(rs.getDouble("produccion_total"));
        sueldo.setMontoLiquidado(rs.getDouble("monto_liquidado"));
        sueldo.setTipoCobroSnapshot(rs.getInt("tipo_cobro_snapshot"));
        sueldo.setFechaPago(LocalDate.parse(rs.getString("fecha_pago")));
        sueldo.setFormaPago(rs.getString("forma_pago"));
        return sueldo;
    }
}
