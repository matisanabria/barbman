package app.barbman.core.service.sales;

import app.barbman.core.dto.history.SaleDetailDTO;
import app.barbman.core.dto.history.SaleHistoryDTO;
import app.barbman.core.dto.history.SaleItemDTO;
import app.barbman.core.repositories.cashbox.movement.CashboxMovementRepository;
import app.barbman.core.repositories.sales.SaleRepository;
import app.barbman.core.repositories.sales.products.product.ProductRepository;
import app.barbman.core.repositories.sales.products.productheader.ProductHeaderRepository;
import app.barbman.core.repositories.sales.products.productsaleitem.ProductSaleItemRepository;
import app.barbman.core.repositories.sales.services.servicedefinition.ServiceDefinitionRepository;
import app.barbman.core.repositories.sales.services.serviceheader.ServiceHeaderRepository;
import app.barbman.core.repositories.sales.services.serviceitems.ServiceItemRepository;
import app.barbman.core.util.legacy.LegacySaleRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for retrieving sales history and detailed sale information.
 */
public class SalesHistoryService {

    private static final Logger logger = LogManager.getLogger(SalesHistoryService.class);
    private static final String PREFIX = "[SALES-HISTORY-SERVICE]";

    private final SaleRepository saleRepo;
    private final ServiceHeaderRepository serviceHeaderRepo;
    private final ServiceItemRepository serviceItemRepo;
    private final ProductHeaderRepository productHeaderRepo;
    private final ProductSaleItemRepository productSaleItemRepo;
    private final ServiceDefinitionRepository serviceDefRepo;
    private final ProductRepository productRepo;

    private final CashboxMovementRepository movementRepo;
    private final LegacySaleRepository legacySaleRepository;

    public SalesHistoryService(
            SaleRepository saleRepo,
            ServiceHeaderRepository serviceHeaderRepo,
            ServiceItemRepository serviceItemRepo,
            ProductHeaderRepository productHeaderRepo,
            ProductSaleItemRepository productSaleItemRepo,
            ServiceDefinitionRepository serviceDefRepo,
            ProductRepository productRepo,
            CashboxMovementRepository movementRepo,
            LegacySaleRepository legacySaleRepository
    ) {
        this.saleRepo = saleRepo;
        this.serviceHeaderRepo = serviceHeaderRepo;
        this.serviceItemRepo = serviceItemRepo;
        this.productHeaderRepo = productHeaderRepo;
        this.productSaleItemRepo = productSaleItemRepo;
        this.serviceDefRepo = serviceDefRepo;
        this.productRepo = productRepo;
        this.movementRepo = movementRepo;
        this.legacySaleRepository = legacySaleRepository;
    }

    // ============================================================
    // SALES HISTORY
    // ============================================================

    /**
     * Returns all sales within a date range, including legacy beta data.
     */
    public List<SaleHistoryDTO> getSalesHistory(LocalDate from, LocalDate to) {
        logger.info("{} Fetching combined sales history from {} to {}", PREFIX, from, to);

        // 1. Obtener datos Legacy (BETA)
        // Van primero en la lista para que al hacer reverse en el controller queden al final (abajo)
        List<SaleHistoryDTO> legacySales = legacySaleRepository.searchByDateRange(from, to);
        logger.info("{} Retrieved {} legacy (beta) sales", PREFIX, legacySales.size());

        // 2. Obtener datos actuales
        List<SaleHistoryDTO> currentSales = saleRepo.findSalesHistory(from, to);
        logger.info("{} Retrieved {} current sales", PREFIX, currentSales.size());

        // 3. Mezclar en el orden solicitado
        List<SaleHistoryDTO> combinedHistory = new ArrayList<>(legacySales);
        combinedHistory.addAll(currentSales);

        return combinedHistory;
    }

    // ============================================================
    // SALE DETAIL
    // ============================================================

