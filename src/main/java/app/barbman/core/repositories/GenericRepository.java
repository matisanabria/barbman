package app.barbman.core.repositories;

import java.util.List;

/**
 * Base CRUD interface for all repositories.
 * Implementations will migrate from raw JDBC to Hibernate EntityManager progressively.
 */
public interface GenericRepository<T, ID> {
    T findById(ID id);
    List<T> findAll();
    void save(T entity);
    void update(T entity);
    void delete(ID id);
}
