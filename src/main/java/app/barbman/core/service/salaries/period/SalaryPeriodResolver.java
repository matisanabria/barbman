package app.barbman.core.service.salaries.period;

/**
 * A utility class for resolving salary periods based on user's pay frequency.
 */

import app.barbman.core.model.human.User;
import app.barbman.core.model.time.DateRange;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;

public class SalaryPeriodResolver {

    public DateRange resolve(User user, LocalDate referenceDate) {

        if (user == null) {
            throw new IllegalArgumentException("User is null");
        }

        return switch (user.getPayFrequency()) {

            case DAILY -> resolveDaily(referenceDate);

            case WEEKLY -> resolveWeekly(referenceDate);

            case BIWEEKLY -> resolveBiWeekly(referenceDate);

            case MONTHLY -> resolveMonthly(referenceDate);
        };
    }

    // -----------------------
    // PERIOD RESOLVERS
    // -----------------------

    private DateRange resolveDaily(LocalDate date) {
        return new DateRange(date, date);
    }

    private DateRange resolveWeekly(LocalDate date) {
        LocalDate start = date.with(DayOfWeek.MONDAY);
        LocalDate end = date.with(DayOfWeek.SATURDAY);
        return new DateRange(start, end);
    }

    private DateRange resolveBiWeekly(LocalDate date) {
        int day = date.getDayOfMonth();

        if (day <= 15) {
            return new DateRange(
                    date.withDayOfMonth(1),
                    date.withDayOfMonth(15)
            );
        } else {
            YearMonth ym = YearMonth.from(date);
            return new DateRange(
                    date.withDayOfMonth(16),
                    date.withDayOfMonth(ym.lengthOfMonth())
            );
        }
    }

    private DateRange resolveMonthly(LocalDate date) {
        YearMonth ym = YearMonth.from(date);
        return new DateRange(
                ym.atDay(1),
                ym.atEndOfMonth()
        );
    }
}
