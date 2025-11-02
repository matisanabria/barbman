package app.barbman.core.repositories.services.servicedefinition;

import app.barbman.core.model.services.ServiceDefinition;
import app.barbman.core.repositories.GenericRepository;

import java.util.List;

public interface ServiceDefinitionRepository extends GenericRepository<ServiceDefinition, Integer> {
    public List<ServiceDefinition> findAllAvailable();
}
