package app.barbman.core.repositories.salaries.salaries;

import app.barbman.core.model.salaries.Salary;

import java.time.LocalDate;
import java.util.List;

public interface SalariesRepository {
    Salary findByUserAndDateWithinPeriod(int userId, LocalDate date);

    void save(Salary salary);
    void delete(Integer id);
    List<Salary> findAll();
}
