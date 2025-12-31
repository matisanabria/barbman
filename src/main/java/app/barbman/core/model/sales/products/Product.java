package app.barbman.core.model.sales.products;

import java.util.Objects;

/**
 * Represents an inventory product available for sale.
 * Products are used by the checkout system when registering product sales.
 */
public class Product {

    private int id;
    private String name;
    private double costPrice;
    private double unitPrice;
    private int stock;
    private String category;
    private String brand;
    private String imagePath;
    private String notes;

    // For creating new products
    public Product(String name, double costPrice, double unitPrice, int stock,
                   String category, String brand, String imagePath, String notes) {
        this.name = name;
        this.costPrice = costPrice;
        this.unitPrice = unitPrice;
        this.stock = stock;
        this.category = category;
        this.brand = brand;
        this.imagePath = imagePath;
        this.notes = notes;
    }

    // For reading from DB
    public Product(int id, String name, double costPrice, double unitPrice, int stock,
                   String category, String brand, String imagePath, String notes) {
        this(name, costPrice, unitPrice, stock, category, brand, imagePath, notes);
        this.id = id;
    }

    // For reading from DB (without imagePath)
    public Product(int id, String name, double costPrice, double unitPrice, int stock,
                   String category, String brand, String notes) {
        this.id = id;
        this.name = name;
        this.costPrice = costPrice;
        this.unitPrice = unitPrice;
        this.stock = stock;
        this.category = category;
        this.brand = brand;
        this.notes = notes;
    }


    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public double getCostPrice() { return costPrice; }
    public double getUnitPrice() { return unitPrice; }
    public int getStock() { return stock; }
    public String getCategory() { return category; }
    public String getBrand() { return brand; }
    public String getImagePath() { return imagePath; }
    public String getNotes() { return notes; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCostPrice(double costPrice) { this.costPrice = costPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    public void setStock(int stock) { this.stock = stock; }
    public void setCategory(String category) { this.category = category; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", unitPrice=" + unitPrice +
                ", stock=" + stock +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Product p = (Product) o;
        return id == p.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
