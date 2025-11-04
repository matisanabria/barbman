package app.barbman.core.service.users;

import app.barbman.core.model.User;
import app.barbman.core.repositories.users.UsersRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Handles user-related logic for both admin management and internal access.
 */
public class UsersService {

    private static final Logger logger = LogManager.getLogger(UsersService.class);
    private static final String PREFIX = "[USERS-SERVICE]";

    private final UsersRepository usersRepository;

    public UsersService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    /**
     * Fetch all users from the repository.
     */
    public List<User> getAllUsers() {
        logger.info("{} Fetching all users...", PREFIX);
        List<User> users = usersRepository.findAll();
        logger.info("{} {} users loaded.", PREFIX, users.size());
        return users;
    }

    /**
     * Find a single user by ID.
     */
    public User getUserById(int id) {
        return usersRepository.findById(id);
    }

    /**
     * Create or update a user.
     */
    public void saveUser(User user) {
        if (user == null) {
            logger.warn("{} Attempted to save null user.", PREFIX);
            throw new IllegalArgumentException("User cannot be null");
        }
        usersRepository.save(user);
        logger.info("{} User saved -> {}", PREFIX, user.getName());
    }

    /**
     * Delete a user by ID.
     */
    public void deleteUser(int id) {
        usersRepository.delete(id);
        logger.info("{} User deleted -> ID {}", PREFIX, id);
    }
}
