package app.barbman.core.repositories.sales.services.servicedefinition;

import app.barbman.core.model.sales.services.ServiceDefinition;

import java.util.List;

public interface ServiceDefinitionRepository {
    List<ServiceDefinition> findAllAvailable();

    ServiceDefinition findById(Integer id);
    List<ServiceDefinition> findAll();
    void save(ServiceDefinition entity);
    void update(ServiceDefinition entity);
    void delete(Integer id);
}
