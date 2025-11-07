package app.barbman.core.service.salaries;

import app.barbman.core.dto.SalaryDTO;
import app.barbman.core.model.Expense;
import app.barbman.core.model.salaries.Salary;
import app.barbman.core.model.User;
import app.barbman.core.repositories.salaries.SalariesRepository;
import app.barbman.core.service.advances.AdvancesService;
import app.barbman.core.service.expenses.ExpensesService;
import app.barbman.core.service.services.ServicesService;
import app.barbman.core.service.users.UsersService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles salary calculations, payments, and weekly salary DTO generation.
 */
public class SalariesService {
    private static final Logger logger = LogManager.getLogger(SalariesService.class);
    private static final String PREFIX = "[SALARIES-SERVICE]";

    private final SalariesRepository salariesRepository;
    private final ExpensesService expensesService;
    private final ServicesService servicesService;
    private final AdvancesService advancesService;
    private final UsersService usersService;

    public SalariesService(
            SalariesRepository repo,
            ExpensesService expensesService,
            ServicesService servicesService,
            AdvancesService advancesService,
            UsersService usersService
    ) {
        this.salariesRepository = repo;
        this.expensesService = expensesService;
        this.servicesService = servicesService;
        this.advancesService = advancesService;
        this.usersService = usersService;
    }

    /**
     * Registers a salary payment and links it with its corresponding expense record.
     *
     * @param salary           Salary to be paid (must include userId, week range, and calculated amount)
     * @param paymentMethodId  Payment method ID (links to payment_methods table)
     * @param bonus            Optional bonus amount to add to the payment
     */
    public void paySalary(User user, Salary salary, int paymentMethodId, double bonus) {
        if (salary == null) throw new IllegalArgumentException("Salary is null");

        // Prevent duplicate weekly payment
        if (isPaid(salary.getUserId(), salary.getWeekStartDate())) {
            throw new IllegalStateException("This user already has a salary registered for this week.");
        }

        salary.setAmountPaid(salary.getAmountPaid() + bonus);
        salary.setPayDate(LocalDate.now());
        salary.setPaymentMethodId(paymentMethodId);

        // Register expense for the user receiving the payment
        Expense expense = expensesService.registerSalaryExpense(
                salary.getUserId(),
                salary.getAmountPaid(),
                paymentMethodId
        );

        // Link salary to expense
        salary.setExpenseId(expense.getId());
        salariesRepository.save(salary);

        logger.info("{} Salary paid and expense linked -> user={}, amount={}, method={}, expenseID={}",
                PREFIX, salary.getUserId(), salary.getAmountPaid(), paymentMethodId, expense.getId());
    }

    /**
     * Calculates a user's weekly salary based on payment type and deductions.
     */
    public Salary calculateSalary(User user, double bonus) {
        if (user == null || user.getId() <= 0) {
            throw new IllegalArgumentException("Invalid user.");
        }

        int type = user.getPaymentType();
        double param1 = user.getParam1();
        double param2 = user.getParam2();

        // Get current week range (Mon–Sat)
        LocalDate[] week = getCurrentWeekRange();
        LocalDate weekStart = week[0];
        LocalDate weekEnd = week[1];

        // Calculate production and total advances
        double production = servicesService.getWeeklyProductionByBarber(user.getId(), weekStart, weekEnd);
        double advances = advancesService.getTotalByUserAndRange(user.getId(), weekStart, weekEnd);

        logger.info("{} Calculating salary -> user={}, type={}, p1={}, p2={}, bonus={}",
                PREFIX, user.getId(), type, param1, param2, bonus);

        // Calculate base amount according to payment type
        double calculatedAmount = switch (type) {
            case 0 -> 0.0; // unpaid
            case 1 -> calculateProductionSalary(production, param1);
            case 2 -> calculateBasePlusPercent(production, param1, param2);
            case 3 -> param1;
            case 4 -> calculateMinThresholdOrPercentage(production, param1, param2);
            default -> throw new IllegalStateException("Undefined payment type");
        };

        // Final amount after bonus and advance deductions
        double finalAmount = calculatedAmount + bonus - advances;

        // Handle negative results (when advances > calculated salary)
        if (finalAmount < 0) {
            double pendingDebt = Math.abs(finalAmount);
            logger.warn("{} Negative salary detected -> user={}, adjustedAmount={}, debt={}",
                    PREFIX, user.getId(), finalAmount, pendingDebt);

            finalAmount = 0;
            LocalDate nextMonday = weekEnd.plusDays(2);

            // Automatically register an advance for the pending debt
            advancesService.saveAdvance(user.getId(), pendingDebt, 0);
            logger.info("{} Auto-generated advance registered -> user={}, amount={}, nextDate={}",
                    PREFIX, user.getId(), pendingDebt, nextMonday);
        }

        return new Salary(
                user.getId(), weekStart, weekEnd, production, finalAmount, type,
                null, // payDate → assigned on payment
                0,    // paymentMethodId → assigned later
                0     // expenseId → assigned on payment
        );
    }

