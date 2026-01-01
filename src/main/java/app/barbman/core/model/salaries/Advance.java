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
    private String description;

    public Advance() {}

    // For creating new advances
    public Advance(int userId,
                   double amount,
                   LocalDate date,
                   int paymentMethodId,
                   int expenseId,
                   String description) {
        this.userId = userId;
        this.amount = amount;
        this.date = date;
        this.paymentMethodId = paymentMethodId;
        this.expenseId = expenseId;
        this.description = description;
    }

    // For reading from DB
    public Advance(int id,
                   int userId,
                   double amount,
                   LocalDate date,
                   int paymentMethodId,
                   int expenseId,
                   String description) {
        this(userId, amount, date, paymentMethodId, expenseId, description);
        this.id = id;
    }

    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public double getAmount() { return amount; }
    public LocalDate getDate() { return date; }
    public int getPaymentMethodId() { return paymentMethodId; }
    public int getExpenseId() { return expenseId; }
    public String getDescription() { return description; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setPaymentMethodId(int paymentMethodId) { this.paymentMethodId = paymentMethodId; }
    public void setExpenseId(int expenseId) { this.expenseId = expenseId; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Advance advance = (Advance) o;
        return id == advance.id
                && userId == advance.userId
                && Double.compare(amount, advance.amount) == 0
                && paymentMethodId == advance.paymentMethodId
                && expenseId == advance.expenseId
                && Objects.equals(date, advance.date)
                && Objects.equals(description, advance.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, amount, date, paymentMethodId, expenseId, description);
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
                ", description='" + description + '\'' +
                '}';
    }
}
