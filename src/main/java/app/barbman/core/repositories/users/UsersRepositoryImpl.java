package app.barbman.core.repositories.users;

import app.barbman.core.infrastructure.HibernateUtil;
import app.barbman.core.model.human.User;
import app.barbman.core.repositories.AbstractHibernateRepository;
import jakarta.persistence.EntityManager;

public class UsersRepositoryImpl extends AbstractHibernateRepository<User, Integer>
        implements UsersRepository {

    public UsersRepositoryImpl() {
        super(User.class);
    }

    @Override
    public User findByPin(String pin) {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            return em.createQuery("FROM User WHERE pin = :pin", User.class)
                    .setParameter("pin", pin)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            logger.warn("[UsersRepositoryImpl] Error finding user by PIN: {}", e.getMessage());
            return null;
        }
    }
}
