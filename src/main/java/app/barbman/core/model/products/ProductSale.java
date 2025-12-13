package app.barbman.core.model.products;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a complete product sale transaction.
 * Individual products sold are recorded in ProductSaleItem.
 */

public class ProductSale {
    private int id;
    private LocalDate date;
    private double total;
    private int paymentMethodId;
    private Integer clientId;

    public ProductSale(LocalDate date, double total, int paymentMethodId, Integer clientId) {
        this.date = date;
        this.total = total;
        this.paymentMethodId = paymentMethodId;
        this.clientId = clientId;
    }

    // For loading from DB
    public ProductSale(int id, LocalDate date, double total, int paymentMethodId, Integer clientId) {
        this(date, total, paymentMethodId, clientId);
        this.id = id;
    }

    // Getters / Setters
    public int getId() { return id; }
    public LocalDate getDate() { return date; }
    public double getTotal() { return total; }
    public int getPaymentMethodId() { return paymentMethodId; }
    public Integer getClientId() { return clientId; }

    public void setId(int id) { this.id = id; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setTotal(double total) { this.total = total; }
    public void setPaymentMethodId(int paymentMethodId) { this.paymentMethodId = paymentMethodId; }
    public void setClientId(Integer clientId) { this.clientId = clientId; }

    @Override
    public String toString() {
        return "ProductSale{" +
                "id=" + id +
                ", total=" + total +
                ", paymentMethodId=" + paymentMethodId +
                '}';
    }
}
