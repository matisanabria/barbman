package app.barbman.core.service.sales.products;

import app.barbman.core.dto.salecart.SaleCartDTO;
import app.barbman.core.dto.salecart.SaleCartItemDTO;
import app.barbman.core.model.sales.products.ProductHeader;
import app.barbman.core.repositories.sales.products.productheader.ProductHeaderRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

public class ProductHeaderService {

    private static final Logger logger = LogManager.getLogger(ProductHeaderService.class);
    private static final String PREFIX = "[PRODUCT-HEADER-SERVICE]";

    private final ProductHeaderRepository productHeaderRepository;

    public ProductHeaderService(ProductHeaderRepository productHeaderRepository) {
        this.productHeaderRepository = productHeaderRepository;
    }

    /**
     * Creates and persists a ProductHeader based on the current cart.
     * Only PRODUCT items are considered.
     */
    public ProductHeader createFromCart(
            SaleCartDTO cart,
            int saleId,
            Connection conn
    ) throws SQLException {

        double subtotal = calculateProductsSubtotal(cart);

        // No products in cart → skip header
        if (subtotal <= 0) {
            logger.debug("{} No product items found. Skipping ProductHeader.", PREFIX);
            return null;
        }

        ProductHeader header = new ProductHeader(
                saleId,
                subtotal
        );

        productHeaderRepository.save(header, conn);

        logger.info("{} ProductHeader created (ID={}, subtotal={})",
                PREFIX, header.getId(), subtotal);

        return header;
    }

    /**
     * Calculates subtotal of PRODUCT items only.
     */
    private double calculateProductsSubtotal(SaleCartDTO cart) {
        return cart.getCartItems().stream()
                .filter(item -> item.getType() == SaleCartItemDTO.ItemType.PRODUCT)
                .mapToDouble(SaleCartItemDTO::getItemTotal)
                .sum();
    }
}
