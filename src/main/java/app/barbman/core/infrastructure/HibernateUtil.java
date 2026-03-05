package app.barbman.core.infrastructure;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages the Hibernate EntityManagerFactory lifecycle.
 *
 * Must be initialized once at startup via {@link #init(String)} before
 * calling {@link #createEntityManager()}. The factory is expensive to create
 * (reads entity metadata, validates schema) so it lives for the app's lifetime.
 *
 * Usage pattern in repositories:
 * <pre>
 *   try (EntityManager em = HibernateUtil.createEntityManager()) {
 *       em.getTransaction().begin();
 *       // ... operations ...
 *       em.getTransaction().commit();
 *   }
 * </pre>
 */
public class HibernateUtil {

    private static final Logger logger = LogManager.getLogger(HibernateUtil.class);
    private static EntityManagerFactory emf;

    private HibernateUtil() {}

    /**
     * Initializes the EntityManagerFactory with the resolved SQLite database path.
     * Must be called once before using any repository.
     *
     * @param dbPath absolute path to the SQLite database file
     */
    public static void init(String dbPath) {
        logger.info("[HIBERNATE] Initializing EntityManagerFactory. DB: {}", dbPath);

        Map<String, Object> props = new HashMap<>();
        props.put("jakarta.persistence.jdbc.url", "jdbc:sqlite:" + dbPath);
        props.put("jakarta.persistence.jdbc.driver", "org.sqlite.JDBC");
        props.put("hibernate.dialect", "org.hibernate.community.dialect.SQLiteDialect");

        // Flyway handles DDL — Hibernate only validates/uses the schema.
        // Use "none" to avoid Hibernate touching tables managed by Flyway.
        props.put("hibernate.hbm2ddl.auto", "none");

        props.put("hibernate.show_sql", "false");
        props.put("hibernate.format_sql", "true");

        emf = Persistence.createEntityManagerFactory("barbman", props);
        logger.info("[HIBERNATE] EntityManagerFactory ready.");
    }

    /**
     * Creates a new EntityManager. Caller is responsible for closing it.
     * Use try-with-resources to ensure it's closed.
     */
    public static EntityManager createEntityManager() {
        if (emf == null) {
            throw new IllegalStateException("HibernateUtil not initialized. Call HibernateUtil.init() first.");
        }
        return emf.createEntityManager();
    }

    /**
     * Shuts down the factory. Called on application exit.
     */
    public static void shutdown() {
        if (emf != null && emf.isOpen()) {
            logger.info("[HIBERNATE] Closing EntityManagerFactory.");
            emf.close();
        }
    }
}
