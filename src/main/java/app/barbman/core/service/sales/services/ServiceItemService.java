package app.barbman.core.service.sales.services;

import app.barbman.core.dto.salecart.SaleCartDTO;
import app.barbman.core.dto.salecart.SaleCartItemDTO;
import app.barbman.core.model.sales.services.ServiceHeader;
import app.barbman.core.model.sales.services.ServiceItem;
import app.barbman.core.repositories.sales.services.serviceitems.ServiceItemRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

public class ServiceItemService {
    private static final Logger logger = LogManager.getLogger(ServiceItemService.class);
    private static final String PREFIX = "[SERVICE-ITEM-SERVICE]";

    private final ServiceItemRepository serviceItemRepository;

    public ServiceItemService(ServiceItemRepository serviceItemRepository) {
        this.serviceItemRepository = serviceItemRepository;
    }

    /**
     * Creates and persists ServiceItems for a given ServiceHeader,
     * based on SERVICE items in the cart.
     */
    public void createItemsFromCart(
            ServiceHeader header,
            SaleCartDTO cart,
            Connection conn
    ) throws SQLException {

        if (header == null) {
            logger.debug("{} No ServiceHeader provided. Skipping ServiceItems.", PREFIX);
            return;
        }

        for (SaleCartItemDTO item : cart.getCartItems()) {

            if (item.getType() != SaleCartItemDTO.ItemType.SERVICE) continue;

            ServiceItem serviceItem = new ServiceItem(
                    header.getId(),
                    item.getReferenceId(),          // service_definition_id
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getItemTotal()
            );

            serviceItemRepository.save(serviceItem, conn);
        }

        logger.info("{} ServiceItems created for ServiceHeader ID={}",
                PREFIX, header.getId());
    }
}
