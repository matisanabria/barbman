package app.barbman.core.service.sales.saleflow;

import app.barbman.core.dto.salecart.SaleCartDTO;
import app.barbman.core.dto.salecart.SaleCartItemDTO;
import app.barbman.core.model.sales.Sale;
import app.barbman.core.model.sales.products.ProductHeader;
import app.barbman.core.model.sales.services.ServiceHeader;
import app.barbman.core.repositories.DbBootstrap;
import app.barbman.core.repositories.sales.SaleRepository;
import app.barbman.core.service.sales.products.ProductHeaderService;
import app.barbman.core.service.sales.products.ProductItemService;
import app.barbman.core.service.sales.services.ServiceHeaderService;
import app.barbman.core.service.sales.services.ServiceItemService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.time.LocalDate;

/**
 * This service is a "Sales Flow Orchestrator".
 * <p>
 * It manages the entire sales process from start to finish,
 * Coordinates different services involved in a sale,
 * and connects frontend actions with backend operations.
 */
public class SaleFlowService {

    private static final Logger logger = LogManager.getLogger(SaleFlowService.class);
    private static final String PREFIX = "[SALE-FLOW]";

    private final SaleRepository saleRepository;
    private final ServiceHeaderService serviceHeaderService;
    private final ServiceItemService serviceItemService;
    private final ProductHeaderService productHeaderService;
    private final ProductItemService productItemService;

    public SaleFlowService(
            SaleRepository saleRepository,
            ServiceHeaderService serviceHeaderService,
            ServiceItemService serviceItemService,
            ProductHeaderService productHeaderService,
            ProductItemService productItemService
    ) {
        this.saleRepository = saleRepository;
        this.serviceHeaderService = serviceHeaderService;
        this.serviceItemService = serviceItemService;
        this.productHeaderService = productHeaderService;
        this.productItemService = productItemService;
    }

    //
    // CART OPERATIONS
    //
    public void addService(
            SaleCartDTO cart,
            int serviceDefinitionId,
            String name,
            double price
    ) {
        cart.addService(serviceDefinitionId, name, price);
    }

    public void addProduct(
            SaleCartDTO cart,
            int productId,
            String name,
            double price
    ) {
        cart.addProduct(productId, name, price);
    }

    public void removeSingleUnit(
            SaleCartDTO cart,
            SaleCartItemDTO item
    ) {
        cart.removeSingleUnit(item);
    }

    public void removeItem(
            SaleCartDTO cart,
            SaleCartItemDTO item
    ) {
        cart.removeItem(item);
    }

    public double calculateTotal(SaleCartDTO cart) {
        return cart.getTotal();
    }



    // ==========
    // PERSISTENCE
    // ==========
    public void completeSale(SaleCartDTO cart) {

        try (Connection conn = DbBootstrap.connect()) {
            conn.setAutoCommit(false);

            // 1. Create Sale (root)
            Sale sale = new Sale(
                    cart.getUserId(),
                    cart.getClientId(),
                    cart.getPaymentMethod(),
                    cart.getDate(),
                    cart.getTotal()
            );
            saleRepository.save(sale, conn);

            // 2. Services
            ServiceHeader serviceHeader =
                    serviceHeaderService.createFromCart(cart, sale.getId(), conn);

            serviceItemService.createItemsFromCart(
                    serviceHeader,
                    cart,
                    conn
            );

            // 3. Products
            ProductHeader productHeader =
                    productHeaderService.createFromCart(cart, sale.getId(), conn);

            productItemService.createItemsFromCart(
                    productHeader,
                    cart,
                    conn
            );

            // 4. Commit
            conn.commit();
            logger.info("{} Sale completed successfully (saleId={})",
                    PREFIX, sale.getId());

            // 5. Clear cart
            cart.getCartItems().clear();

        } catch (Exception e) {
            logger.error("{} Sale failed, rolling back: {}", PREFIX, e.getMessage(), e);
            throw new RuntimeException("Sale could not be completed", e);
        }
    }
//
//    public void deleteSale(int saleId) {
//
//        try (Connection conn = DbBootstrap.connect()) {
//            conn.setAutoCommit(false);
//
//            // 1.Services
//            serviceItemService.deleteBySaleId(saleId, conn);
//            serviceHeaderService.deleteBySaleId(saleId, conn);
//
//            // 2. Products
//            productItemService.deleteBySaleId(saleId, conn);
//            productHeaderService.deleteBySaleId(saleId, conn);
//
//            // 3. Stock rollback (si aplica)
//            productStockService.restoreStockBySaleId(saleId, conn);
//
//            // 4. Salary / production impact (opcional ahora)
//            salaryService.onSaleDeleted(saleId, conn);
//
//            // 5. Finally sale
//            saleRepository.delete(saleId, conn);
//
//            conn.commit();
//            logger.info("{} Sale fully deleted -> saleId={}", PREFIX, saleId);
//
//        } catch (Exception e) {
//            logger.error("{} Error deleting sale {}, rolling back", PREFIX, saleId, e);
//            throw new RuntimeException("Failed to delete sale", e);
//        }
//    }

}
