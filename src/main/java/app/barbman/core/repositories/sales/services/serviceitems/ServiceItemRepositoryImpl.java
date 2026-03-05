package app.barbman.core.repositories.sales.services.serviceitems;

import app.barbman.core.infrastructure.HibernateUtil;
import app.barbman.core.model.sales.services.ServiceItem;
import app.barbman.core.repositories.AbstractHibernateRepository;
import jakarta.persistence.EntityManager;

import java.util.List;

public class ServiceItemRepositoryImpl extends AbstractHibernateRepository<ServiceItem, Integer>
        implements ServiceItemRepository {

    public ServiceItemRepositoryImpl() {
        super(ServiceItem.class);
    }

    @Override
    public List<ServiceItem> findByServiceId(int serviceHeaderId) {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            return em.createQuery(
                    "FROM ServiceItem WHERE serviceHeaderId = :headerId",
                    ServiceItem.class)
                    .setParameter("headerId", serviceHeaderId)
                    .getResultList();
        } catch (Exception e) {
            logger.error("[ServiceItemRepositoryImpl] Error fetching items for header {}: {}",
                    serviceHeaderId, e.getMessage());
            return List.of();
        }
    }

    @Override
    public void deleteByHeaderId(int serviceHeaderId, EntityManager em) {
        em.createQuery("DELETE FROM ServiceItem WHERE serviceHeaderId = :headerId")
                .setParameter("headerId", serviceHeaderId)
                .executeUpdate();
    }
}
