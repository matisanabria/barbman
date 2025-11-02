package app.barbman.core.model.services;

import java.time.LocalDate;
import java.util.Objects;

public class Service {

    private int id;
    private int userId;
    private LocalDate date;
    private int paymentMethodId;
    private double total;
    private String notes;

    public Service(int userId, LocalDate date, int paymentMethodId, double total, String notes) {
        this.userId = userId;
        this.date = date;
        this.paymentMethodId = paymentMethodId;
        this.total = total;
        this.notes = notes;
    }

    // For reading from DB
    public Service(int id, int userId, LocalDate date, int paymentMethodId, double total, String notes) {
        this(userId, date, paymentMethodId, total, notes);
        this.id = id;
    }

    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public LocalDate getDate() { return date; }
    public int getPaymentMethodId() { return paymentMethodId; }
    public double getTotal() { return total; }
    public String getNotes() { return notes; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setPaymentMethodId(int paymentMethodId) { this.paymentMethodId = paymentMethodId; }
    public void setTotal(double total) { this.total = total; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public String toString() {
        return "Service{" +
                "id=" + id +
                ", userId=" + userId +
                ", date=" + date +
                ", paymentMethodId=" + paymentMethodId +
                ", total=" + total +
                ", notes='" + notes + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Service service)) return false;
        return id == service.id &&
                userId == service.userId &&
                paymentMethodId == service.paymentMethodId &&
                Double.compare(service.total, total) == 0 &&
                Objects.equals(date, service.date) &&
                Objects.equals(notes, service.notes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, date, paymentMethodId, total, notes);
    }
}
