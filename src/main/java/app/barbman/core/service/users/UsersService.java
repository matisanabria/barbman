package app.barbman.core.service.users;

import app.barbman.core.model.human.User;
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
     * Create a new user.
     * Validates that the user doesn't already have an ID.
     */
    public void create(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        if (user.getId() != 0) {
            throw new IllegalArgumentException("Cannot create user with existing ID. Use update() instead.");
        }

        // Validate PIN uniqueness
        User existingUser = usersRepository.findByPin(user.getPin());
        if (existingUser != null) {
            throw new IllegalArgumentException("PIN already exists. Please use a different PIN.");
        }

        usersRepository.save(user);
        logger.info("{} User created -> {} (ID: {})", PREFIX, user.getName(), user.getId());
    }

    /**
     * Fetch all active users (excluding deleted ones).
     */
    public List<User> getAllUsers() {
        logger.info("{} Fetching all active users...", PREFIX);
        List<User> users = usersRepository.findAll().stream()
                .filter(u -> !"deleted".equals(u.getRole()))
                .collect(java.util.stream.Collectors.toList());
        logger.info("{} {} active users loaded.", PREFIX, users.size());
        return users;
    }

    /**
     * Find a single user by ID.
     */
    public User getUserById(int id) {
        return usersRepository.findById(id);
    }

    public void updateUser(User user) {
        if (user == null) {
            logger.warn("{} Attempted to update null user.", PREFIX);
            throw new IllegalArgumentException("User cannot be null");
        }
        usersRepository.update(user);
        logger.info("{} User updated -> {}", PREFIX, user.getName());
    }

    /**
     * Delete a user by ID.
     */
    public void deleteUser(int id) {
        usersRepository.delete(id);
        logger.info("{} User deleted -> ID {}", PREFIX, id);
    }



    /**
     * Count active admin users (excluding deleted).
     */
    private int countActiveAdmins() {
        return (int) usersRepository.findAll().stream()
                .filter(u -> !"deleted".equals(u.getRole()))
                .filter(u -> "admin".equalsIgnoreCase(u.getRole()))
                .count();
    }

    /**
     * Soft delete a user by setting role to "deleted".
     * Prevents deletion if user is the last admin.
     */
    public void softDelete(int id) {
        User user = usersRepository.findById(id);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        // Check if user is admin
        if ("admin".equalsIgnoreCase(user.getRole())) {
            int activeAdmins = countActiveAdmins();

            if (activeAdmins <= 1) {
                throw new IllegalStateException("No se puede eliminar el ultimo administrador del sistema.");
            }
        }

        user.setRole("deleted");
        usersRepository.update(user);
        logger.info("{} User soft deleted (role set to 'deleted') -> ID {}", PREFIX, id);
    }
}
