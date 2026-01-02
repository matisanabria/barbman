package app.barbman.core.service.sales.products;

import app.barbman.core.dto.salecart.SaleCartDTO;
import app.barbman.core.dto.salecart.SaleCartItemDTO;
import app.barbman.core.model.sales.products.ProductHeader;
import app.barbman.core.model.sales.products.ProductSaleItem;
import app.barbman.core.repositories.sales.products.productsaleitem.ProductSaleItemRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;


public class ProductItemService {
    private static final Logger logger = LogManager.getLogger(ProductItemService.class);
    private static final String PREFIX = "[PRODUCT-ITEM-SERVICE]";

    private final ProductSaleItemRepository productSaleItemRepository;

    public ProductItemService(ProductSaleItemRepository productSaleItemRepository) {
        this.productSaleItemRepository = productSaleItemRepository;
    }

    /**
     * Creates and persists ProductSaleItems for a given ProductHeader,
     * based on PRODUCT items in the cart.
     */
    public void createItemsFromCart(
            ProductHeader header,
            SaleCartDTO cart,
            Connection conn
    ) throws SQLException {

        if (header == null) {
            logger.debug("{} No ProductHeader provided. Skipping ProductItems.", PREFIX);
            return;
        }

        for (SaleCartItemDTO item : cart.getCartItems()) {

            if (item.getType() != SaleCartItemDTO.ItemType.PRODUCT) continue;

            ProductSaleItem productItem = new ProductSaleItem(
                    header.getId(),              // product_header_id
                    item.getReferenceId(),       // product_id
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getItemTotal()
            );

            productSaleItemRepository.save(productItem, conn);
        }

        logger.info("{} ProductItems created for ProductHeader ID={}",
                PREFIX, header.getId());
    }
}
