package app.barbman.core.service.services;

import app.barbman.core.dto.sale.CartItemDTO;
import app.barbman.core.dto.sale.CheckoutDTO;
import app.barbman.core.model.services.Service;
import app.barbman.core.model.services.ServiceItem;
import app.barbman.core.repositories.services.service.ServiceRepository;
import app.barbman.core.repositories.services.serviceitems.ServiceItemRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Registers service sales using a shared DB transaction.
 * Responsible for:
 *  - Calculating the subtotal of SERVICE items
 *  - Creating the service header
 *  - Inserting individual service items (1 row per unit)
 *
 *  THIS METHOD ONLY REGISTERS SERVICES SOLD TO CUSTOMERS. AND HANDLES RELEVANT LOGIC FOR THAT.
 */
public class ServiceSaleService {

    private static final Logger logger = LogManager.getLogger(ServiceSaleService.class);

    private final ServiceRepository serviceRepository;
    private final ServiceItemRepository serviceItemRepository;

    public ServiceSaleService(ServiceRepository serviceRepository,
                              ServiceItemRepository serviceItemRepository) {
        this.serviceRepository = serviceRepository;
        this.serviceItemRepository = serviceItemRepository;
    }

    /**
     * Registers the sale of services.
     *
     * @param dto          checkout context
     * @param serviceItems list of CartItemDTO with type SERVICE
     * @param conn         shared database connection
     */
    public void registerServiceSale(CheckoutDTO dto,
                                    List<CartItemDTO> serviceItems,
                                    Connection conn) throws SQLException {

        logger.info("[SERVICE-SALE] Starting registration... items={}", serviceItems.size());

        // 1. Calculate subtotal only for services
        double subtotalServicios = serviceItems.stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();

        logger.debug("[SERVICE-SALE] Subtotal (services) = {}", subtotalServicios);

        // 2. Build and save service header
        Service service = new Service(
                dto.getUserId(),
                dto.getDate(),
                dto.getPaymentMethod(),
                subtotalServicios,    // ✔ total ONLY for services
                dto.getNotes(),
                dto.getClientId()
        );

        serviceRepository.save(service, conn);
        logger.info("[SERVICE-SALE] Header saved (ServiceID={})", service.getId());

        // 3. Insert rows for each unit
        for (CartItemDTO item : serviceItems) {
            for (int i = 0; i < item.getQuantity(); i++) {

                ServiceItem si = new ServiceItem(
                        service.getId(),
                        item.getDefinitionId(),  // fk to service_definitions
                        item.getPrice()
                );

                serviceItemRepository.save(si);
            }
        }

        logger.info("[SERVICE-SALE] {} ServiceItems inserted for ServiceID={}",
                serviceItems.size(), service.getId());

        // 4. Placeholder for future movement logging
        // movementService.registerIncome(...);

        logger.info("[SERVICE-SALE] Completed successfully.");
    }
}
