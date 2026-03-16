package app.barbman.core.service.cashbox;

import app.barbman.core.dto.CashboxReportDTO;
import app.barbman.core.model.cashbox.CashboxMovement;
import app.barbman.core.model.cashbox.CashboxOpening;
import app.barbman.core.model.human.User;
import app.barbman.core.repositories.cashbox.movement.CashboxMovementRepository;
import app.barbman.core.repositories.cashbox.opening.CashboxOpeningRepository;
import app.barbman.core.repositories.sales.products.productheader.ProductHeaderRepository;
import app.barbman.core.repositories.sales.services.serviceheader.ServiceHeaderRepository;
import app.barbman.core.repositories.users.UsersRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for generating cashbox reports (daily, weekly, monthly).
 */
public class CashboxReportService {

    private static final Logger logger = LogManager.getLogger(CashboxReportService.class);
    private static final String PREFIX = "[CASHBOX-REPORT]";

    private final CashboxMovementRepository movementRepo;
    private final ServiceHeaderRepository serviceHeaderRepo;
    private final ProductHeaderRepository productHeaderRepo;
    private final UsersRepository usersRepo;
    private final CashboxOpeningRepository openingRepo;

    public CashboxReportService(
            CashboxMovementRepository movementRepo,
            ServiceHeaderRepository serviceHeaderRepo,
            ProductHeaderRepository productHeaderRepo,
            UsersRepository usersRepo,
            CashboxOpeningRepository openingRepo
    ) {
        this.movementRepo = movementRepo;
        this.serviceHeaderRepo = serviceHeaderRepo;
        this.productHeaderRepo = productHeaderRepo;
        this.usersRepo = usersRepo;
        this.openingRepo = openingRepo;
    }

    // ============================================================
    // CURRENT PERIOD REPORT (based on open cashbox)
    // ============================================================

    public CashboxReportDTO getCurrentPeriodReport() {
        CashboxOpening opening = openingRepo.findCurrentOpen();
        if (opening == null) {
            return emptyReport();
        }
        return generateReportForOpening(opening);
    }

    // ============================================================
    // DATE-RANGE REPORTS
    // ============================================================

    public CashboxReportDTO getDailyReport(LocalDate date) {
        logger.info("{} Generating daily report for {}", PREFIX, date);

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

        return generateDateRangeReport(date, date, start, end);
    }

    public CashboxReportDTO getWeeklyReport(LocalDate weekStart) {
        logger.info("{} Generating weekly report for week starting {}", PREFIX, weekStart);

        LocalDate weekEnd = weekStart.plusDays(6);
        LocalDateTime start = weekStart.atStartOfDay();
        LocalDateTime end = weekEnd.atTime(23, 59, 59);

        return generateDateRangeReport(weekStart, weekEnd, start, end);
    }

    public CashboxReportDTO getMonthlyReport(YearMonth month) {
        logger.info("{} Generating monthly report for {}", PREFIX, month);

        LocalDate monthStart = month.atDay(1);
        LocalDate monthEnd = month.atEndOfMonth();

        LocalDateTime start = monthStart.atStartOfDay();
        LocalDateTime end = monthEnd.atTime(23, 59, 59);

        return generateDateRangeReport(monthStart, monthEnd, start, end);
    }

    // ============================================================
    // CORE LOGIC — opening-based report
    // ============================================================

    private CashboxReportDTO generateReportForOpening(CashboxOpening opening) {
        List<CashboxMovement> movements = movementRepo.findByOpeningId(opening.getId());

        CashboxReportDTO report = new CashboxReportDTO();
        report.setPeriodStart(opening.getOpenedAt().toLocalDate());
        report.setPeriodEnd(LocalDate.now());

        calculateMovementTotals(report, movements, opening.getCashAmount(), opening.getBankAmount());
        calculateProductionByUser(report, opening.getOpenedAt().toLocalDate(), LocalDate.now());

        return report;
    }

