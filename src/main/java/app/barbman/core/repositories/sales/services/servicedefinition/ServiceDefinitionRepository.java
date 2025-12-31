package app.barbman.core.repositories.sales.services.servicedefinition;

import app.barbman.core.model.sales.services.ServiceDefinition;
import app.barbman.core.repositories.GenericRepository;

import java.util.List;

public interface ServiceDefinitionRepository extends GenericRepository<ServiceDefinition, Integer> {
    public List<ServiceDefinition> findAllAvailable();
}
