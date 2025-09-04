package app.barbman.core.repositories.serviciodefinido;

import app.barbman.core.model.ServicioDefinido;

import java.util.List;

public interface ServicioDefinidoRepository {
    ServicioDefinido findById(int id);
    List<ServicioDefinido> findAll();
    void save(ServicioDefinido servicio);
    // TODO: void update(ServicioDefinido servicio);
    void delete(int id);
}
