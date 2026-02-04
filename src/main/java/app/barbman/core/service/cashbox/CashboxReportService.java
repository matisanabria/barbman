package app.barbman.core.service.cashbox;

import app.barbman.core.dto.CashboxReportDTO;
import app.barbman.core.model.cashbox.CashboxMovement;
import app.barbman.core.model.cashbox.CashboxOpening;
import app.barbman.core.model.human.User;
import app.barbman.core.repositories.cashbox.movement.CashboxMovementRepository;
import app.barbman.core.repositories.cashbox.opening.CashboxOpeningRepository;
import app.barbman.core.repositories.sales.services.serviceheader.ServiceHeaderRepository;
import app.barbman.core.repositories.users.UsersRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for generating cashbox reports (daily, weekly, monthly).
 */
public class CashboxReportService {

    private static final Logger logger = LogManager.getLogger(CashboxReportService.class);
    private static final String PREFIX = "[CASHBOX-REPORT]";

    private final CashboxMovementRepository movementRepo;
    private final ServiceHeaderRepository serviceHeaderRepo;
    private final UsersRepository usersRepo;
    private final CashboxOpeningRepository openingRepo;

    public CashboxReportService(
            CashboxMovementRepository movementRepo,
            ServiceHeaderRepository serviceHeaderRepo,
            UsersRepository usersRepo,
            CashboxOpeningRepository openingRepo
    ) {
        this.movementRepo = movementRepo;
        this.serviceHeaderRepo = serviceHeaderRepo;
        this.usersRepo = usersRepo;
        this.openingRepo = openingRepo;
    }

    // ============================================================
    // DAILY REPORT
    // ============================================================

    public CashboxReportDTO getDailyReport(LocalDate date) {
        logger.info("{} Generating daily report for {}", PREFIX, date);

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

        return generateReport(date, date, start, end);
    }

    // ============================================================
    // WEEKLY REPORT
    // ============================================================

    public CashboxReportDTO getWeeklyReport(LocalDate weekStart) {
        logger.info("{} Generating weekly report for week starting {}", PREFIX, weekStart);

        LocalDate weekEnd = weekStart.plusDays(6);
        LocalDateTime start = weekStart.atStartOfDay();
        LocalDateTime end = weekEnd.atTime(23, 59, 59);

        return generateReport(weekStart, weekEnd, start, end);
    }

    // ============================================================
    // MONTHLY REPORT
    // ============================================================

    public CashboxReportDTO getMonthlyReport(YearMonth month) {
        logger.info("{} Generating monthly report for {}", PREFIX, month);

        LocalDate monthStart = month.atDay(1);
        LocalDate monthEnd = month.atEndOfMonth();

        LocalDateTime start = monthStart.atStartOfDay();
        LocalDateTime end = monthEnd.atTime(23, 59, 59);

        return generateReport(monthStart, monthEnd, start, end);
    }

    // ============================================================
    // CORE LOGIC
    // ============================================================

