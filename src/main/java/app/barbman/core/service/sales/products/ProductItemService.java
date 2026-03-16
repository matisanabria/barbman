package app.barbman.core.service.sales.products;

import app.barbman.core.dto.salecart.SaleCartDTO;
import app.barbman.core.dto.salecart.SaleCartItemDTO;
import app.barbman.core.model.sales.products.ProductHeader;
import app.barbman.core.model.sales.products.ProductSaleItem;
import app.barbman.core.repositories.sales.products.productsaleitem.ProductSaleItemRepository;
import jakarta.persistence.EntityManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProductItemService {

    private static final Logger logger = LogManager.getLogger(ProductItemService.class);
    private static final String PREFIX = "[PRODUCT-ITEM-SERVICE]";

    private final ProductSaleItemRepository productSaleItemRepository;

    public ProductItemService(ProductSaleItemRepository productSaleItemRepository) {
        this.productSaleItemRepository = productSaleItemRepository;
    }

    public void createItemsFromCart(ProductHeader header, SaleCartDTO cart, EntityManager em) {
        if (header == null) {
            logger.debug("{} No ProductHeader provided. Skipping ProductItems.", PREFIX);
            return;
        }

        for (SaleCartItemDTO item : cart.getCartItems()) {
            if (item.getType() != SaleCartItemDTO.ItemType.PRODUCT) continue;

            ProductSaleItem productItem = ProductSaleItem.builder()
                    .productHeaderId(header.getId())
                    .productId(item.getReferenceId())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .itemTotal(item.getItemTotal())
                    .build();

            productSaleItemRepository.save(productItem, em);
        }

        logger.info("{} ProductItems created for ProductHeader ID={}", PREFIX, header.getId());
    }
}
