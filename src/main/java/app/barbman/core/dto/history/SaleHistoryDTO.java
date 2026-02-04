package app.barbman.core.dto.history;

import java.time.LocalDate;
import java.util.Objects;

/**
 * DTO that represents sales history on a table. It don't contain details of the sale.
 */

public class SaleHistoryDTO {
    private int saleId;

    private LocalDate date;
    private String userName;
    private String clientName;

    private double total;
    private String paymentMethod;
    private boolean beta;

    // opcional
    private boolean paid;

    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public boolean isBeta() { return beta; }
    public void setBeta(boolean beta) { this.beta = beta; }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SaleHistoryDTO that = (SaleHistoryDTO) o;
        return saleId == that.saleId && Double.compare(total, that.total) == 0 && paid == that.paid && Objects.equals(date, that.date) && Objects.equals(userName, that.userName) && Objects.equals(clientName, that.clientName) && Objects.equals(paymentMethod, that.paymentMethod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(saleId, date, userName, clientName, total, paymentMethod, paid);
    }

    @Override
    public String toString() {
        return "SaleHistoryDTO{" +
                "saleId=" + saleId +
                ", date=" + date +
                ", userName='" + userName + '\'' +
                ", clientName='" + clientName + '\'' +
                ", total=" + total +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", paid=" + paid +
                '}';
    }
}
