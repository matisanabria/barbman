package app.barbman.core.service.sales.saleflow;

import app.barbman.core.dto.salecart.SaleCartDTO;
import app.barbman.core.dto.salecart.SaleCartItemDTO;
import app.barbman.core.model.cashbox.CashboxMovement;
import app.barbman.core.model.sales.Sale;
import app.barbman.core.model.sales.products.ProductHeader;
import app.barbman.core.model.sales.services.ServiceHeader;
import app.barbman.core.repositories.DbBootstrap;
import app.barbman.core.repositories.cashbox.movement.CashboxMovementRepository;
import app.barbman.core.repositories.sales.SaleRepository;
import app.barbman.core.service.cashbox.CashboxService;
import app.barbman.core.service.sales.products.ProductHeaderService;
import app.barbman.core.service.sales.products.ProductItemService;
import app.barbman.core.service.sales.products.ProductStockService;
import app.barbman.core.service.sales.services.ServiceHeaderService;
import app.barbman.core.service.sales.services.ServiceItemService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private final ProductStockService productStockService =
            new ProductStockService();
    private final CashboxMovementRepository cashboxMovementRepository;

    public SaleFlowService(
            SaleRepository saleRepository,
            ServiceHeaderService serviceHeaderService,
            ServiceItemService serviceItemService,
            ProductHeaderService productHeaderService,
            ProductItemService productItemService,
            CashboxMovementRepository cashboxMovementRepository
    ) {
        this.saleRepository = saleRepository;
        this.serviceHeaderService = serviceHeaderService;
        this.serviceItemService = serviceItemService;
        this.productHeaderService = productHeaderService;
        this.productItemService = productItemService;
        this.cashboxMovementRepository = cashboxMovementRepository;
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
    /**
     * Completa una venta con todos sus detalles.
     *
     * IMPORTANTE:
     * - Sale.userId = cart.getSelectedUserId() (el barbero seleccionado)
     * - ServiceHeader.userId = cart.getSelectedUserId() (el barbero seleccionado)
     * - Todo a nombre del usuario seleccionado, no del usuario de sesión
     */
    public Sale completeSale(SaleCartDTO cart) {

        try (Connection conn = DbBootstrap.connect()) {
            conn.setAutoCommit(false);

            // 1. Create Sale (root)
            // IMPORTANTE: Usar selectedUserId (el barbero), no userId (admin)
            Sale sale = new Sale(
                    cart.getSelectedUserId(),  // ← El barbero seleccionado
                    cart.getClientId(),
                    cart.getPaymentMethod(),
                    cart.getDate(),
                    cart.getTotal()
            );
            saleRepository.save(sale, conn); // set Id

            logger.info("{} Sale creada a nombre de usuario: {} (selectedUserId: {})",
                    PREFIX, cart.getSelectedUserId(), cart.getSelectedUserId());

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
            productStockService.decreaseStockFromCart(cart, conn);

            // 4. Commit
            conn.commit();
            logger.info("{} Sale completed successfully (saleId={}, userId={})",
                    PREFIX, sale.getId(), cart.getSelectedUserId());

            // 5. Cashbox movement (LEDGER)
            // El movimiento también se registra con el usuario seleccionado
            cashboxMovementRepository.save(new CashboxMovement(
                    "SALE",
                    "IN",
                    sale.getTotal(),
                    sale.getPaymentMethodId(),
                    "SALE",
                    sale.getId(),
                    "Sale registered",
                    cart.getSelectedUserId(),  // ← El barbero, no el admin
                    LocalDateTime.now()
            ));

            // 6. Clear cart
            cart.getCartItems().clear();
            return sale;

        } catch (Exception e) {
            logger.error("{} Sale failed, rolling back: {}", PREFIX, e.getMessage(), e);
            throw new RuntimeException("Sale could not be completed", e);
        }
    }
}