package app.barbman.core.repositories.caja;

import app.barbman.core.model.CajaDiaria;

import java.time.LocalDate;
import java.util.List;

public interface CajaRepository {
    List<CajaDiaria> findAll();
    CajaDiaria findById(int id);
    void save(CajaDiaria caja);
    void update(CajaDiaria caja);
    void delete(int id);

    CajaDiaria findByFecha(LocalDate fecha);
    CajaDiaria findUltimaCajaAntes(LocalDate fecha);
}
