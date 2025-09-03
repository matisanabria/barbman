package app.barbman.onbarber.repositories.barbero;

import app.barbman.onbarber.model.Barbero;

import java.util.ArrayList;
import java.util.List;

public interface BarberoRepository {
    List<Barbero> listaBarberos = new ArrayList<>();
    Barbero findById(int id);
    List<Barbero> findAll();
    Barbero findByPin(String pin);
    void save(Barbero barbero);
    void delete(int id);

}
