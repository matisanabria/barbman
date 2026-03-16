package app.barbman.core.repositories.expense;

import app.barbman.core.model.Expense;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository {
    List<Expense> searchByDateRange(LocalDate startDate, LocalDate endDate);
    double sumTotalByPaymentMethodAndPeriod(int paymentMethodId, LocalDate start, LocalDate end);
    double sumTotalByPeriod(LocalDate start, LocalDate end);
    void save(Expense expense);
    void delete(Integer id);
    List<Expense> findAll();
}
