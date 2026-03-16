package app.barbman.core.repositories.sales.services.servicedefinition;

import app.barbman.core.infrastructure.HibernateUtil;
import app.barbman.core.model.sales.services.ServiceDefinition;
import app.barbman.core.repositories.AbstractHibernateRepository;
import jakarta.persistence.EntityManager;

import java.util.List;

public class ServiceDefinitionRepositoryImpl extends AbstractHibernateRepository<ServiceDefinition, Integer>
        implements ServiceDefinitionRepository {

    public ServiceDefinitionRepositoryImpl() {
        super(ServiceDefinition.class);
    }

    @Override
    public List<ServiceDefinition> findAllAvailable() {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            return em.createQuery("FROM ServiceDefinition WHERE available = true", ServiceDefinition.class)
                    .getResultList();
        } catch (Exception e) {
            logger.error("[ServiceDefinitionRepositoryImpl] Error fetching available services: {}", e.getMessage());
            return List.of();
        }
    }
}
