package app.barbman.core.dto.services;

import app.barbman.core.model.services.Service;
import app.barbman.core.model.services.ServiceItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for Service along with its associated ServiceItems.
 * Contains the main Service object and a list of ServiceItems.
 * Used for building a service with its items before saving to the database.
 */
public class ServiceDTO {
    private static final Logger logger = LogManager.getLogger(ServiceDTO.class);
    private static final String PREFIX = "[SERVICES-DTO]";

    private Service service; // The main service object
    private List<ServiceItem> items = new ArrayList<>(); // Items associated with the service

    // Base constructor
    public ServiceDTO(Service service) {
        this.service = service;
    }

    /**
     * Adds a ServiceItem to the DTO after validation.
     * @param item
     */
    public void addItem(ServiceItem item) {
        if (item == null) {
            logger.warn("{} Can't add a null item.", PREFIX);
            return;
        }

        if (item.getPrice() <= 0) {
            logger.warn("{} Invalid price: ({} Gs).", PREFIX, item.getPrice());
            return;
        }

        boolean exists = items.stream()
                .anyMatch(existing -> existing.getServiceTypeId() == item.getServiceTypeId());
        if (exists) {
            logger.warn("{} Service already added (serviceTypeId={}).", PREFIX, item.getServiceTypeId());
            return;
        }

        items.add(item);
        logger.info("{} Item added -> Tipo: {}, Price: {} Gs",
                PREFIX, item.getServiceTypeId(), item.getPrice());

        recalculateTotal();
    }


    /**
        * Removes a ServiceItem from the DTO.
     */
    public void removeItem(ServiceItem item) {
        if (item == null) return;

        items.remove(item);
        logger.info("{} Item removed (serviceTypeId={})", PREFIX, item.getServiceTypeId());
        recalculateTotal();
    }

    /**
     * Recalculates the total price based on the current items.
     * Updates the total in the main Service object.
     */
    public void recalculateTotal() {
        double total = items.stream().mapToDouble(ServiceItem::getPrice).sum();
        service.setTotal(total);

        logger.debug("{} Total -> {} Gs", PREFIX, total);
    }

    /**
     * Clears all items from the DTO and resets the total. (Useful for starting a new service)
     */
    public void clearItems() {
        items.clear();
        service.setTotal(0);
        logger.info("{} Lista de ítems limpiada.", PREFIX);
    }
    /**
     * Resets the entire DTO to a clean state.
     * Clears items, resets total, and optionally clears notes in the Service.
     */
    public void reset() {
        items.clear();
        if (service != null) {
            service.setTotal(0);
            service.setNotes(null);
        }
        logger.info("{} DTO reset to empty state.", PREFIX);
    }

    // Getters
    public double getTotal() {
        return service.getTotal();
    }

    public Service getService() {
        return service;
    }

    public List<ServiceItem> getItems() {
        return items;
    }

    /**
     * Verify if the DTO is ready to be saved.
     * Used on controller to check if you can proceed with saving.
     * @return true if ready, false otherwise
     */
    public boolean isReadyToSave() {
        boolean ready = service != null && !items.isEmpty() && service.getTotal() > 0;
        logger.debug("{} Estado de guardado: {}", PREFIX, ready ? "OK" : "INCOMPLETO");
        return ready;
    }
}
