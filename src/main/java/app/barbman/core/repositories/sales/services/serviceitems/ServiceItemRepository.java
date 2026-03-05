package app.barbman.core.repositories.sales.services.serviceitems;

import app.barbman.core.model.sales.services.ServiceItem;
import app.barbman.core.repositories.GenericRepository;
import jakarta.persistence.EntityManager;

import java.util.List;

public interface ServiceItemRepository extends GenericRepository<ServiceItem, Integer> {
    List<ServiceItem> findByServiceId(int serviceHeaderId);

    void save(ServiceItem item, EntityManager em);
    void update(ServiceItem item, EntityManager em);
    void delete(Integer id, EntityManager em);
    void deleteByHeaderId(int serviceHeaderId, EntityManager em);
}
