package app.barbman.core.model.cashbox;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class CashboxOpening {

    private Integer id;

    private LocalDate periodStartDate;   // monday of the week
    private LocalDateTime openedAt;

    private Integer openedByUserId;

    private double cashAmount;
    private double bankAmount;

    private String notes;

    public CashboxOpening() {}

    public CashboxOpening(
            LocalDate periodStartDate,
            LocalDateTime openedAt,
            Integer openedByUserId,
            double cashAmount,
            double bankAmount,
            String notes
    ) {
        this.periodStartDate = periodStartDate;
        this.openedAt = openedAt;
        this.openedByUserId = openedByUserId;
        this.cashAmount = cashAmount;
        this.bankAmount = bankAmount;
        this.notes = notes;
    }

    public CashboxOpening(
            Integer id,
            LocalDate periodStartDate,
            LocalDateTime openedAt,
            Integer openedByUserId,
            double cashAmount,
            double bankAmount,
            String notes
    ) {
        this(periodStartDate, openedAt, openedByUserId, cashAmount, bankAmount, notes);
        this.id = id;
    }

    // Getters
    public Integer getId() { return id; }
    public LocalDate getPeriodStartDate() { return periodStartDate; }
    public LocalDateTime getOpenedAt() { return openedAt; }
    public Integer getOpenedByUserId() { return openedByUserId; }
    public double getCashAmount() { return cashAmount; }
    public double getBankAmount() { return bankAmount; }
    public String getNotes() { return notes; }

    // Setters
    public void setId(Integer id) { this.id = id; }
    public void setPeriodStartDate(LocalDate periodStartDate) { this.periodStartDate = periodStartDate; }
    public void setOpenedAt(LocalDateTime openedAt) { this.openedAt = openedAt; }
    public void setOpenedByUserId(Integer openedByUserId) { this.openedByUserId = openedByUserId; }
    public void setCashAmount(double cashAmount) { this.cashAmount = cashAmount; }
    public void setBankAmount(double bankAmount) { this.bankAmount = bankAmount; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CashboxOpening)) return false;
        CashboxOpening that = (CashboxOpening) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