    private CashboxReportDTO generateReport(
            LocalDate periodStart,
            LocalDate periodEnd,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd
    ) {
        // Fetch movements for the period (may be recalculated later for monthly reports)
        List<CashboxMovement> movements = movementRepo.findByDateRange(rangeStart, rangeEnd);

        CashboxReportDTO report = new CashboxReportDTO();
        report.setPeriodStart(periodStart);
        report.setPeriodEnd(periodEnd);

        // Fetch cashbox opening to calculate initial balance
        logger.debug("{} [Using CashboxOpeningRepository] Fetching opening for period", PREFIX);
        double initialCash = 0;
        double initialBank = 0;

        // Determine if this is a daily/weekly or monthly report
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(periodStart, periodEnd);

        if (daysBetween <= 6) {
            // Daily or weekly report: find the opening for that specific week
            LocalDate weekStart = periodStart.with(java.time.DayOfWeek.MONDAY);
            var opening = openingRepo.findByPeriodStart(weekStart);

            if (opening != null) {
                initialCash = opening.getCashAmount();
                initialBank = opening.getBankAmount();
                logger.debug("{} Opening found for week starting {}: cash={}, bank={}",
                        PREFIX, weekStart, initialCash, initialBank);
            } else {
                logger.warn("{} No opening found for week starting {}", PREFIX, weekStart);
            }
        } else {
            // Monthly report: find the LAST opening in the period and use only movements after it
            logger.debug("{} Monthly report: finding last opening in period", PREFIX);

            var allOpenings = openingRepo.findAll();

            // Find all openings within the month, sorted by date (latest first)
            List<CashboxOpening> openingsInMonth = allOpenings.stream()
                    .filter(o -> !o.getPeriodStartDate().isBefore(periodStart)
                            && !o.getPeriodStartDate().isAfter(periodEnd))
                    .sorted((a, b) -> b.getPeriodStartDate().compareTo(a.getPeriodStartDate()))
                    .collect(java.util.stream.Collectors.toList());

            if (!openingsInMonth.isEmpty()) {
                // Use the LAST (most recent) opening
                var lastOpening = openingsInMonth.get(0);
                initialCash = lastOpening.getCashAmount();
                initialBank = lastOpening.getBankAmount();

                // Recalculate movements ONLY from the last opening onwards
                LocalDateTime lastOpeningDate = lastOpening.getPeriodStartDate().atStartOfDay();
                movements = movementRepo.findByDateRange(lastOpeningDate, rangeEnd);

                logger.info("{} Using last opening from {}: cash={}, bank={}",
                        PREFIX, lastOpening.getPeriodStartDate(), initialCash, initialBank);
                logger.info("{} Recalculated movements from {} to {} ({} movements)",
                        PREFIX, lastOpeningDate, rangeEnd, movements.size());
            } else {
                logger.warn("{} No openings found in month - showing only net movements", PREFIX);
            }
        }

        // Separate by payment method: 0 = cash, 1/2/3 = bank
        double cashIn = 0;
        double cashOut = 0;
        double bankIn = 0;
        double bankOut = 0;

        for (CashboxMovement m : movements) {
            Integer paymentMethodId = m.getPaymentMethodId();
            double amount = m.getAmount();

            // If cash (0)
            if (paymentMethodId != null && paymentMethodId == 0) {
                if ("IN".equals(m.getDirection())) {
                    cashIn += amount;
                } else if ("OUT".equals(m.getDirection())) {
                    cashOut += amount;
                }
            }
            // If bank (1, 2, 3)
            else if (paymentMethodId != null && (paymentMethodId == 1 || paymentMethodId == 2 || paymentMethodId == 3)) {
                if ("IN".equals(m.getDirection())) {
                    bankIn += amount;
                } else if ("OUT".equals(m.getDirection())) {
                    bankOut += amount;
                }
            }
        }

        // Calculate balances: opening + movements
        report.setCashIn(cashIn);
        report.setCashOut(cashOut);
        report.setCashBalance(initialCash + cashIn - cashOut);

        report.setBankIn(bankIn);
        report.setBankOut(bankOut);
        report.setBankBalance(initialBank + bankIn - bankOut);

        report.setTotalIn(cashIn + bankIn);
        report.setTotalOut(cashOut + bankOut);
        report.setTotalBalance((initialCash + initialBank) + (cashIn + bankIn) - (cashOut + bankOut));

        // Calculate production per employee
        calculateProductionByUser(report, periodStart, periodEnd);

        logger.debug("{} Report generated: total={}", PREFIX, report.getTotalBalance());

        return report;
    }

    /**
     * Calculates production per user (employee) for the given period.
     */
    private void calculateProductionByUser(CashboxReportDTO report, LocalDate start, LocalDate end) {
        List<User> users = usersRepo.findAll().stream()
                .filter(u -> "user".equals(u.getRole()) || "admin".equals(u.getRole()))
                .collect(Collectors.toList());

        for (User user : users) {
            double production = serviceHeaderRepo.sumServiceTotalsByUserAndDateRange(
                    user.getId(),
                    start,
                    end
            );

            report.getProductionByUser().put(user.getId(), production);
            report.getUserNames().put(user.getId(), user.getName());
        }
    }
}