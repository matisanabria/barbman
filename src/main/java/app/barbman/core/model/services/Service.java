package app.barbman.core.model.services;

import java.time.LocalDate;
import java.util.Objects;

public class Service {

    private int id;
    private int userId;
    private Integer clientId; // Integer to allow null (no client)
    private int quantity;
    private LocalDate date;
    private int paymentMethodId;
    private double total;
    private String notes;

    public Service(int userId, LocalDate date, int paymentMethodId, double total, String notes, Integer clientId, int quantity) {
        this.userId = userId;
        this.date = date;
        this.paymentMethodId = paymentMethodId;
        this.total = total;
        this.notes = notes;
        this.clientId = clientId;
        this.quantity = quantity;
    }

    // For reading from DB
    public Service(int id, int userId, LocalDate date, int paymentMethodId, double total, String notes, Integer clientId, int quantity) {
        this(userId, date, paymentMethodId, total, notes, clientId, quantity);
        this.id = id;
    }

    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public LocalDate getDate() { return date; }
    public int getPaymentMethodId() { return paymentMethodId; }
    public double getTotal() { return total; }
    public String getNotes() { return notes; }
    public Integer getClientId() { return clientId; }
    public int getQuantity() { return quantity; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setPaymentMethodId(int paymentMethodId) { this.paymentMethodId = paymentMethodId; }
    public void setTotal(double total) { this.total = total; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setClientId(Integer clientId) { this.clientId = clientId; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Service service = (Service) o;
        return id == service.id && userId == service.userId && quantity == service.quantity && paymentMethodId == service.paymentMethodId && Double.compare(total, service.total) == 0 && Objects.equals(clientId, service.clientId) && Objects.equals(date, service.date) && Objects.equals(notes, service.notes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, clientId, quantity, date, paymentMethodId, total, notes);
    }

    @Override
    public String toString() {
        return "Service{" +
                "id=" + id +
                ", userId=" + userId +
                ", clientId=" + clientId +
                ", quantity=" + quantity +
                ", date=" + date +
                ", paymentMethodId=" + paymentMethodId +
                ", total=" + total +
                ", notes='" + notes + '\'' +
                '}';
    }
}
