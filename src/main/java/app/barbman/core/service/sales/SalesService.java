package app.barbman.core.service.sales;

import app.barbman.core.repositories.sales.SaleRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

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

    /**
     * Get total sales for today.
     */
    public double getTodayTotal() {
        LocalDate today = LocalDate.now();
        logger.info("[SALES-SERVICE] Getting TODAY total for: {}", today);
        double total = saleRepository.sumTotalByPeriod(today, today);
        logger.info("[SALES-SERVICE] TODAY total = {}", total);
        return total;
    }

    /**
     * Get total sales for current week (Monday to Sunday).
     */
    public double getWeekTotal() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(
                java.time.DayOfWeek.MONDAY
        ));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(
                java.time.DayOfWeek.SUNDAY
        ));

        logger.info("[SALES-SERVICE] Getting WEEK total [{} -> {}]", startOfWeek, endOfWeek);
        double total = saleRepository.sumTotalByPeriod(startOfWeek, endOfWeek);
        logger.info("[SALES-SERVICE] WEEK total = {}", total);
        return total;
    }

    /**
     * Get total sales for current month.
     */
    public double getMonthTotal() {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());

        logger.info("[SALES-SERVICE] Getting MONTH total [{} -> {}]", startOfMonth, endOfMonth);
        double total = saleRepository.sumTotalByPeriod(startOfMonth, endOfMonth);
        logger.info("[SALES-SERVICE] MONTH total = {}", total);
        return total;
    }
}