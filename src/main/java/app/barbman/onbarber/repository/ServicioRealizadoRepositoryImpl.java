package app.barbman.onbarber.repository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import app.barbman.onbarber.model.Barbero;
import app.barbman.onbarber.model.ServicioRealizado;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class ServicioRealizadoRepositoryImpl implements ServicioRealizadoRepository {

    List<ServicioRealizado> listaServiciosRealizados = new ArrayList<>();
    private static final Logger logger = LogManager.getLogger(ServicioRealizadoRepositoryImpl.class);

    @Override
    public ServicioRealizado findById(int id) {
        ServicioRealizado sr = new ServicioRealizado(id);
        var query = "SELECT * FROM servicios_realizados WHERE id=?";
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(query)
        ) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new ServicioRealizado(
                            rs.getInt("id"),
                            rs.getInt("id_barbero"),
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
        return sr;
    }

    @Override
    public List<ServicioRealizado> findAll() {
        // Lógica para encontrar todos los servicios realizados
        return null; // Placeholder
    }

    @Override
    public void save(ServicioRealizado servicioRealizado) {
        // Lógica para guardar un servicio realizado en la base de datos
    }

    @Override
    public void delete(Long id) {
        // Lógica para eliminar un servicio realizado por ID
    }
    @Override
    public List<ServicioRealizado> searchByDateRange(Date startDate, Date endDate) {
        // Lógica para buscar servicios realizados por rango de fechas
        return new ArrayList<>(); // Placeholder
    }
    @Override
    public List<ServicioRealizado> searchByBarberoId(int barberoId) {
        // Lógica para buscar servicios realizados por ID de barbero
        return new ArrayList<>(); // Placeholder
    }
    @Override
    public List<ServicioRealizado> searchByTipoServicio(int tipoServicio) { 
        // Lógica para buscar servicios realizados por tipo de servicio
        return new ArrayList<>(); // Placeholder
    }

}