    /**
     * Returns complete detail of a sale including all items.
     */
    public SaleDetailDTO getSaleDetail(int saleId) {
        logger.info("{} Fetching detail for sale ID={}", PREFIX, saleId);

        // Get sale header
        SaleDetailDTO detail = saleRepo.findSaleHeaderDetail(saleId);

        if (detail == null) {
            logger.warn("{} Sale ID={} not found", PREFIX, saleId);
            return null;
        }

        // Get service items
        List<SaleItemDTO> serviceItems = getServiceItems(saleId);
        detail.setServiceItems(serviceItems);

        // Get product items
        List<SaleItemDTO> productItems = getProductItems(saleId);
        detail.setProductItems(productItems);

        logger.info("{} Sale detail loaded: {} services, {} products",
                PREFIX, serviceItems.size(), productItems.size());

        return detail;
    }

    /**
     * Gets all service items for a sale.
     */
    private List<SaleItemDTO> getServiceItems(int saleId) {
        logger.debug("{} [Using ServiceHeaderRepository] Fetching service items for sale {}", PREFIX, saleId);

        List<SaleItemDTO> items = new ArrayList<>();

        // Find service header by sale_id
        var serviceHeader = serviceHeaderRepo.findBySaleId(saleId);

        if (serviceHeader == null) {
            logger.debug("{} No service header found for sale {}", PREFIX, saleId);
            return items;
        }

        // Find all service items for this header
        var serviceItems = serviceItemRepo.findByServiceId(serviceHeader.getId());

        for (var item : serviceItems) {
            logger.debug("{} [Using ServiceDefinitionRepository] Fetching service definition {}",
                    PREFIX, item.getServiceDefinitionId());

            var serviceDef = serviceDefRepo.findById(item.getServiceDefinitionId());

            if (serviceDef != null) {
                SaleItemDTO dto = new SaleItemDTO();
                dto.setType("SERVICE");
                dto.setName(serviceDef.getName());
                dto.setQuantity(item.getQuantity());
                dto.setUnitPrice(item.getUnitPrice());
                dto.setTotal(item.getItemTotal());
                items.add(dto);
            }
        }

        return items;
    }

    /**
     * Gets all product items for a sale.
     */
    private List<SaleItemDTO> getProductItems(int saleId) {
        logger.debug("{} [Using ProductHeaderRepository] Fetching product items for sale {}", PREFIX, saleId);

        List<SaleItemDTO> items = new ArrayList<>();

        // Find product header by sale_id
        var productHeader = productHeaderRepo.findBySaleId(saleId);

        if (productHeader == null) {
            logger.debug("{} No product header found for sale {}", PREFIX, saleId);
            return items;
        }

        // Find all product items for this header
        var productItems = productSaleItemRepo.findBySaleId(productHeader.getId());

        for (var item : productItems) {
            logger.debug("{} [Using ProductRepository] Fetching product {}",
                    PREFIX, item.getProductId());

            var product = productRepo.findById(item.getProductId());

            if (product != null) {
                SaleItemDTO dto = new SaleItemDTO();
                dto.setType("PRODUCT");
                dto.setName(product.getName());
                dto.setQuantity(item.getQuantity());
                dto.setUnitPrice(item.getUnitPrice());
                dto.setTotal(item.getItemTotal());
                items.add(dto);
            }
        }

        return items;
    }

    // ============================================================
    // DELETE SALE
    // ============================================================

    /**
     * Deletes a sale (admin only - validation should be done in controller).
     * CASCADE will delete related service_header, product_sales, and their items.
     */
    public void deleteSale(int saleId) {
        logger.warn("{} Deleting sale ID={}", PREFIX, saleId);

        // 1. Borrar movimientos de caja relacionados
        logger.debug("{} [Using CashboxMovementRepository] Deleting movements for sale {}", PREFIX, saleId);
        var movements = movementRepo.findByReference("SALE", saleId);

        for (var movement : movements) {
            movementRepo.delete(movement.getId());
            logger.debug("{} Deleted movement ID={}", PREFIX, movement.getId());
        }

        logger.info("{} Deleted {} cashbox movements for sale {}", PREFIX, movements.size(), saleId);

        // 2. Borrar la venta (CASCADE borrará service_header, product_sales, items)
        saleRepo.delete(saleId);

        logger.info("{} Sale deleted (movements + sale + related records)", PREFIX);
    }
}