    // ============================================================
    // CORE LOGIC — date-range report
    // ============================================================

    private CashboxReportDTO generateDateRangeReport(
            LocalDate periodStart,
            LocalDate periodEnd,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd
    ) {
        List<CashboxMovement> movements = movementRepo.findByDateRange(rangeStart, rangeEnd);

        // Filter out movements with null paymentMethodId
        movements = movements.stream()
                .filter(m -> m.getPaymentMethodId() != null)
                .collect(Collectors.toList());

        CashboxReportDTO report = new CashboxReportDTO();
        report.setPeriodStart(periodStart);
        report.setPeriodEnd(periodEnd);

        // Find the most recent opening at or before the period start
        double initialCash = 0;
        double initialBank = 0;

        List<CashboxOpening> allOpenings = openingRepo.findAll();
        CashboxOpening relevantOpening = allOpenings.stream()
                .filter(o -> !o.getOpenedAt().toLocalDate().isAfter(periodEnd))
                .max((a, b) -> a.getOpenedAt().compareTo(b.getOpenedAt()))
                .orElse(null);

        if (relevantOpening != null) {
            initialCash = relevantOpening.getCashAmount();
            initialBank = relevantOpening.getBankAmount();
        }

        calculateMovementTotals(report, movements, initialCash, initialBank);
        calculateProductionByUser(report, periodStart, periodEnd);

        return report;
    }

    // ============================================================
    // SHARED CALCULATION
    // ============================================================

    private void calculateMovementTotals(
            CashboxReportDTO report,
            List<CashboxMovement> movements,
            double initialCash,
            double initialBank
    ) {
        double cashIn = 0, cashOut = 0, bankIn = 0, bankOut = 0;

        for (CashboxMovement m : movements) {
            Integer paymentMethodId = m.getPaymentMethodId();
            if (paymentMethodId == null) continue;

            double amount = m.getAmount();

            if (paymentMethodId == 0) {
                if ("IN".equals(m.getDirection())) cashIn += amount;
                else if ("OUT".equals(m.getDirection())) cashOut += amount;
            } else if (paymentMethodId >= 1 && paymentMethodId <= 3) {
                if ("IN".equals(m.getDirection())) bankIn += amount;
                else if ("OUT".equals(m.getDirection())) bankOut += amount;
            }
        }

        report.setCashIn(cashIn);
        report.setCashOut(cashOut);
        report.setCashBalance(initialCash + cashIn - cashOut);

        report.setBankIn(bankIn);
        report.setBankOut(bankOut);
        report.setBankBalance(initialBank + bankIn - bankOut);

        report.setTotalIn(cashIn + bankIn);
        report.setTotalOut(cashOut + bankOut);
        report.setTotalBalance((initialCash + initialBank) + (cashIn + bankIn) - (cashOut + bankOut));
    }

    // ============================================================
    // PRODUCTION (services + products)
    // ============================================================

    private void calculateProductionByUser(CashboxReportDTO report, LocalDate start, LocalDate end) {
        List<User> users = usersRepo.findAll().stream()
                .filter(u -> "user".equals(u.getRole()) || "admin".equals(u.getRole()))
                .collect(Collectors.toList());

        for (User user : users) {
            double serviceProduction = serviceHeaderRepo.sumServiceTotalsByUserAndDateRange(
                    user.getId(), start, end);

            double productProduction = productHeaderRepo.sumProductTotalsByUserAndDateRange(
                    user.getId(), start, end);

            double totalProduction = serviceProduction + productProduction;

            report.getProductionByUser().put(user.getId(), totalProduction);
            report.getUserNames().put(user.getId(), user.getName());
        }
    }

    private CashboxReportDTO emptyReport() {
        CashboxReportDTO report = new CashboxReportDTO();
        report.setPeriodStart(LocalDate.now());
        report.setPeriodEnd(LocalDate.now());
        return report;
    }
}
