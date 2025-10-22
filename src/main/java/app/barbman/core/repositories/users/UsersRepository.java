package app.barbman.core.repositories.users;

import app.barbman.core.model.User;
import app.barbman.core.repositories.GenericRepository;

public interface UsersRepository  extends GenericRepository<User, Integer> {
    User findByPin(String pin);
}
