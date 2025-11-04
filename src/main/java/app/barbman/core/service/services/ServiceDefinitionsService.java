package app.barbman.core.service.services;

import app.barbman.core.model.services.ServiceDefinition;
import app.barbman.core.repositories.services.servicedefinition.ServiceDefinitionRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Handles logic related to service definitions (e.g. haircut, beard trim, combos).
 * Provides CRUD operations and acts as the abstraction between controller and repository.
 */
public class ServiceDefinitionsService {

    private static final Logger logger = LogManager.getLogger(ServiceDefinitionsService.class);
    private static final String PREFIX = "[SERVDEF-SERVICE]";

    private final ServiceDefinitionRepository serviceDefinitionRepository;

    public ServiceDefinitionsService(ServiceDefinitionRepository serviceDefinitionRepository) {
        this.serviceDefinitionRepository = serviceDefinitionRepository;
    }

    /**
     * Fetch all service definitions from the repository.
     */
    public List<ServiceDefinition> getAll() {
        logger.info("{} Fetching all service definitions...", PREFIX);
        List<ServiceDefinition> defs = serviceDefinitionRepository.findAll();
        logger.info("{} {} service definitions loaded.", PREFIX, defs.size());
        return defs;
    }

    /**
     * Find a single service definition by ID.
     */
    public ServiceDefinition getById(int id) {
        return serviceDefinitionRepository.findById(id);
    }

    /**
     * Create or update a service definition.
     */
    public void save(ServiceDefinition def) {
        if (def == null) {
            logger.warn("{} Attempted to save null service definition.", PREFIX);
            throw new IllegalArgumentException("Service definition cannot be null");
        }
        serviceDefinitionRepository.save(def);
        logger.info("{} Service definition saved -> {}", PREFIX, def.getName());
    }

    /**
     * Update an existing service definition.
     */
    public void update(ServiceDefinition def) {
        if (def == null || def.getId() == 0) {
            logger.warn("{} Attempted to update invalid service definition.", PREFIX);
            throw new IllegalArgumentException("Invalid service definition for update");
        }
        serviceDefinitionRepository.update(def);
        logger.info("{} Service definition updated -> ID {}", PREFIX, def.getId());
    }

    /**
     * Delete a service definition by ID.
     */
    public void delete(int id) {
        serviceDefinitionRepository.delete(id);
        logger.info("{} Service definition deleted -> ID {}", PREFIX, id);
    }
}
