package app.barbman.core.model.sales;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a sales transaction header.
 * A sale groups services and/or products into a single
 * cash operation with a final total amount.
 */
public class Sale {

    private int id;
    private int userId;
    private Integer clientId;          // nullable
    private int paymentMethodId;
    private LocalDate date;
    private double total;

    // For creating new sales
    public Sale(int userId, Integer clientId, int paymentMethodId,
                LocalDate date, double total) {
        this.userId = userId;
        this.clientId = clientId;
        this.paymentMethodId = paymentMethodId;
        this.date = date;
        this.total = total;
    }

    // For reading from DB
    public Sale(int id, int userId, Integer clientId, int paymentMethodId,
                LocalDate date, double total) {
        this(userId, clientId, paymentMethodId, date, total);
        this.id = id;
    }

    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public Integer getClientId() { return clientId; }
    public int getPaymentMethodId() { return paymentMethodId; }
    public LocalDate getDate() { return date; }
    public double getTotal() { return total; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setClientId(Integer clientId) { this.clientId = clientId; }
    public void setPaymentMethodId(int paymentMethodId) { this.paymentMethodId = paymentMethodId; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setTotal(double total) { this.total = total; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sale sale = (Sale) o;
        return id == sale.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Sale{" +
                "id=" + id +
                ", userId=" + userId +
                ", clientId=" + clientId +
                ", paymentMethodId=" + paymentMethodId +
                ", date=" + date +
                ", total=" + total +
                '}';
    }
}
