package app.barbman.onbarber.repositories.barbero;

import app.barbman.onbarber.model.Barbero;

import java.util.ArrayList;
import java.util.List;

public interface BarberoRepository {
    List<Barbero> listaBarberos = new ArrayList<>();
    public List<Barbero> loadBarberos();
    public Barbero getBarberoWithPin(String pin);
    public String getNombreById(int id);

}
