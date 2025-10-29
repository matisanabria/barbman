package app.barbman.core.model;

import java.util.Objects;

public class ServiceDefinition {
    private int id;
    private String name;
    private double basePrice;

    public ServiceDefinition(){}
    public ServiceDefinition(String name, double basePrice){
        // For saving on DB new services (ID is auto incremented)
        this.name=name;
        this.basePrice=basePrice;
    }
    public ServiceDefinition(int id, String name, double basePrice){
        this.id=id;
        this.name = name;
        this.basePrice = basePrice;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ServiceDefinition that = (ServiceDefinition) o;
        return id == that.id && Double.compare(basePrice, that.basePrice) == 0 && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, basePrice);
    }
}
