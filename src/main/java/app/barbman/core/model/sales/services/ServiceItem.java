package app.barbman.core.model.sales.services;

import java.util.Objects;

public class ServiceItem {

    private int id;
    private int serviceHeaderId;
    private int serviceDefinitionId;
    private int quantity;
    private double unitPrice;
    private double itemTotal;

    public ServiceItem() {}

    // For saving new items
    public ServiceItem(int serviceHeaderId, int serviceDefinitionId, int quantity, double unitPrice, double itemTotal) {
        this.serviceHeaderId = serviceHeaderId;
        this.serviceDefinitionId = serviceDefinitionId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.itemTotal = itemTotal;
    }

    // For reading from DB
    public ServiceItem(int id, int serviceHeaderId, int serviceDefinitionId, int quantity, double unitPrice, double itemTotal) {
        this(serviceHeaderId, serviceDefinitionId, quantity, unitPrice, itemTotal);
        this.id = id;
    }

    // Getters
    public int getId() { return id; }
    public int getServiceHeaderId() { return serviceHeaderId; }
    public int getServiceDefinitionId() { return serviceDefinitionId; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
    public double getItemTotal() { return itemTotal; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setServiceHeaderId(int serviceHeaderId) { this.serviceHeaderId = serviceHeaderId; }
    public void setServiceDefinitionId(int serviceDefinitionId) { this.serviceDefinitionId = serviceDefinitionId; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    public void setItemTotal(double itemTotal) { this.itemTotal = itemTotal; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceItem that = (ServiceItem) o;
        return id == that.id &&
                serviceHeaderId == that.serviceHeaderId &&
                serviceDefinitionId == that.serviceDefinitionId &&
                quantity == that.quantity &&
                Double.compare(that.unitPrice, unitPrice) == 0 &&
                Double.compare(that.itemTotal, itemTotal) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, serviceHeaderId, serviceDefinitionId, quantity, unitPrice, itemTotal);
    }

    @Override
    public String toString() {
        return "ServiceItem{" +
                "id=" + id +
                ", serviceHeaderId=" + serviceHeaderId +
                ", serviceDefinitionId=" + serviceDefinitionId +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", itemTotal=" + itemTotal +
                '}';
    }
}
