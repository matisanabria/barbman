package app.barbman.core.model.services;

import java.util.Objects;

public class ServiceItem {
    private int id;
    private int serviceId;
    private int serviceTypeId;
    private double price;

    public ServiceItem() {}

    // For saving new items
    public ServiceItem(int serviceId, int serviceTypeId, double price) {
        this.serviceId = serviceId;
        this.serviceTypeId = serviceTypeId;
        this.price = price;
    }

    // For reading
    public ServiceItem(int id, int serviceId, int serviceTypeId, double price) {
        this(serviceId, serviceTypeId, price);
        this.id = id;
    }

    public int getId() { return id; }
    public int getServiceId() { return serviceId; }
    public int getServiceTypeId() { return serviceTypeId; }
    public double getPrice() { return price; }

    public void setId(int id) { this.id = id; }
    public void setServiceId(int serviceId) { this.serviceId = serviceId; }
    public void setServiceTypeId(int serviceTypeId) { this.serviceTypeId = serviceTypeId; }
    public void setPrice(double price) { this.price = price; }

    @Override
    public String toString() {
        return "ServiceItem{" +
                "id=" + id +
                ", serviceId=" + serviceId +
                ", serviceTypeId=" + serviceTypeId +
                ", price=" + price +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ServiceItem that = (ServiceItem) o;
        return id == that.id && serviceId == that.serviceId && serviceTypeId == that.serviceTypeId && Double.compare(price, that.price) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, serviceId, serviceTypeId, price);
    }
}
