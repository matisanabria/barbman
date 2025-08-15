package app.barbman.onbarber.repositories.servicio;

import app.barbman.onbarber.model.ServicioRealizado;
import app.barbman.onbarber.repositories.DbBootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Implementación del repositorio para la entidad ServicioRealizado.
 * Proporciona métodos para realizar operaciones CRUD y consultas específicas
 * en la tabla servicios_realizados de la base de datos.
 */
public class ServicioRealizadoRepositoryImpl implements ServicioRealizadoRepository {

    private static final Logger logger = LogManager.getLogger(ServicioRealizadoRepositoryImpl.class);

    /**
     * Busca un servicio realizado por su ID.
     * @param id Identificador del servicio realizado
     * @return ServicioRealizado si se encuentra, null si no existe
     */
    @Override
    public ServicioRealizado findById(int id) {
        var query = "SELECT * FROM servicios_realizados WHERE id=?";
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(query)
        ) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new ServicioRealizado(
                            rs.getInt("id"),
                            rs.getInt("barbero_id"),
                            rs.getInt("tipo_servicio"),
                            rs.getInt("precio"),
                            rs.getDate("fecha"),
                            rs.getString("forma_pago"),
                            rs.getString("observaciones")
                    );
                }
            }


        } catch (Exception e) {
            logger.warn("Error al obtener el barbero con pin: " + e.getMessage());
        }
        return null;
    }

    /**
     * Obtiene la lista de todos los servicios realizados.
     * @return Lista de ServicioRealizado, null si ocurre un error
     */
    @Override
    public List<ServicioRealizado> findAll() {
        List<ServicioRealizado> listaServiciosRealizados = new ArrayList<>();
        var query = "SELECT * FROM servicios_realizados";
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(query);
             ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                listaServiciosRealizados.add(new ServicioRealizado(
                        rs.getInt("id"),
                        rs.getInt("barbero_id"),
                        rs.getInt("tipo_servicio"),
                        rs.getInt("precio"),
                        rs.getDate("fecha"),
                        rs.getString("forma_pago"),
                        rs.getString("observaciones")
                ));
            }
            return listaServiciosRealizados;
        } catch (Exception e) {
            logger.warn("Error al obtener los servicios realizados: " + e.getMessage());
        }
        return null;
    }

    /**
     * Guarda un nuevo servicio realizado en la base de datos.
     * @param servicioRealizado Objeto ServicioRealizado a guardar
     */
    @Override
    public void save(ServicioRealizado servicioRealizado) {
        var query = "INSERT INTO servicios_realizados (id_barbero, tipo_servicio, precio, fecha, forma_pago, observaciones) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(query)
        ) {
            ps.setInt(1, servicioRealizado.getBarberoId());
            ps.setInt(2, servicioRealizado.getTipoServicio());
            ps.setInt(3, servicioRealizado.getPrecio());
            ps.setDate(4, new java.sql.Date(servicioRealizado.getFecha().getTime()));
            ps.setString(5, servicioRealizado.getFormaPago());
            ps.setString(6, servicioRealizado.getObservaciones());
            ps.executeUpdate();
        } catch (Exception e) {
            logger.warn("Error al guardar el servicio realizado: " + e.getMessage());
        }
    }

    /**
     * Elimina un servicio realizado por su ID.
     * @param id Identificador del servicio realizado a eliminar
     */
    @Override
    public void delete(Long id) {
        var query = "DELETE FROM servicios_realizados WHERE id = ?";
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(query)
        ) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            logger.warn("Error al eliminar el servicio realizado: " + e.getMessage());
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
    public List<ServicioRealizado> searchByDateRange(Date startDate, Date endDate) {
        throw new UnsupportedOperationException("Funcionalidad próximamente disponible");
    }

    /**
     * Busca servicios realizados por el ID del barbero.
     * @param barberoId Identificador del barbero
     * @return Lista de ServicioRealizado asociados al barbero
     */
    @Override
    public List<ServicioRealizado> searchByBarberoId(int barberoId) {
        List<ServicioRealizado> listaServiciosRealizados = new ArrayList<>();
        var query = "SELECT * FROM servicios_realizados WHERE barbero_id=?";
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(query)) {
            ps.setInt(1, barberoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ServicioRealizado sr = new ServicioRealizado(
                            rs.getInt("id"),
                            rs.getInt("barbero_id"),
                            rs.getInt("tipo_servicio"),
                            rs.getInt("precio"),
                            rs.getDate("fecha"),
                            rs.getString("forma_pago"),
                            rs.getString("observaciones")
                    );
                    listaServiciosRealizados.add(sr);
                }
            }
        } catch (Exception e) {
            logger.warn("Error al buscar servicios realizados por ID de barbero: " + e.getMessage());
        }
        return listaServiciosRealizados;
    }

    /**
     * Busca servicios realizados por el tipo de servicio.
     * @param tipoServicio Identificador del tipo de servicio
     * @return Lista de ServicioRealizado asociados al tipo de servicio
     */
    @Override
    public List<ServicioRealizado> searchByTipoServicio(int tipoServicio) {
        List<ServicioRealizado> listaServiciosRealizados = new ArrayList<>();
        var query = "SELECT * FROM servicios_realizados WHERE tipo_servicio=?";
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(query)) {
            ps.setInt(1, tipoServicio);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ServicioRealizado sr = new ServicioRealizado(
                            rs.getInt("id"),
                            rs.getInt("barbero_id"),
                            rs.getInt("tipo_servicio"),
                            rs.getInt("precio"),
                            rs.getDate("fecha"),
                            rs.getString("forma_pago"),
                            rs.getString("observaciones")
                    );
                    listaServiciosRealizados.add(sr);
                }
            }
        } catch (Exception e) {
            logger.warn("Error al buscar servicios realizados por tipo de servicio: " + e.getMessage());
        }
        return listaServiciosRealizados;
    }

}
