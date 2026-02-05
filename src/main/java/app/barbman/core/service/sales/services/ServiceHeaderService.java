package app.barbman.core.service.sales.services;

import app.barbman.core.dto.salecart.SaleCartItemDTO;
import app.barbman.core.dto.salecart.SaleCartDTO;
import app.barbman.core.model.sales.services.ServiceHeader;
import app.barbman.core.repositories.sales.services.serviceheader.ServiceHeaderRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;


public class ServiceHeaderService {

    private static final Logger logger = LogManager.getLogger(ServiceHeaderService.class);

    private final ServiceHeaderRepository serviceHeaderRepository;
    private static final String PREFIX = "[SERVICE-HEADER-SERVICE]";

    public ServiceHeaderService(ServiceHeaderRepository serviceHeaderRepository) {
        this.serviceHeaderRepository = serviceHeaderRepository;
    }

    /**
     * Creates and persists a ServiceHeader based on the current cart.
     * Only SERVICE items are considered.
     */
    public ServiceHeader createFromCart(
            SaleCartDTO cart,
            int saleId,
            Connection conn
    ) throws SQLException {

        double subtotal = calculateServicesSubtotal(cart);

        // No services in cart, skip creating header
        if (subtotal <= 0) {
            logger.debug("{} No service items found. Skipping ServiceHeader.", PREFIX);
            return null;
        }

        ServiceHeader header = new ServiceHeader(
                cart.getSelectedUserId(),  // ← Uses selected user
                saleId,
                LocalDate.now(),
                subtotal
        );

        serviceHeaderRepository.save(header, conn);

        logger.info("{} ServiceHeader created (ID={}, subtotal={})",
                PREFIX, header.getId(), subtotal);

        return header;
    }
    /**
     * Calculates subtotal of SERVICE items only.
     */
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
