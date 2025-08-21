package app.barbman.onbarber.repositories;

import app.barbman.onbarber.model.Barbero;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio de acceso a datos para la tabla "barberos".
 * Se encarga de cargar la lista de barberos y buscar un barbero por su PIN.
 */

public class BarberoRepository {
    List<Barbero> listaBarberos = new ArrayList<>();
    private static final Logger logger = LogManager.getLogger(BarberoRepository.class);

    /**
     * Carga todos los barberos de la base.
     *
     * @return lista con todos los barberos (si hay), vacía si no hay o si falló.
     */
    public List<Barbero> loadBarberos() {
        List<Barbero> listaBarberos = new ArrayList<>();
        var query = "SELECT * FROM barberos";

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Barbero b = new Barbero(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("rol"),
                        rs.getString("pin"),
                        rs.getInt("tipo_cobro"),
                        rs.getDouble("param_1"),
                        rs.getDouble("param_2")
                );
                listaBarberos.add(b);
            }

        } catch (Exception e) {
            logger.warn("Error al obtener los barberos: " + e.getMessage());
        }

        return listaBarberos;
    }


    /**
     * Busca un barbero por PIN. Si no existe, devuelve null.
     *
     * @param pin PIN de 4 dígitos.
     */
    public static Barbero getBarberoWithPin(String pin) {
        var query = "SELECT * FROM barberos WHERE pin = ?";

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(query)
        ) {
            ps.setString(1, pin); // asigna el valor al "?"

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Barbero(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getString("rol"),
                            rs.getString("pin"),
                            rs.getInt("tipo_cobro"),
                            rs.getDouble("param_1"),
                            rs.getDouble("param_2"));
                }
            }

        } catch (Exception e) {
            logger.warn("Error al obtener el barbero con pin: " + e.getMessage());
        }

        return null;
    }
    public static String getNombreById(int id) {
        var query = "SELECT nombre FROM barberos WHERE id = ?";
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(query)
        ) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nombre");
                }
            }
        } catch (Exception e) {
            logger.warn("Error al obtener el nombre del barbero con id: " + id + " - " + e.getMessage());
        }
        return "";
    }

}
