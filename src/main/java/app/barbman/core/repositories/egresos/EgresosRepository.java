package app.barbman.core.repositories.egresos;

import app.barbman.core.model.Egreso;

import java.util.List;

public interface EgresosRepository {
    List<Egreso> findAll();
    Egreso findById(int id);
    void save(Egreso egreso);
    void update(Egreso egreso);
    void delete(int id);
}
