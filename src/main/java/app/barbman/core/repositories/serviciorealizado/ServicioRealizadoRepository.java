package app.barbman.core.repositories.serviciorealizado;

import app.barbman.core.model.ServicioRealizado;


import java.time.LocalDate;
import java.util.List;

public interface ServicioRealizadoRepository {
    ServicioRealizado findById(int id);
    List<ServicioRealizado> findAll();
    void save(ServicioRealizado servicioRealizado);
    void delete(int id);
    List<ServicioRealizado> searchByDateRange(LocalDate startDate, LocalDate endDate);
    List<ServicioRealizado> searchByBarberoId(int barberoId);
    List<ServicioRealizado> searchByTipoServicio(int tipoServicio);
    double getProduccionSemanalPorBarbero(int barberoId, LocalDate desde, LocalDate hasta);
}