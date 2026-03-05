package app.barbman.core.service.sales.saleflow;

import app.barbman.core.dto.salecart.SaleCartDTO;
import app.barbman.core.dto.salecart.SaleCartItemDTO;
import app.barbman.core.infrastructure.HibernateUtil;
import app.barbman.core.model.cashbox.CashboxMovement;
import app.barbman.core.model.sales.Sale;
import app.barbman.core.model.sales.products.ProductHeader;
import app.barbman.core.model.sales.services.ServiceHeader;
import app.barbman.core.repositories.cashbox.movement.CashboxMovementRepository;
import app.barbman.core.repositories.sales.SaleRepository;
import app.barbman.core.service.cashbox.CashboxService;
import app.barbman.core.service.sales.products.ProductHeaderService;
import app.barbman.core.service.sales.products.ProductItemService;
import app.barbman.core.service.sales.products.ProductStockService;
import app.barbman.core.service.sales.services.ServiceHeaderService;
import app.barbman.core.service.sales.services.ServiceItemService;
import jakarta.persistence.EntityManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Orchestrates the entire sale flow in a single JPA transaction.
 */
public class SaleFlowService {

    private static final Logger logger = LogManager.getLogger(SaleFlowService.class);
    private static final String PREFIX = "[SALE-FLOW]";

    private final SaleRepository saleRepository;
    private final ServiceHeaderService serviceHeaderService;
    private final ServiceItemService serviceItemService;
    private final ProductHeaderService productHeaderService;
    private final ProductItemService productItemService;
    private final ProductStockService productStockService = new ProductStockService();
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

    // ── Cart operations ──────────────────────────────────────────────────────

    public void addService(SaleCartDTO cart, int serviceDefinitionId, String name, double price) {
        cart.addService(serviceDefinitionId, name, price);
    }

    public void addProduct(SaleCartDTO cart, int productId, String name, double price) {
        cart.addProduct(productId, name, price);
    }

    public void removeSingleUnit(SaleCartDTO cart, SaleCartItemDTO item) {
        cart.removeSingleUnit(item);
    }

    public void removeItem(SaleCartDTO cart, SaleCartItemDTO item) {
        cart.removeItem(item);
    }

    public double calculateTotal(SaleCartDTO cart) {
        return cart.getTotal();
    }

    // ── Persistence ──────────────────────────────────────────────────────────

    public Sale completeSale(SaleCartDTO cart) {
        EntityManager em = HibernateUtil.createEntityManager();
        try {
            em.getTransaction().begin();

            // 1. Sale (root)
            Sale sale = Sale.builder()
                    .userId(cart.getSelectedUserId())
                    .clientId(cart.getClientId())
                    .paymentMethodId(cart.getPaymentMethod())
                    .date(cart.getDate())
                    .total(cart.getTotal())
                    .build();
            saleRepository.save(sale, em);

            logger.info("{} Sale created for userId={}", PREFIX, cart.getSelectedUserId());

            // 2. Services
            ServiceHeader serviceHeader = serviceHeaderService.createFromCart(cart, sale.getId(), em);
            serviceItemService.createItemsFromCart(serviceHeader, cart, em);

            // 3. Products
            ProductHeader productHeader = productHeaderService.createFromCart(cart, sale.getId(), em);
            productItemService.createItemsFromCart(productHeader, cart, em);
            productStockService.decreaseStockFromCart(cart, em);

            em.getTransaction().commit();
            logger.info("{} Sale completed (saleId={}, userId={})",
                    PREFIX, sale.getId(), cart.getSelectedUserId());

            // 4. Cashbox movement (outside main transaction — its own persist)
            cashboxMovementRepository.save(CashboxMovement.builder()
                    .movementType("SALE")
                    .direction("IN")
                    .amount(sale.getTotal())
                    .paymentMethodId(sale.getPaymentMethodId())
                    .referenceType("SALE")
                    .referenceId(sale.getId())
                    .description("Sale registered")
                    .userId(cart.getSelectedUserId())
                    .occurredAt(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .build());

            cart.getCartItems().clear();
            return sale;

        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            logger.error("{} Sale failed, rolled back: {}", PREFIX, e.getMessage(), e);
            throw new RuntimeException("Sale could not be completed", e);
        } finally {
            em.close();
        }
    }
}
