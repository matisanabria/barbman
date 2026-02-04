package app.barbman.core.dto.history;

/**
 * DTO for individual sale items (services or products).
 */
public class SaleItemDTO {

    private String type; // "SERVICE" or "PRODUCT"
    private String name;
    private int quantity;
    private double unitPrice;
    private double total;

    // ============================================================
    // GETTERS & SETTERS
    // ============================================================

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}