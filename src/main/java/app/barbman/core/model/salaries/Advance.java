package app.barbman.core.model.salaries;

import java.time.LocalDate;
import java.util.Objects;

public class Advance {
    private int id;
    private int userId;
    private double amount;
    private LocalDate date;
    private int paymentMethodId;
    private int expenseId;

    public Advance() {}
    public Advance(int userId, double amount, LocalDate date, int paymentMethodId, int expenseId) {
        this.userId = userId;
        this.amount = amount;
        this.date = date;
        this.paymentMethodId = paymentMethodId;
        this.expenseId = expenseId;
    }
    public Advance(int id, int userId, double amount, LocalDate date, int paymentMethodId, int expenseId) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.date = date;
        this.paymentMethodId = paymentMethodId;
        this.expenseId = expenseId;
    }


    // Getters & Setters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public double getAmount() { return amount; }
    public LocalDate getDate() { return date; }
    public int getPaymentMethodId() { return paymentMethodId; }
    public int getExpenseId() { return expenseId; }

    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setPaymentMethodId(int paymentMethodId) { this.paymentMethodId = paymentMethodId; }
    public void setExpenseId(int expenseId) { this.expenseId = expenseId; }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Advance advance = (Advance) o;
        return id == advance.id && userId == advance.userId && Double.compare(amount, advance.amount) == 0 && paymentMethodId == advance.paymentMethodId && expenseId == advance.expenseId && Objects.equals(date, advance.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, amount, date, paymentMethodId, expenseId);
    }

    @Override
    public String toString() {
        return "Advance{" +
                "id=" + id +
                ", userId=" + userId +
                ", amount=" + amount +
                ", date=" + date +
                ", paymentMethodId=" + paymentMethodId +
                ", expenseId=" + expenseId +
                '}';
    }
}
