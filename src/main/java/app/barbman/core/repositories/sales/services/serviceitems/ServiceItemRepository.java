package app.barbman.core.repositories.sales.services.serviceitems;

import app.barbman.core.model.sales.services.ServiceItem;
import jakarta.persistence.EntityManager;

import java.util.List;

public interface ServiceItemRepository {
    List<ServiceItem> findByServiceId(int serviceHeaderId);

    void save(ServiceItem item, EntityManager em);
    void update(ServiceItem item, EntityManager em);
    void delete(Integer id, EntityManager em);
    void deleteByHeaderId(int serviceHeaderId, EntityManager em);

    void delete(Integer id);
}
