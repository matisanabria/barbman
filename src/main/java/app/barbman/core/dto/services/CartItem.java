package app.barbman.core.dto.services;

/**
 * Represents an item in a shopping cart for services.
 */
public class CartItem {
    private int serviceTypeId;
    private String serviceName;
    private double price;
    private int quantity;

    public CartItem(int serviceTypeId, String serviceName, double price, int quantity) {
        this.serviceTypeId = serviceTypeId;
        this.serviceName = serviceName;
        this.price = price;
        this.quantity = quantity;
    }

    public double getSubtotal() {
        return price * quantity;
    }

    public int getServiceTypeId() {
        return serviceTypeId;
    }

    public void setServiceTypeId(int serviceTypeId) {
        this.serviceTypeId = serviceTypeId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
