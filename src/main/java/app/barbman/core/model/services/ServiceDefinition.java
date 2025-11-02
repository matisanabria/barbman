package app.barbman.core.model.services;

import java.util.Objects;

public class ServiceDefinition {
    private int id;
    private String name;
    private double basePrice;
    private boolean available;

    public ServiceDefinition(){}

    public ServiceDefinition(String name, double basePrice, boolean available) {
        this.name = name;
        this.basePrice = basePrice;
        this.available = available;
    }

    // Constructor without availability (defaults to true)
    public ServiceDefinition(String name, double basePrice) {
        this(name, basePrice, true);
    }

    public ServiceDefinition(int id, String name, double basePrice, boolean available) {
        this.id = id;
        this.name = name;
        this.basePrice = basePrice;
        this.available = available;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getBasePrice() { return basePrice; }
    public boolean isAvailable() { return available; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setBasePrice(double basePrice) { this.basePrice = basePrice; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceDefinition that)) return false;
        return id == that.id &&
                Double.compare(that.basePrice, basePrice) == 0 &&
                available == that.available &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, basePrice, available);
    }

    @Override
    public String toString() {
        return "ServiceDefinition{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", basePrice=" + basePrice +
                ", available=" + available +
                '}';
    }
}
