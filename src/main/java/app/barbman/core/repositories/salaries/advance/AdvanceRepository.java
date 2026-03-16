package app.barbman.core.repositories.salaries.advance;

import app.barbman.core.model.salaries.Advance;

import java.time.LocalDate;
import java.util.List;

public interface AdvanceRepository {

    List<Advance> findByUserAndDateRange(int userId, LocalDate from, LocalDate to);

    double getTotalByUserAndDateRange(int userId, LocalDate from, LocalDate to);

    void save(Advance advance);

    void delete(Integer id);

    List<Advance> findAll();
}
