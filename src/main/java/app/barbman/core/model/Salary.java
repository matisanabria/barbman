package app.barbman.core.model;

import java.time.LocalDate;
import java.util.Objects;

public class Salary {
    private int id;
    private int userId;
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private double totalProduction;
    private double amountPaid;
    private int payTypeSnapshot; // saves payment type at the time of payment
    private LocalDate payDate;
    private int paymentMethodId;

    public Salary() { }
    public Salary(int id) {
        this.id = id;
    }
    public Salary(int userId,
                  LocalDate weekStartDate,
                  LocalDate weekEndDate,
                  double totalProduction,
                  double amountPaid,
                  int paymentTypeSnapshot,
                  LocalDate payDate,
                  int paymentMethodId) {
        this.userId = userId;
        this.weekStartDate = weekStartDate;
        this.weekEndDate = weekEndDate;
        this.totalProduction = totalProduction;
        this.amountPaid = amountPaid;
        this.payTypeSnapshot = paymentTypeSnapshot;
        this.payDate = payDate;
        this.paymentMethodId = paymentMethodId;
    }


    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public LocalDate getWeekStartDate() { return weekStartDate; }
    public LocalDate getWeekEndDate() { return weekEndDate; }
    public double getTotalProduction() { return totalProduction; }
    public double getAmountPaid() { return amountPaid; }
    public int getPayTypeSnapshot() { return payTypeSnapshot; }
    public LocalDate getPayDate() { return payDate; }
    public int getPaymentMethodId() { return paymentMethodId; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setWeekStartDate(LocalDate weekStartDate) { this.weekStartDate = weekStartDate; }
    public void setWeekEndDate(LocalDate weekEndDate) { this.weekEndDate = weekEndDate; }
    public void setTotalProduction(double totalProduction) { this.totalProduction = totalProduction; }
    public void setAmountPaid(double amountPaid) { this.amountPaid = amountPaid; }
    public void setPayTypeSnapshot(int payTypeSnapshot) { this.payTypeSnapshot = payTypeSnapshot; }
    public void setPayDate(LocalDate payDate) { this.payDate = payDate; }
    public void setPaymentMethodId(int paymentMethodId) { this.paymentMethodId = paymentMethodId; }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Salary salary = (Salary) o;
        return id == salary.id && userId == salary.userId && Double.compare(totalProduction, salary.totalProduction) == 0 && Double.compare(amountPaid, salary.amountPaid) == 0 && payTypeSnapshot == salary.payTypeSnapshot && paymentMethodId == salary.paymentMethodId && Objects.equals(weekStartDate, salary.weekStartDate) && Objects.equals(weekEndDate, salary.weekEndDate) && Objects.equals(payDate, salary.payDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, weekStartDate, weekEndDate, totalProduction, amountPaid, payTypeSnapshot, payDate, paymentMethodId);
    }

    @Override
    public String toString() {
        return "Salary{" +
                "id=" + id +
                ", userId=" + userId +
                ", weekStartDate=" + weekStartDate +
                ", weekEndDate=" + weekEndDate +
                ", totalProduction=" + totalProduction +
                ", amountPaid=" + amountPaid +
                ", payTypeSnapshot=" + payTypeSnapshot +
                ", payDate=" + payDate +
                ", paymentMethodId=" + paymentMethodId +
                '}';
    }
}
