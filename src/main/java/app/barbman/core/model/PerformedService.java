package app.barbman.core.model;

import java.time.LocalDate;
import java.util.Objects;

public class PerformedService {

    private int id;
    private int userId;
    private int serviceTypeId;
    private double price;
    private LocalDate date;
    private int paymentMethodId;
    private String notes;

    public PerformedService() {}

    public PerformedService(int id) {
        this.id = id;
    }

    public PerformedService(int userId, int serviceTypeId, double price,
                            LocalDate date, int paymentMethodId, String notes) {
        this.userId = userId;
        this.serviceTypeId = serviceTypeId;
        this.price = price;
        this.date = date;
        this.paymentMethodId = paymentMethodId;
        this.notes = notes;
    }

    public PerformedService(int id, int userId, int serviceTypeId, double price,
                            LocalDate date, int paymentMethodId, String notes) {
        this(userId, serviceTypeId, price, date, paymentMethodId, notes);
        this.id = id;
    }

    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getServiceTypeId() { return serviceTypeId; }
    public double getPrice() { return price; }
    public LocalDate getDate() { return date; }
    public int getPaymentMethodId() { return paymentMethodId; }
    public String getNotes() { return notes; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setServiceTypeId(int serviceTypeId) { this.serviceTypeId = serviceTypeId; }
    public void setPrice(double price) { this.price = price; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setPaymentMethodId(int paymentMethodId) { this.paymentMethodId = paymentMethodId; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public String toString() {
        return "PerformedService{" +
                "id=" + id +
                ", userId=" + userId +
                ", serviceTypeId=" + serviceTypeId +
                ", price=" + price +
                ", date=" + date +
                ", paymentMethodId=" + paymentMethodId +
                ", notes='" + notes + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PerformedService that = (PerformedService) o;
        return id == that.id &&
                userId == that.userId &&
                serviceTypeId == that.serviceTypeId &&
                Double.compare(that.price, price) == 0 &&
                paymentMethodId == that.paymentMethodId &&
                Objects.equals(date, that.date) &&
                Objects.equals(notes, that.notes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, serviceTypeId, price, date, paymentMethodId, notes);
    }
}
