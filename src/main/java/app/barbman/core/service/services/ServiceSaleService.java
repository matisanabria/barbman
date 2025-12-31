package app.barbman.core.service.services;

import app.barbman.core.dto.sale.CartItemDTO;
import app.barbman.core.dto.sale.CheckoutDTO;
import app.barbman.core.model.sales.services.ServiceHeader;
import app.barbman.core.model.sales.services.ServiceItem;
import app.barbman.core.repositories.sales.services.serviceheader.ServiceHeaderRepository;
import app.barbman.core.repositories.sales.services.serviceitems.ServiceItemRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Registers serviceheader sales using a shared DB transaction.
 * Responsible for:
 *  - Calculating the subtotal of SERVICE items
 *  - Creating the serviceheader header which contains TOTAL
 *  - Inserting individual serviceheader items (1 row per unit)
 *
 *  THIS METHOD ONLY REGISTERS SERVICES SOLD TO CUSTOMERS. AND HANDLES RELEVANT LOGIC FOR THAT.
 */
public class ServiceSaleService {

    private static final Logger logger = LogManager.getLogger(ServiceSaleService.class);

    private final ServiceHeaderRepository serviceHeaderRepository;
    private final ServiceItemRepository serviceItemRepository;

    public ServiceSaleService(ServiceHeaderRepository serviceHeaderRepository,
                              ServiceItemRepository serviceItemRepository) {
        this.serviceHeaderRepository = serviceHeaderRepository;
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

        // 2. Build and save serviceheader header
        ServiceHeader serviceHeader = new ServiceHeader(
                dto.getUserId(),
                dto.getDate(),
                dto.getPaymentMethod(),
                subtotalServicios,    // ✔ total ONLY for services
                dto.getNotes(),
                dto.getClientId()
        );

        serviceHeaderRepository.save(serviceHeader, conn);
        logger.info("[SERVICE-SALE] Header saved (ServiceID={})", serviceHeader.getId());

        // 3. Insert rows for each unit
        for (CartItemDTO item : serviceItems) {
            for (int i = 0; i < item.getQuantity(); i++) {

                ServiceItem si = new ServiceItem(
                        serviceHeader.getId(),
                        item.getDefinitionId(),  // fk to service_definitions
                        item.getPrice()
                );

                serviceItemRepository.save(si);
            }
        }

        logger.info("[SERVICE-SALE] {} ServiceItems inserted for ServiceID={}",
                serviceItems.size(), serviceHeader.getId());

        // 4. Placeholder for future movement logging
        // movementService.registerIncome(...);

        logger.info("[SERVICE-SALE] Completed successfully.");
    }
}
