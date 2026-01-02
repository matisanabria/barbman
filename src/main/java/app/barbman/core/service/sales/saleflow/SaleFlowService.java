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

    private SaleCartDTO currentSale;

    // ==========
    // FRONTEND
    // ==========
    /**
     * Starts a new sale flow.
     * Only user and date are required at this stage.
     */
    public void startSale(int userId) {
        this.currentSale = new SaleCartDTO(userId);
        this.currentSale.setDate(LocalDate.now());
    }

    /**
     * Cancels the current sale flow.
     */
    public void cancelSale() {
        this.currentSale = null;
    }

    /**
     * Returns true if there is an active sale.
     */
    public boolean hasActiveSale() {
        return currentSale != null;
    }

    /**
     * Returns the current sale cart.
     * Throws if no sale is active if there's a programming error.
     */
    public SaleCartDTO getCurrentSale() {
        if (currentSale == null) {
            throw new IllegalStateException("No active sale.");
        }
        return currentSale;
    }

    //
    // CART OPERATIONS
    //
    public void addService(int serviceDefinitionId, String name, double price) {
        getCurrentSale().addService(serviceDefinitionId, name, price); // Delegates to SaleCartDTO
    }

    public void addProduct(int productId, String name, double price) {
        getCurrentSale().addProduct(productId, name, price); // getCurrentSale() is the SaleCartDTO instance
    }

    public void removeSingleUnitFromCart(SaleCartItemDTO cartItem) {
        getCurrentSale().removeSingleUnit((app.barbman.core.dto.salecart.SaleCartItemDTO) cartItem);
    }

    public void removeItemFromCart(SaleCartItemDTO cartItem) {
        getCurrentSale().removeItem((app.barbman.core.dto.salecart.SaleCartItemDTO) cartItem);
    }

    public double getCurrentTotal() {
        return getCurrentSale().getTotal();
    }


    // ==========
    // BACKEND
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

}
