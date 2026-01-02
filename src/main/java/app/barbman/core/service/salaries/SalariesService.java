package app.barbman.core.service.salaries;

import app.barbman.core.dto.SalaryDTO;
import app.barbman.core.model.Expense;
import app.barbman.core.model.salaries.Salary;
import app.barbman.core.model.human.User;
import app.barbman.core.model.time.DateRange;
import app.barbman.core.repositories.payments.salaries.SalariesRepository;
import app.barbman.core.service.salaries.advances.AdvancesService;
import app.barbman.core.service.expenses.ExpensesService;
import app.barbman.core.service.salaries.period.SalaryPeriodResolver;
import app.barbman.core.service.sales.services.ServiceHeaderService;
import app.barbman.core.service.users.UsersService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;

/**
 * Handles salary calculations, payments, and weekly salary DTO generation.
 */
public class SalariesService {
    private static final Logger logger = LogManager.getLogger(SalariesService.class);
    private static final String PREFIX = "[SALARIES-SERVICE]";

    private final SalariesRepository salariesRepository;
    private final ExpensesService expensesService;
    private final AdvancesService advancesService;
    private final ServiceHeaderService servicesHeaderService;
    private final SalaryPeriodResolver salaryPeriodResolver;

    public SalariesService(
            SalariesRepository repo,
            ExpensesService expensesService,
            AdvancesService advancesService,
            ServiceHeaderService servicesHeaderService,
            SalaryPeriodResolver salaryPeriodResolver
    ) {
        this.salariesRepository = repo;
        this.expensesService = expensesService;
        this.advancesService = advancesService;
        this.servicesHeaderService = servicesHeaderService;
        this.salaryPeriodResolver = salaryPeriodResolver;
    }


    /**
     * Registers a salary payment and links it with its corresponding expense record.
     *
     * @param salary          Salary to be paid (must include userId, week range, and calculated amount)
     * @param paymentMethodId Payment method ID (links to payment_methods table)
     * @param bonus           Optional bonus amount to add to the payment
     */
    public void paySalary(User user, Salary salary, int paymentMethodId, double bonus) {
        if (salary == null) throw new IllegalArgumentException("Salary is null");

        // Prevent duplicate weekly payment
        if (isPaid(salary.getUserId(), LocalDate.now())){
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
     * Calculates a user's salary for the current date range (Defined on User's configuration).
     */
    public Salary calculateSalary(User user, LocalDate referenceDate, double bonus) {
        if (user == null) {
            throw new IllegalArgumentException("User is null");
        }

        DateRange range =
                salaryPeriodResolver.resolve(user, referenceDate);

        double production =
                servicesHeaderService.getProductionByUserAndDateRange(
                        user.getId(),
                        range.getStart(),
                        range.getEnd()
                );

        double advances =
                advancesService.getTotalByUserAndRange(
                        user.getId(),
                        range.getStart(),
                        range.getEnd()
                );

        double calculated =
                calculateByPaymentType(
                        user.getPaymentType(),
                        production,
                        user.getParam1(),
                        user.getParam2()
                );

        double finalAmount = calculated + bonus - advances;

        if (finalAmount < 0) {
            double debt = Math.abs(finalAmount);
            finalAmount = 0;
            advancesService.saveAdvance(user.getId(), debt, 0);
        }

        return new Salary(
                user.getId(),
                range.getStart(),
                range.getEnd(),
                production,
                finalAmount,
                user.getPaymentType(),
                null,
                0,
                0
        );
    }

    /**
     * Calculates salary based on payment type.
     */
    private double calculateByPaymentType(
            int paymentType,
            double production,
            double param1,
            double param2
    ) {
        return switch (paymentType) {

            case 0 -> 0.0;
            // Manual payment → the amount will be entered manually at pay time

            case 1 -> calculateProductionSalary(production, param1);

            case 2 -> calculateBasePlusPercent(production, param1, param2);

            case 3 -> param1;
            // Fixed amount (weekly / monthly / whatever the period is)

            case 4 -> calculateMinThresholdOrPercentage(production, param1, param2);

            default -> throw new IllegalStateException(
                    "Invalid payment type: " + paymentType
            );
        };
    }


    /**
     * Calculates salary based on production percentage.
     */
    private double calculateProductionSalary(double production, double percentage) {
        double result = production * percentage;
        logger.debug("{} Production-based salary -> prod={}, percent={}, result={}", PREFIX, production, percentage, result);
        return result;
    }

    /**
     * Calculates salary as base + production percentage.
     */
    private double calculateBasePlusPercent(double production, double baseSalary, double percentage) {
        double result = baseSalary + (production * percentage);
        logger.debug("{} Base+percent salary -> prod={}, base={}, percent={}, result={}", PREFIX, production, baseSalary, percentage, result);
        return result;
    }

    /**
     * Calculates salary with guaranteed minimum threshold.
     */
    private double calculateMinThresholdOrPercentage(double production, double minThreshold, double percentage) {
        double calculated = production * percentage;
        double result = Math.max(minThreshold, calculated);
        logger.info("{} MinThresholdOrPercentage salary -> prod={}, min={}, percent={}, result={}",
                PREFIX, production, minThreshold, percentage, result);
        return result;
    }

    /**
     * Checks if the user already has a registered salary in the given date range
     */
    public boolean isPaid(int userId, LocalDate referenceDate) {
        return salariesRepository
                .findByUserAndDateWithinPeriod(userId, referenceDate) != null;
    }

    public SalaryDTO buildSalaryDTO(User user, LocalDate referenceDate) {

        DateRange range = salaryPeriodResolver.resolve(user, referenceDate);

        double production =
                servicesHeaderService.getProductionByUserAndDateRange(
                        user.getId(),
                        range.getStart(),
                        range.getEnd()
                );

        double advances =
                advancesService.getTotalByUserAndRange(
                        user.getId(),
                        range.getStart(),
                        range.getEnd()
                );

        double calculated =
                calculateByPaymentType(
                        user.getPaymentType(),
                        production,
                        user.getParam1(),
                        user.getParam2()
                );

        double finalAmount = Math.max(0, calculated - advances);

        Salary existingSalary =
                salariesRepository.findByUserAndDateWithinPeriod(
                        user.getId(),
                        referenceDate
                );

        SalaryDTO dto = new SalaryDTO();
        dto.setUserId(user.getId());
        dto.setUsername(user.getName());
        dto.setPeriodStart(range.getStart());
        dto.setPeriodEnd(range.getEnd());
        dto.setProduction(production);
        dto.setAdvances(advances);
        dto.setCalculatedAmount(calculated);
        dto.setFinalAmount(finalAmount);
        dto.setPaid(existingSalary != null);
        dto.setSalaryId(existingSalary != null ? existingSalary.getId() : null);

        return dto;
    }

}
