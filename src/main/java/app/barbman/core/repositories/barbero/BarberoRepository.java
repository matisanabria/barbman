package app.barbman.core.repositories.barbero;

import app.barbman.core.model.User;

import java.util.ArrayList;
import java.util.List;

public interface BarberoRepository {
    List<User> LISTA_USERS = new ArrayList<>();
    User findById(int id);
    List<User> findAll();
    User findByPin(String pin);
    void save(User user);
    void delete(int id);

}
