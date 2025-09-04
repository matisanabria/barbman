package app.barbman.core.repositories.egresos;

import app.barbman.core.model.Egreso;
import app.barbman.core.repositories.DbBootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EgresosRepositoryImpl implements EgresosRepository{
    private static final Logger logger = LogManager.getLogger(EgresosRepositoryImpl.class);

    private static final String SELECT_BASE = """
        SELECT id, descripcion, monto, fecha, tipo
        FROM egresos
        """;

    /**
     * Obtiene la lista de todos los egresos.
     * @return Lista de egresos, lista vacía si ocurre un error
     */
    @Override
    public List<Egreso> findAll() {
        List<Egreso> list = new ArrayList<>();
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(SELECT_BASE);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (Exception e) {
            logger.warn("Error al listar egresos: {}", e.getMessage());
        }
        return List.of();
    }

    /**
     * Busca un egreso por su ID.
     * @param id Identificador del egreso
     * @return Egreso si se encuentra, null si no existe
     */
    @Override
    public Egreso findById(int id) {
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
            logger.warn("Error al obtener egreso por id {}: {}", id, e.getMessage());
        }
        return null;
    }

    /**
     * Guarda un nuevo egreso en la base de datos.
     * @param egreso Objeto Egreso a guardar
     */
    @Override
    public void save(Egreso egreso) {
        String sql = """
            INSERT INTO egresos (descripcion, monto, fecha, tipo)
            VALUES (?, ?, ?, ?)
            """;
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, egreso.getDescripcion());
            ps.setDouble(2, egreso.getMonto());
            ps.setString(3, egreso.getFecha().toString());
            ps.setString(4, egreso.getTipo());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    egreso.setId(keys.getInt(1));
                }
            }
        } catch (Exception e) {
            logger.warn("Error al guardar egreso: {}", e.getMessage());
        }
    }

    /**
     * Actualiza un egreso existente en la base de datos.
     * @param egreso Objeto Egreso con los datos actualizados
     */
    @Override
    public void update(Egreso egreso) {
        String sql = """
            UPDATE egresos
            SET descripcion = ?, monto = ?, fecha = ?, tipo = ?
            WHERE id = ?
            """;
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setString(1, egreso.getDescripcion());
            ps.setDouble(2, egreso.getMonto());
            ps.setString(3, egreso.getFecha().toString());
            ps.setString(4, egreso.getTipo());
            ps.setInt(5, egreso.getId());

            ps.executeUpdate();
        } catch (Exception e) {
            logger.warn("Error al actualizar egreso id {}: {}", egreso.getId(), e.getMessage());
        }
    }

    /**
     * Elimina un egreso por su ID.
     * @param id Identificador del egreso a eliminar
     */
    @Override
    public void delete(int id) {
        String sql = "DELETE FROM egresos WHERE id = ?";
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            logger.warn("Error al borrar egreso id {}: {}", id, e.getMessage());
        }
    }

    private Egreso mapRow(ResultSet rs) throws SQLException {
        return new Egreso(
                rs.getInt("id"),
                rs.getString("descripcion"),
                rs.getDouble("monto"),
                LocalDate.parse(rs.getString("fecha")),
                rs.getString("tipo")
        );
    }
}
