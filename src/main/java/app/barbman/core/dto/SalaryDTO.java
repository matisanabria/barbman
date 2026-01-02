package app.barbman.core.dto;

import java.time.LocalDate;

/**
 * DTO for saving and transferring salary information.
 */
public class SalaryDTO {
    private int userId;
    private String username;

    private LocalDate periodStart;
    private LocalDate periodEnd;

    private double production;
    private double advances;
    private double calculatedAmount;
    private double finalAmount;

    private boolean paid;
    private Integer salaryId; // null if not yet paid

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

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDate periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDate periodEnd) {
        this.periodEnd = periodEnd;
    }

    public double getProduction() {
        return production;
    }

    public void setProduction(double production) {
        this.production = production;
    }

    public double getAdvances() {
        return advances;
    }

    public void setAdvances(double advances) {
        this.advances = advances;
    }

    public double getCalculatedAmount() {
        return calculatedAmount;
    }

    public void setCalculatedAmount(double calculatedAmount) {
        this.calculatedAmount = calculatedAmount;
    }

    public double getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(double finalAmount) {
        this.finalAmount = finalAmount;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public Integer getSalaryId() {
        return salaryId;
    }

    public void setSalaryId(Integer salaryId) {
        this.salaryId = salaryId;
    }


}
