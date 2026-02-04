package app.barbman.core.service.sales.services;

import app.barbman.core.model.sales.services.ServiceDefinition;
import app.barbman.core.repositories.sales.services.servicedefinition.ServiceDefinitionRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Handles logic related to services already defined in the system.
 * Those services can then be used when registering serviceheader sales.
 *
 * These predefined services are not the services sold to customers, only their definitions.
 *
 * THIS METHOD HANDLES TYPES OF SERVICES SUCH AS HAIRCUTS, SHAVES, ETC.
 */
public class ServiceDefinitionsService {

    private static final Logger logger = LogManager.getLogger(ServiceDefinitionsService.class);
    private static final String PREFIX = "[SERVDEF-SERVICE]";

    private final ServiceDefinitionRepository serviceDefinitionRepository;

    public ServiceDefinitionsService(ServiceDefinitionRepository serviceDefinitionRepository) {
        this.serviceDefinitionRepository = serviceDefinitionRepository;
    }

    /**
     * Fetch all serviceheader definitions from the repository.
     */
    public List<ServiceDefinition> getAll() {
        logger.info("{} Fetching all serviceheader definitions...", PREFIX);
        List<ServiceDefinition> defs = serviceDefinitionRepository.findAll();
        logger.info("{} {} serviceheader definitions loaded.", PREFIX, defs.size());
        return defs;
    }

    /**
     * Find a single serviceheader definition by ID.
     */
    public ServiceDefinition getById(int id) {
        return serviceDefinitionRepository.findById(id);
    }

    /**
     * Create or update a serviceheader definition.
     */
    public void save(ServiceDefinition def) {
        if (def == null) {
            logger.warn("{} Attempted to save null serviceheader definition.", PREFIX);
            throw new IllegalArgumentException("ServiceHeader definition cannot be null");
        }
        serviceDefinitionRepository.save(def);
        logger.info("{} ServiceHeader definition saved -> {}", PREFIX, def.getName());
    }

    /**
     * Update an existing serviceheader definition.
     */
    public void update(ServiceDefinition def) {
        if (def == null || def.getId() == 0) {
            logger.warn("{} Attempted to update invalid serviceheader definition.", PREFIX);
            throw new IllegalArgumentException("Invalid serviceheader definition for update");
        }
        serviceDefinitionRepository.update(def);
        logger.info("{} ServiceHeader definition updated -> ID {}", PREFIX, def.getId());
    }

    /**
     * Delete a serviceheader definition by ID.
     */
    public void delete(int id) {
        serviceDefinitionRepository.delete(id);
        logger.info("{} ServiceHeader definition deleted -> ID {}", PREFIX, id);
    }
    public void softDelete(int id) {
        ServiceDefinition service = serviceDefinitionRepository.findById(id);
        if (service != null) {
            service.setAvailable(false);  // Soft delete
            serviceDefinitionRepository.update(service);
            logger.info("{} Service soft deleted (set unavailable) -> ID {}", PREFIX, id);
        }
    }
}
