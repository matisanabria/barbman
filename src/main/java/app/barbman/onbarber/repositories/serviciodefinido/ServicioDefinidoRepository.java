package app.barbman.onbarber.repositories.serviciodefinido;

import app.barbman.onbarber.model.ServicioDefinido;

import java.util.List;

public interface ServicioDefinidoRepository {
    ServicioDefinido findById(int id);
    List<ServicioDefinido> findAll();
    void save(ServicioDefinido servicio);
    void delete(int id);
}
