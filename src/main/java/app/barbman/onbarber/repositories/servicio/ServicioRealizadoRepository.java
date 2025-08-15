package app.barbman.onbarber.repositories.servicio;

import app.barbman.onbarber.model.ServicioRealizado;

import java.util.Date;
import java.util.List;

public interface ServicioRealizadoRepository {
    ServicioRealizado findById(int id);
    List<ServicioRealizado> findAll();
    void save(ServicioRealizado servicioRealizado);
    void delete(Long id);
    List<ServicioRealizado> searchByDateRange(Date startDate, Date endDate);
    List<ServicioRealizado> searchByBarberoId(int barberoId);
    List<ServicioRealizado> searchByTipoServicio(int tipoServicio);
}