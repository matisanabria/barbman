package app.barbman.core.repositories.sueldos;

import app.barbman.core.model.Sueldo;

import java.util.List;

public interface SueldosRepository {
    List<Sueldo> findAll();
    Sueldo findById(int id);
    void save(Sueldo sueldo);
    void update(Sueldo sueldo);
    void delete(int id);
}
