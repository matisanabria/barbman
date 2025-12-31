package app.barbman.core.model.sales.services;

import java.time.LocalDate;
import java.util.Objects;

public class ServiceHeader {

    private int id;
    private int userId;
    private int saleId;
    private LocalDate date;
    private double subtotal;

    // Constructor for new records
    public ServiceHeader(int userId, int saleId, LocalDate date, double subtotal) {
        this.userId = userId;
        this.saleId = saleId;
        this.date = date;
        this.subtotal = subtotal;
    }

    // Constructor for reading from DB
    public ServiceHeader(int id, int userId, int saleId, LocalDate date, double subtotal) {
        this(userId, saleId, date, subtotal);
        this.id = id;
    }

    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getSaleId() { return saleId; }
    public LocalDate getDate() { return date; }
    public double getSubtotal() { return subtotal; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setSaleId(int saleId) { this.saleId = saleId; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceHeader that = (ServiceHeader) o;
        return id == that.id &&
                userId == that.userId &&
                saleId == that.saleId &&
                Double.compare(that.subtotal, subtotal) == 0 &&
                Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, saleId, date, subtotal);
    }

    @Override
    public String toString() {
        return "ServiceHeader{" +
                "id=" + id +
                ", userId=" + userId +
                ", saleId=" + saleId +
                ", date=" + date +
                ", subtotal=" + subtotal +
                '}';
    }
}
