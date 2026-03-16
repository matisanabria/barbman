package app.barbman.core.repositories.users;

import app.barbman.core.model.human.User;

import java.util.List;

public interface UsersRepository {
    User findByPin(String pin);
    List<User> findAll();
    User findById(Integer id);
    void save(User user);
    void update(User user);
    void delete(Integer id);
}
