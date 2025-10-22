package app.barbman.core.repositories.salaries;

import app.barbman.core.model.Salary;
import app.barbman.core.repositories.GenericRepository;

import java.time.LocalDate;

public interface SalariesRepository extends GenericRepository<Salary,Integer> {
    Salary findByBarberoAndFecha(int nameId, LocalDate date);
}
