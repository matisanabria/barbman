package app.barbman.core.model;

import java.time.LocalDate;
import java.util.Objects;

public class Expense {
    private int id;
    private String description;
    private double amount;
    private LocalDate date;
    private String type;
    private int paymentMethodId;  // efectivo, transferencia

    /**
     * Expenses types:
     * 'supply': supplies and products (e.g., restocking inventory)
     * 'service': cleaning, rent, electricity, etc.
     * 'purchase': furniture, tools, decoration
     * 'tax': taxes, fees, licenses
     * 'other': irregular expenses, delivery, miscellaneous
     * The following are those who program uses automatically, user can't choose them:
     * 'salary': employees wages
     * 'advance': money given in advance to employees
     */

    public Expense() { }
    public Expense(int id) {
        this.id = id;
    }
    public Expense(String description, double amount, LocalDate date, String type, int paymentMethodId) {
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.type = type;
        this.paymentMethodId = paymentMethodId;
    }
    public Expense(int id, String description, double amount, LocalDate date, String type, int paymentMethodId) {
        this(description, amount, date, type, paymentMethodId);
        this.id = id;
    }

    // Getters
    public int getId() { return id; }
    public String getDescription() { return description; }
    public double getAmount() { return amount; }
    public LocalDate getDate() { return date; }
    public String getType() { return type; }
    public int getPaymentMethodId() { return paymentMethodId;}

    // Setters
    public void setId(int id) { this.id = id; }
    public void setDescription(String description) { this.description = description; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setType(String type) { this.type = type; }
    public void setPaymentMethodId(int paymentMethodId) { this.paymentMethodId = paymentMethodId; }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Expense expense = (Expense) o;
        return id == expense.id && Double.compare(amount, expense.amount) == 0 && Objects.equals(description, expense.description) && Objects.equals(date, expense.date) && Objects.equals(type, expense.type) && Objects.equals(paymentMethodId, expense.paymentMethodId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, description, amount, date, type, paymentMethodId);
    }

    @Override
    public String toString() {
        return "Expense{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", amount=" + amount +
                ", date=" + date +
                ", type='" + type + '\'' +
                ", paymentMethodId='" + paymentMethodId + '\'' +
                '}';
    }
}

