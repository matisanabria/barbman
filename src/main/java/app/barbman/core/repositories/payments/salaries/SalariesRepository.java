package app.barbman.core.repositories.payments.salaries;

import app.barbman.core.model.salaries.Salary;
import app.barbman.core.repositories.GenericRepository;

import java.time.LocalDate;

public interface SalariesRepository extends GenericRepository<Salary,Integer> {
    /**
     * Finds a salary record for a given user where the provided date
     * is within the salary period (start_date <= date <= end_date).
     */
    Salary findByUserAndDateWithinPeriod(int userId, LocalDate date);
}
