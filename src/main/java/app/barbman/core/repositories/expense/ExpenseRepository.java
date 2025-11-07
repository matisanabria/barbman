package app.barbman.core.repositories.expense;

import app.barbman.core.model.Expense;
import app.barbman.core.repositories.GenericRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends GenericRepository<Expense, Integer> {
    List<Expense> searchByDateRange(LocalDate startDate, LocalDate endDate);
}
