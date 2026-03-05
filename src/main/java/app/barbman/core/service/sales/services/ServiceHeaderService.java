package app.barbman.core.service.sales.services;

import app.barbman.core.dto.salecart.SaleCartDTO;
import app.barbman.core.dto.salecart.SaleCartItemDTO;
import app.barbman.core.model.sales.services.ServiceHeader;
import app.barbman.core.repositories.sales.services.serviceheader.ServiceHeaderRepository;
import jakarta.persistence.EntityManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;

public class ServiceHeaderService {

    private static final Logger logger = LogManager.getLogger(ServiceHeaderService.class);
    private static final String PREFIX = "[SERVICE-HEADER-SERVICE]";

    private final ServiceHeaderRepository serviceHeaderRepository;

    public ServiceHeaderService(ServiceHeaderRepository serviceHeaderRepository) {
        this.serviceHeaderRepository = serviceHeaderRepository;
    }

    public ServiceHeader createFromCart(SaleCartDTO cart, int saleId, EntityManager em) {
        double subtotal = calculateServicesSubtotal(cart);

        if (subtotal <= 0) {
            logger.debug("{} No service items found. Skipping ServiceHeader.", PREFIX);
            return null;
        }

        ServiceHeader header = ServiceHeader.builder()
                .userId(cart.getSelectedUserId())
                .saleId(saleId)
                .date(LocalDate.now())
                .subtotal(subtotal)
                .build();

        serviceHeaderRepository.save(header, em);

        logger.info("{} ServiceHeader created (ID={}, subtotal={})", PREFIX, header.getId(), subtotal);
        return header;
    }

    private double calculateServicesSubtotal(SaleCartDTO cart) {
        return cart.getCartItems().stream()
                .filter(item -> item.getType() == SaleCartItemDTO.ItemType.SERVICE)
                .mapToDouble(SaleCartItemDTO::getItemTotal)
                .sum();
    }

    public double getProductionByUserAndDateRange(int userId, LocalDate from, LocalDate to) {
        return serviceHeaderRepository.sumServiceTotalsByUserAndDateRange(userId, from, to);
    }
}
