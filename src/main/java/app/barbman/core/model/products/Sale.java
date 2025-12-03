package app.barbman.core.model.products;

import java.time.LocalDate;
import java.util.Objects;

public class Sale {

    private int id;
    private LocalDate date;
    private double total;
    private int paymentMethodId;

    private Integer clientId; // opcional

    public Sale(LocalDate date, double total, int paymentMethodId, Integer clientId) {
        this.date = date;
        this.total = total;
        this.paymentMethodId = paymentMethodId;
        this.clientId = clientId;
    }

    public Sale(int id, LocalDate date, double total, int paymentMethodId, Integer clientId) {
        this(date, total, paymentMethodId, clientId);
        this.id = id;
    }

    // ==== Getters ====
    public int getId() { return id; }
    public LocalDate getDate() { return date; }
    public double getTotal() { return total; }
    public int getPaymentMethodId() { return paymentMethodId; }
    public Integer getClientId() { return clientId; }

    // ==== Setters ====
    public void setId(int id) { this.id = id; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setTotal(double total) { this.total = total; }
    public void setPaymentMethodId(int paymentMethodId) { this.paymentMethodId = paymentMethodId; }
    public void setClientId(Integer clientId) { this.clientId = clientId; }

    @Override
    public String toString() {
        return "Sale{" +
                "id=" + id +
                ", date=" + date +
                ", total=" + total +
                ", paymentMethodId=" + paymentMethodId +
                ", clientId=" + clientId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Sale sale = (Sale) o;
        return id == sale.id && Double.compare(total, sale.total) == 0 && paymentMethodId == sale.paymentMethodId && Objects.equals(date, sale.date) && Objects.equals(clientId, sale.clientId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, date, total, paymentMethodId, clientId);
    }
}
