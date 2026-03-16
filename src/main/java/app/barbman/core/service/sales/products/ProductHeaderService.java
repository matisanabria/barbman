package app.barbman.core.service.sales.products;

import app.barbman.core.dto.salecart.SaleCartDTO;
import app.barbman.core.dto.salecart.SaleCartItemDTO;
import app.barbman.core.model.sales.products.ProductHeader;
import app.barbman.core.repositories.sales.products.productheader.ProductHeaderRepository;
import jakarta.persistence.EntityManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProductHeaderService {

    private static final Logger logger = LogManager.getLogger(ProductHeaderService.class);
    private static final String PREFIX = "[PRODUCT-HEADER-SERVICE]";

    private final ProductHeaderRepository productHeaderRepository;

    public ProductHeaderService(ProductHeaderRepository productHeaderRepository) {
        this.productHeaderRepository = productHeaderRepository;
    }

    public ProductHeader createFromCart(SaleCartDTO cart, int saleId, EntityManager em) {
        double subtotal = calculateProductsSubtotal(cart);

        if (subtotal <= 0) {
            logger.debug("{} No product items found. Skipping ProductHeader.", PREFIX);
            return null;
        }

        ProductHeader header = ProductHeader.builder()
                .saleId(saleId)
                .subtotal(subtotal)
                .build();
        productHeaderRepository.save(header, em);

        logger.info("{} ProductHeader created (ID={}, subtotal={})", PREFIX, header.getId(), subtotal);
        return header;
    }

    private double calculateProductsSubtotal(SaleCartDTO cart) {
        return cart.getCartItems().stream()
                .filter(item -> item.getType() == SaleCartItemDTO.ItemType.PRODUCT)
                .mapToDouble(SaleCartItemDTO::getItemTotal)
                .sum();
    }
}
