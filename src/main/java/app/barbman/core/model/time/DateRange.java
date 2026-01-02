package app.barbman.core.model.time;

import java.time.LocalDate;

/**
 * Represents a range of dates with a start and end date.
 */
public class DateRange {

    private final LocalDate start;
    private final LocalDate end;

    public DateRange(LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        this.start = start;
        this.end = end;
    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }
}
