package app.barbman.core.service.products;

import app.barbman.core.dto.sale.CartItemDTO;
import app.barbman.core.dto.sale.CheckoutDTO;
import app.barbman.core.model.sales.products.Product;
import app.barbman.core.model.sales.products.ProductHeader;
import app.barbman.core.model.sales.products.ProductSaleItem;
import app.barbman.core.repositories.sales.products.product.ProductRepository;
import app.barbman.core.repositories.sales.products.productheader.ProductHeaderRepository;
import app.barbman.core.repositories.sales.products.productsaleitem.ProductSaleItemRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Handles the process of registering a product sale.
 *
 * This serviceheader does NOT manage its own DB connections. All actions occur inside
 * the transaction created in CheckoutService to ensure atomicity for mixed checkouts
 * (products + services together).
 *
 * Steps:
 * 1. Insert header row in `product_sales`
 * 2. Insert one `product_sale_items` row per product (respecting quantity)
 * 3. Decrease stock accordingly
 * 4. (Future) Register a cash movement
 */
public class ProductSaleService {

    private static final Logger logger = LogManager.getLogger(ProductSaleService.class);

    private final ProductHeaderRepository saleRepository;
    private final ProductSaleItemRepository saleItemRepository;
    private final ProductRepository productRepository;

    public ProductSaleService(ProductHeaderRepository saleRepository,
                              ProductSaleItemRepository saleItemRepository,
                              ProductRepository productRepository) {
        this.saleRepository = saleRepository;
        this.saleItemRepository = saleItemRepository;
        this.productRepository = productRepository;
    }

    /**
     * Registers a product sale using a shared transaction.
     *
     * @param dto           The checkout header data (date, client, payment method, etc.)
     * @param productItems  Items of type PRODUCT extracted from the DTO
     * @param conn          Shared transaction connection
     */
    public void registerProductSale(CheckoutDTO dto,
                                    List<CartItemDTO> productItems,
                                    Connection conn) throws SQLException {

        logger.info("[PRODUCT-SALE] Starting product sale... items={}", productItems.size());

        // ======================
        // 1. Create sale header
        // ======================
        double subtotalProductos = calculateSubtotal(productItems);

        ProductHeader sale = new ProductHeader(
                dto.getDate(),
                subtotalProductos,
                dto.getPaymentMethod(),
                dto.getClientId()
        );

        saleRepository.save(sale, conn);
        logger.info("[PRODUCT-SALE] Created ProductHeader ID={}", sale.getId());

        // ======================
        // 2. Insert sale items
        // ======================
        for (CartItemDTO item : productItems) {

            Product product = productRepository.findById(item.getDefinitionId());

            if (product == null) {
                throw new SQLException("Product not found: ID=" + item.getDefinitionId());
            }

            // Insert item row
            ProductSaleItem psi = new ProductSaleItem(
                    sale.getId(),
                    product.getId(),
                    item.getQuantity(),
                    item.getPrice()
            );

            saleItemRepository.save(psi);
            logger.info("[PRODUCT-SALE] Added ProductSaleItem: product={}, qty={}", product.getName(), item.getQuantity());

            // ======================
            // 3. Decrease stock
            // ======================
            int newStock = product.getStock() - item.getQuantity();

            if (newStock < 0) {
                throw new SQLException("Insufficient stock for product: " + product.getName());
            }

            product.setStock(newStock);
            productRepository.update(product);
        }

        logger.info("[PRODUCT-SALE] All product items saved & stock updated.");

        // ======================
        // 4. Reserve space for movement registration
        // ======================
        // movementService.registerIncome(sale.getId(), subtotalProductos, dto.getPaymentMethod(), conn);

        logger.info("[PRODUCT-SALE] Product sale completed successfully.");
    }


    private double calculateSubtotal(List<CartItemDTO> items) {
        return items.stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();
    }
}
