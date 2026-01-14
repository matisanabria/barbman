package app.barbman.core.model.cashbox;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class CashboxClosure {

    private Integer id;

    private LocalDate periodStartDate;
    private LocalDate periodEndDate;

    private LocalDateTime closedAt;
    private Integer closedByUserId;

    private double expectedCash;
    private double expectedBank;
    private double expectedTotal;


    private String notes;

    public CashboxClosure() {}

    public CashboxClosure(
            LocalDate periodStartDate,
            LocalDate periodEndDate,
            LocalDateTime closedAt,
            Integer closedByUserId,
            double expectedCash,
            double expectedBank,
            double expectedTotal,
            String notes
    ) {
        this.periodStartDate = periodStartDate;
        this.periodEndDate = periodEndDate;
        this.closedAt = closedAt;
        this.closedByUserId = closedByUserId;
        this.expectedCash = expectedCash;
        this.expectedBank = expectedBank;
        this.expectedTotal = expectedTotal;
        this.notes = notes;
    }

    public CashboxClosure(
            Integer id,
            LocalDate periodStartDate,
            LocalDate periodEndDate,
            LocalDateTime closedAt,
            Integer closedByUserId,
            double expectedCash,
            double expectedBank,
            double expectedTotal,
            String notes
    ) {
        this(periodStartDate, periodEndDate, closedAt, closedByUserId,
                expectedCash, expectedBank, expectedTotal,
                notes);
        this.id = id;
    }

    // Getters
    public Integer getId() { return id; }
    public LocalDate getPeriodStartDate() { return periodStartDate; }
    public LocalDate getPeriodEndDate() { return periodEndDate; }
    public LocalDateTime getClosedAt() { return closedAt; }
    public Integer getClosedByUserId() { return closedByUserId; }
    public double getExpectedCash() { return expectedCash; }
    public double getExpectedBank() { return expectedBank; }
    public double getExpectedTotal() { return expectedTotal; }
    public String getNotes() { return notes; }

    // Setters
    public void setId(Integer id) { this.id = id; }
    public void setPeriodStartDate(LocalDate periodStartDate) { this.periodStartDate = periodStartDate; }
    public void setPeriodEndDate(LocalDate periodEndDate) { this.periodEndDate = periodEndDate; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }
    public void setClosedByUserId(Integer closedByUserId) { this.closedByUserId = closedByUserId; }
    public void setExpectedCash(double expectedCash) { this.expectedCash = expectedCash; }
    public void setExpectedBank(double expectedBank) { this.expectedBank = expectedBank; }
    public void setExpectedTotal(double expectedTotal) { this.expectedTotal = expectedTotal; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CashboxClosure)) return false;
        CashboxClosure that = (CashboxClosure) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
