package app.barbman.onbarber.repository;

import app.barbman.onbarber.models.Barbero;
import app.barbman.onbarber.util.LoggerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Repositorio de acceso a datos para la tabla "barberos".
 * Se encarga de cargar la lista de barberos y buscar un barbero por su PIN.
 */

public class BarberoRepo {
    List<Barbero> listaBarberos = new ArrayList<>();
    private static final Logger logger = LoggerUtil.getLogger(BarberoRepo.class);

    /**
     * Carga todos los barberos de la base.
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
            logger.warning("Error al obtener los barberos: " + e.getMessage());
        }

        return listaBarberos;
    }


    /**
     * Busca un barbero por PIN. Si no existe, devuelve null.
     * @param pin PIN de 4 dígitos.
     */
    public Barbero getBarberoWithPin(String pin) {
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
            logger.warning("Error al obtener el barbero con pin: " + e.getMessage());
        }

        return null;
    }

}
