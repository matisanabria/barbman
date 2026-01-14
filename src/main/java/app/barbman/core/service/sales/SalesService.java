package app.barbman.core.service.sales;

import app.barbman.core.repositories.sales.SaleRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;

/**
 * Service for sales-related operations.
 */

public class SalesService {
    private static final Logger logger = LogManager.getLogger(SalesService.class);

    private final SaleRepository saleRepository;

    public SalesService(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    /**
     * Get total sales amount for a specific payment method within a date range.
     */
    public double getTotalForPaymentMethodInPeriod(
            int paymentMethodId,
            LocalDate start,
            LocalDate end
    ) {
        return saleRepository
                .sumTotalByPaymentMethodAndPeriod(
                        paymentMethodId, start, end
                );
    }
}
