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
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
     * Returns sales history for the main table (no details).
     * Combines new and legacy sales, sorted by date DESC (most recent first).
     */
    public List<SaleHistoryDTO> getSalesHistory(LocalDate from, LocalDate to) {

        logger.info("{} Fetching sales history from {} to {}", PREFIX, from, to);

        // Get new sales (already sorted DESC in repository)
        List<SaleHistoryDTO> newSales = saleRepo.findSalesHistory(from, to);

        // Get legacy sales (NOT sorted in legacy repo)
        List<SaleHistoryDTO> legacySales = legacySaleRepository.searchByDateRange(from, to);

        // Merge and sort by date DESC, then by ID DESC
        List<SaleHistoryDTO> list = Stream.concat(newSales.stream(), legacySales.stream())
                .sorted((s1, s2) -> {
                    // Compare dates in descending order (most recent first)
                    int dateCompare = s2.getDate().compareTo(s1.getDate());
                    if (dateCompare != 0) {
                        return dateCompare;
                    }
                    // If same date, sort by ID descending
                    return Integer.compare(s2.getSaleId(), s1.getSaleId());
                })
                .collect(Collectors.toList());

        logger.info("{} {} sales loaded for history table (new: {}, legacy: {})",
                PREFIX, list.size(), newSales.size(), legacySales.size());

        return list;
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
     * Deletes a sale completely with all related data (admin only).
     * Manually deletes in correct order since CASCADE was removed.
     *
     * Deletion order:
     * 1. Service items (by service_header_id)
     * 2. Service headers (by sale_id)
     * 3. Product sale items (by product_header_id)
     * 4. Product sales (by sale_id)
     * 5. Cashbox movements (by reference to sale)
     * 6. Sale itself
     */
    public void deleteSaleComplete(int saleId) {
        logger.warn("{} Starting complete deletion of sale ID={}", PREFIX, saleId);

        try {
            // ==================================================
            // 1. DELETE SERVICE SIDE
            // ==================================================
            logger.debug("{} [Using ServiceHeaderRepository] Finding service header for sale {}",
                    PREFIX, saleId);

            var serviceHeader = serviceHeaderRepo.findBySaleId(saleId);

            if (serviceHeader != null) {
                logger.info("{} Found service header ID={} for sale {}",
                        PREFIX, serviceHeader.getId(), saleId);

                // 1.1 Delete service items first
                logger.debug("{} [Using ServiceItemRepository] Deleting items for header {}",
                        PREFIX, serviceHeader.getId());

                var serviceItems = serviceItemRepo.findByServiceId(serviceHeader.getId());
                logger.info("{} Found {} service items to delete", PREFIX, serviceItems.size());

                for (var item : serviceItems) {
                    serviceItemRepo.delete(item.getId());
                    logger.debug("{} Deleted service item ID={}", PREFIX, item.getId());
                }

                // 1.2 Delete service header
                serviceHeaderRepo.delete(serviceHeader.getId());
                logger.info("{} Deleted service header ID={}", PREFIX, serviceHeader.getId());

            } else {
                logger.debug("{} No service header found for sale {}", PREFIX, saleId);
            }

            // ==================================================
            // 2. DELETE PRODUCT SIDE
            // ==================================================
            logger.debug("{} [Using ProductHeaderRepository] Finding product sales for sale {}",
                    PREFIX, saleId);

            var productHeader = productHeaderRepo.findBySaleId(saleId);

            if (productHeader != null) {
                logger.info("{} Found product header ID={} for sale {}",
                        PREFIX, productHeader.getId(), saleId);

                // 2.1 Delete product sale items first
                logger.debug("{} [Using ProductSaleItemRepository] Deleting items for header {}",
                        PREFIX, productHeader.getId());

                var productItems = productSaleItemRepo.findBySaleId(productHeader.getId());
                logger.info("{} Found {} product items to delete", PREFIX, productItems.size());

                for (var item : productItems) {
                    productSaleItemRepo.delete(item.getId());
                    logger.debug("{} Deleted product item ID={}", PREFIX, item.getId());
                }

                // 2.2 Delete product header
                productHeaderRepo.delete(productHeader.getId());
                logger.info("{} Deleted product header ID={}", PREFIX, productHeader.getId());

            } else {
                logger.debug("{} No product header found for sale {}", PREFIX, saleId);
            }

            // ==================================================
            // 3. DELETE CASHBOX MOVEMENTS
            // ==================================================
            logger.debug("{} [Using CashboxMovementRepository] Deleting movements for sale {}",
                    PREFIX, saleId);

            var movements = movementRepo.findByReference("SALE", saleId);
            logger.info("{} Found {} cashbox movements to delete", PREFIX, movements.size());

            for (var movement : movements) {
                movementRepo.delete(movement.getId());
                logger.debug("{} Deleted cashbox movement ID={}", PREFIX, movement.getId());
            }

            // ==================================================
            // 4. DELETE SALE
            // ==================================================
            logger.debug("{} [Using SaleRepository] Deleting sale {}", PREFIX, saleId);
            saleRepo.delete(saleId);
            logger.info("{} Deleted sale ID={}", PREFIX, saleId);

            logger.info("{} ✓ Sale ID={} deleted successfully with all related data",
                    PREFIX, saleId);

        } catch (Exception e) {
            logger.error("{} ✗ Failed to delete sale ID={}", PREFIX, saleId, e);
            throw new RuntimeException("Error al eliminar la venta ID " + saleId + ": " + e.getMessage(), e);
        }
    }
}