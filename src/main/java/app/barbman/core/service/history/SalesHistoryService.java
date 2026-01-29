package app.barbman.core.service.history;

import app.barbman.core.dto.history.ProductDetailDTO;
import app.barbman.core.dto.history.SaleDetailDTO;
import app.barbman.core.dto.history.SaleHistoryDTO;
import app.barbman.core.dto.history.ServiceDetailDTO;
import app.barbman.core.repositories.history.product.ProductHistoryRepository;
import app.barbman.core.repositories.history.service.ServiceHistoryRepository;
import app.barbman.core.repositories.sales.SaleRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.util.List;

/**
 * Handles sales history retrieval for table embed-views and detailed popups.
 * This service is READ-ONLY.
 */

public class SalesHistoryService {

    private static final Logger logger = LogManager.getLogger(SalesHistoryService.class);
    private static final String PREFIX = "[SALES-HISTORY-SERVICE]";

    private final SaleRepository saleRepository;
    private final ServiceHistoryRepository serviceHistoryRepository;
    private final ProductHistoryRepository productHistoryRepository;

    public SalesHistoryService(
            SaleRepository saleRepository,
            ServiceHistoryRepository serviceHistoryRepository,
            ProductHistoryRepository productHistoryRepository
    ) {
        this.saleRepository = saleRepository;
        this.serviceHistoryRepository = serviceHistoryRepository;
        this.productHistoryRepository = productHistoryRepository;
    }


    /**
     * Returns sales history for the main table (no details).
     */
    public List<SaleHistoryDTO> getSalesHistory(LocalDate from, LocalDate to) {

        logger.info("{} Fetching sales history from {} to {}", PREFIX, from, to);

        List<SaleHistoryDTO> list =
                saleRepository.findSalesHistory(from, to);

        logger.info("{} {} sales loaded for history table.", PREFIX, list.size());

        return list;
    }

    /**
     * Returns full detail of a sale, including services and products.
     */
    public SaleDetailDTO getSaleDetail(int saleId) {

        logger.info("{} Fetching sale detail -> saleId={}", PREFIX, saleId);

        SaleDetailDTO detail =
                saleRepository.findSaleHeaderDetail(saleId);

        if (detail == null) {
            logger.warn("{} Sale not found -> saleId={}", PREFIX, saleId);
            throw new IllegalArgumentException("Sale not found: " + saleId);
        }

        List<ServiceDetailDTO> services =
                serviceHistoryRepository.findServicesBySaleId(saleId);

        List<ProductDetailDTO> products =
                productHistoryRepository.findProductsBySaleId(saleId);

        detail.setServices(services);
        detail.setProducts(products);

        logger.info(
                "{} Sale detail loaded -> saleId={}, services={}, products={}",
                PREFIX,
                saleId,
                services.size(),
                products.size()
        );

        return detail;
    }
}

