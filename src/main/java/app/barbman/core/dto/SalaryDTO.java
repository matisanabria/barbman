package app.barbman.core.dto;

/**
 * DTO para guardar información que se muestra en la vista de salaries
 */
public class SalaryDTO {
    private int userId;
    private String username;
    private double totalProduction;
    private double amountPaid;
    private boolean paymentStatus; // true if exists salary paid in that week
    private int salaryId;   // si ya existe un sueldo, se usa este id, si no = -1

    public SalaryDTO() {
    }

    public SalaryDTO(int userId, String username, double produccionSemanal,
                     double amountPaid, boolean paymentStatus, int salaryId) {
        this.userId = userId;
        this.username = username;
        this.totalProduction = produccionSemanal;
        this.amountPaid = amountPaid;
        this.paymentStatus = paymentStatus;
        this.salaryId = salaryId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getTotalProduction() {
        return totalProduction;
    }

    public void setTotalProduction(double totalProduction) {
        this.totalProduction = totalProduction;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public boolean isPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(boolean paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public int getSalaryId() {
        return salaryId;
    }

    public void setSalaryId(int salaryId) {
        this.salaryId = salaryId;
    }
}