    /** Returns the current week range (Monday → Saturday). */
    private LocalDate[] getCurrentWeekRange() {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        LocalDate saturday = today.with(DayOfWeek.SATURDAY);
        return new LocalDate[]{monday, saturday};
    }

    /** Calculates salary based on production percentage. */
    private double calculateProductionSalary(double production, double percentage) {
        double result = production * percentage;
        logger.debug("{} Production-based salary -> prod={}, percent={}, result={}", PREFIX, production, percentage, result);
        return result;
    }

    /** Calculates salary as base + production percentage. */
    private double calculateBasePlusPercent(double production, double baseSalary, double percentage) {
        double result = baseSalary + (production * percentage);
        logger.debug("{} Base+percent salary -> prod={}, base={}, percent={}, result={}", PREFIX, production, baseSalary, percentage, result);
        return result;
    }

    /** Calculates salary with guaranteed minimum threshold. */
    private double calculateMinThresholdOrPercentage(double production, double minThreshold, double percentage) {
        double calculated = production * percentage;
        double result = Math.max(minThreshold, calculated);
        logger.info("{} MinThresholdOrPercentage salary -> prod={}, min={}, percent={}, result={}",
                PREFIX, production, minThreshold, percentage, result);
        return result;
    }

    /** Checks if the user already has a registered salary in the given week. */
    public boolean isPaid(int userId, LocalDate weekStart) {
        boolean result = salariesRepository.findByBarberoAndFecha(userId, weekStart) != null;
        logger.debug("{} Checking if user is paid -> user={}, weekStart={}, result={}", PREFIX, userId, weekStart, result);
        return result;
    }

    /**
     * Generates a list of SalaryDTOs for the given weekly period.
     * Used in the SalariesView for UI display.
     */
    public List<SalaryDTO> generateWeeklySalaryDTOs(LocalDate start, LocalDate end) {
        List<SalaryDTO> result = new ArrayList<>();
        List<User> users = usersService.getAllUsers();

        for (User user : users) {
            int userId = user.getId();
            String name = user.getName();

            double production = servicesService.getWeeklyProductionByBarber(userId, start, end);
            Salary tempSalary = calculateSalary(user, 0);
            double advances = advancesService.getTotalByUserAndRange(userId, start, end);
            double netAmount = tempSalary.getAmountPaid() - advances;

            boolean alreadyPaid = isPaid(userId, start);
            int salaryId = alreadyPaid
                    ? salariesRepository.findByBarberoAndFecha(userId, start).getId()
                    : 0;

            SalaryDTO dto = new SalaryDTO();
            dto.setUserId(userId);
            dto.setUsername(name);
            dto.setTotalProduction(production);
            dto.setAmountPaid(netAmount);
            dto.setPaymentStatus(alreadyPaid);
            dto.setSalaryId(salaryId);

            result.add(dto);
        }

        logger.info("{} Generated {} weekly salary DTOs ({} → {})", PREFIX, result.size(), start, end);
        return result;
    }
}
