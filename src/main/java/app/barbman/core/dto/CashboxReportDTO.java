package app.barbman.core.dto;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO for cashbox reports (daily, weekly, monthly).
 */
public class CashboxReportDTO {

    private LocalDate periodStart;
    private LocalDate periodEnd;

    // Efectivo
    private double cashIn;
    private double cashOut;
    private double cashBalance;

    // Banco (transferencia + tarjeta + QR)
    private double bankIn;
    private double bankOut;
    private double bankBalance;

    // Total
    private double totalIn;
    private double totalOut;
    private double totalBalance;

    // Producción por empleado: userId -> monto
    private Map<Integer, Double> productionByUser = new HashMap<>();

    // Nombres de empleados: userId -> nombre
    private Map<Integer, String> userNames = new HashMap<>();

    // ============================================================
    // GETTERS & SETTERS
    // ============================================================

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

    public double getCashIn() {
        return cashIn;
    }

    public void setCashIn(double cashIn) {
        this.cashIn = cashIn;
    }

    public double getCashOut() {
        return cashOut;
    }

    public void setCashOut(double cashOut) {
        this.cashOut = cashOut;
    }

    public double getCashBalance() {
        return cashBalance;
    }

    public void setCashBalance(double cashBalance) {
        this.cashBalance = cashBalance;
    }

    public double getBankIn() {
        return bankIn;
    }

    public void setBankIn(double bankIn) {
        this.bankIn = bankIn;
    }

    public double getBankOut() {
        return bankOut;
    }

    public void setBankOut(double bankOut) {
        this.bankOut = bankOut;
    }

    public double getBankBalance() {
        return bankBalance;
    }

    public void setBankBalance(double bankBalance) {
        this.bankBalance = bankBalance;
    }

    public double getTotalIn() {
        return totalIn;
    }

    public void setTotalIn(double totalIn) {
        this.totalIn = totalIn;
    }

    public double getTotalOut() {
        return totalOut;
    }

    public void setTotalOut(double totalOut) {
        this.totalOut = totalOut;
    }

    public double getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(double totalBalance) {
        this.totalBalance = totalBalance;
    }

    public Map<Integer, Double> getProductionByUser() {
        return productionByUser;
    }

    public void setProductionByUser(Map<Integer, Double> productionByUser) {
        this.productionByUser = productionByUser;
    }

    public Map<Integer, String> getUserNames() {
        return userNames;
    }

    public void setUserNames(Map<Integer, String> userNames) {
        this.userNames = userNames;
    }
}