package app.barbman.core.service.checkout;

import app.barbman.core.dto.sale.CartItemDTO;
import app.barbman.core.dto.sale.CheckoutDTO;
import app.barbman.core.repositories.DbBootstrap;
import app.barbman.core.service.products.ProductSaleService;
import app.barbman.core.service.services.ServiceSaleService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Coordinates the checkout process.
 *
 * CheckoutService never writes to the database directly.
 * It separates items by type and delegates persistence to:
 *   - ServiceSaleService  (services)
 *   - ProductSaleService  (products)
 *
 * It may optionally wrap both operations inside a single transaction
 * to ensure atomicity when a checkout includes mixed items.
 */
public class CheckoutService {

    private static final Logger logger = LogManager.getLogger(CheckoutService.class);

    private final ServiceSaleService serviceSaleService;
    private final ProductSaleService productSaleService;

    public CheckoutService(ServiceSaleService serviceSaleService,
                           ProductSaleService productSaleService) {
        this.serviceSaleService = serviceSaleService;
        this.productSaleService = productSaleService;
    }

    /**
     * Processes an entire checkout (services + products).
     * Delegates to the appropriate services using a shared DB transaction.
     */
    public void processCheckout(CheckoutDTO dto) {

        // Separate SERVICE and PRODUCT items
        List<CartItemDTO> serviceItems = dto.getCartItems().stream()
                .filter(i -> i.getType() == CartItemDTO.ItemType.SERVICE)
                .collect(Collectors.toList());

        List<CartItemDTO> productItems = dto.getCartItems().stream()
                .filter(i -> i.getType() == CartItemDTO.ItemType.PRODUCT)
                .collect(Collectors.toList());

        logger.info("[CHECKOUT] Starting checkout - services={}, products={}",
                serviceItems.size(), productItems.size());

        try (Connection conn = DbBootstrap.connect()) {
            conn.setAutoCommit(false); // Begin transaction

            if (!serviceItems.isEmpty()) {
                serviceSaleService.registerServiceSale(dto, serviceItems, conn);
            }

            if (!productItems.isEmpty()) {
                productSaleService.registerProductSale(dto, productItems, conn);
            }

            conn.commit();
            logger.info("[CHECKOUT] Checkout completed successfully.");

        } catch (Exception e) {
            logger.error("[CHECKOUT] Failed: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}
