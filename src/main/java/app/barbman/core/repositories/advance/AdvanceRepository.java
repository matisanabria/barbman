package app.barbman.core.repositories.advance;

import app.barbman.core.model.salaries.Advance;
import app.barbman.core.repositories.GenericRepository;

import java.time.LocalDate;
import java.util.List;

public interface AdvanceRepository extends GenericRepository<Advance, Integer> {

    /**
     * Finds all advances belonging to a specific user within a given date range.
     */
    List<Advance> findByUserAndDateRange(int userId, LocalDate from, LocalDate to);

    /**
     * Calculates the total amount advanced to a user in a specific period.
     */
    double getTotalByUserAndDateRange(int userId, LocalDate from, LocalDate to);
}
