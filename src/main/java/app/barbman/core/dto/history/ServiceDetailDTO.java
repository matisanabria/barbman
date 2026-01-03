package app.barbman.core.dto.history;

import java.util.Objects;

/**
 * DTO that represents the detail of a service in a sale. Used when
 * user double-clicks on a sale in the sales history table.
 */

public class ServiceDetailDTO {
    private String name;
    private int quantity;
    private double unitPrice;
    private double total;

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ServiceDetailDTO that = (ServiceDetailDTO) o;
        return quantity == that.quantity && Double.compare(unitPrice, that.unitPrice) == 0 && Double.compare(total, that.total) == 0 && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, quantity, unitPrice, total);
    }
}