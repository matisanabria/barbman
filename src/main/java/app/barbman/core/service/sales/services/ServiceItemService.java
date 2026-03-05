package app.barbman.core.service.sales.services;

import app.barbman.core.dto.salecart.SaleCartDTO;
import app.barbman.core.dto.salecart.SaleCartItemDTO;
import app.barbman.core.model.sales.services.ServiceHeader;
import app.barbman.core.model.sales.services.ServiceItem;
import app.barbman.core.repositories.sales.services.serviceitems.ServiceItemRepository;
import jakarta.persistence.EntityManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServiceItemService {

    private static final Logger logger = LogManager.getLogger(ServiceItemService.class);
    private static final String PREFIX = "[SERVICE-ITEM-SERVICE]";

    private final ServiceItemRepository serviceItemRepository;

    public ServiceItemService(ServiceItemRepository serviceItemRepository) {
        this.serviceItemRepository = serviceItemRepository;
    }

    public void createItemsFromCart(ServiceHeader header, SaleCartDTO cart, EntityManager em) {
        if (header == null) {
            logger.debug("{} No ServiceHeader provided. Skipping ServiceItems.", PREFIX);
            return;
        }

        for (SaleCartItemDTO item : cart.getCartItems()) {
            if (item.getType() != SaleCartItemDTO.ItemType.SERVICE) continue;

            ServiceItem serviceItem = ServiceItem.builder()
                    .serviceHeaderId(header.getId())
                    .serviceDefinitionId(item.getReferenceId())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .itemTotal(item.getItemTotal())
                    .build();

            serviceItemRepository.save(serviceItem, em);
        }

        logger.info("{} ServiceItems created for ServiceHeader ID={}", PREFIX, header.getId());
    }
}
