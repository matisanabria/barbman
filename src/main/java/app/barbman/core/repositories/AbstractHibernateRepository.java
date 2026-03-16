package app.barbman.core.repositories;

import app.barbman.core.infrastructure.HibernateUtil;
import jakarta.persistence.EntityManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Base repository that implements standard CRUD via JPA EntityManager.
 *
 * Each EntityManager is opened and closed per operation (stateless).
 * For multi-repo transactions, use the overloads that accept an EntityManager
 * provided by the service layer:
 *
 * <pre>
 *   EntityManager em = HibernateUtil.createEntityManager();
 *   em.getTransaction().begin();
 *   saleRepo.save(sale, em);
 *   serviceHeaderRepo.save(header, em);
 *   em.getTransaction().commit();
 *   em.close();
 * </pre>
 *
 * Rollback on error is handled by the service layer; repos only throw RuntimeException.
 */
public abstract class AbstractHibernateRepository<T, ID> {

    private final Class<T> entityClass;
    protected final Logger logger;

    protected AbstractHibernateRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
        this.logger = LogManager.getLogger(getClass());
    }

    // ================================================================
    // Standalone operations (own EntityManager)
    // ================================================================

    public T findById(ID id) {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            return em.find(entityClass, id);
        } catch (Exception e) {
            logger.error("[{}] Error finding {} by ID {}: {}", getClass().getSimpleName(), entityClass.getSimpleName(), id, e.getMessage());
            return null;
        }
    }

    public List<T> findAll() {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            return em.createQuery("FROM " + entityClass.getSimpleName(), entityClass).getResultList();
        } catch (Exception e) {
            logger.error("[{}] Error fetching all {}: {}", getClass().getSimpleName(), entityClass.getSimpleName(), e.getMessage());
            return List.of();
        }
    }

    public void save(T entity) {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(entity);
            em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("[{}] Error persisting {}: {}", getClass().getSimpleName(), entityClass.getSimpleName(), e.getMessage());
            throw new RuntimeException("Failed to save " + entityClass.getSimpleName(), e);
        }
    }

    public void update(T entity) {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            em.getTransaction().begin();
            em.merge(entity);
            em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("[{}] Error updating {}: {}", getClass().getSimpleName(), entityClass.getSimpleName(), e.getMessage());
            throw new RuntimeException("Failed to update " + entityClass.getSimpleName(), e);
        }
    }

    public void delete(ID id) {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            em.getTransaction().begin();
            T entity = em.find(entityClass, id);
            if (entity != null) em.remove(entity);
            em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("[{}] Error deleting {} ID {}: {}", getClass().getSimpleName(), entityClass.getSimpleName(), id, e.getMessage());
            throw new RuntimeException("Failed to delete " + entityClass.getSimpleName(), e);
        }
    }

    // ================================================================
    // Transactional overloads — EntityManager provided by service layer
    // ================================================================

    /** Persists entity within a caller-managed transaction. */
    public void save(T entity, EntityManager em) {
        em.persist(entity);
    }

    /** Merges entity within a caller-managed transaction. */
    public void update(T entity, EntityManager em) {
        em.merge(entity);
    }

    /** Removes entity within a caller-managed transaction. */
    public void delete(ID id, EntityManager em) {
        T entity = em.find(entityClass, id);
        if (entity != null) em.remove(entity);
    }
}
