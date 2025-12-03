package app.barbman.core.dto.cart;

import java.util.Objects;

public class CartItem {

    public enum ItemType { SERVICE, PRODUCT }

    private ItemType type;       // SERVICE o PRODUCT
    private int definitionId;    // id del service_definition o product
    private String name;         // nombre visible
    private double price;        // precio actual cargado
    private int quantity;        // unidades

    public CartItem(ItemType type, int definitionId, String name, double price, int quantity) {
        this.type = type;
        this.definitionId = definitionId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    // Subtotal (clean)
    public double getSubtotal() {
        return price * quantity;
    }

    // Getters & Setters
    public ItemType getType() {
        return type;
    }

    public void setType(ItemType type) {
        this.type = type;
    }

    public int getDefinitionId() {
        return definitionId;
    }

    public void setDefinitionId(int definitionId) {
        this.definitionId = definitionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return definitionId == cartItem.definitionId && Double.compare(price, cartItem.price) == 0 && quantity == cartItem.quantity && type == cartItem.type && Objects.equals(name, cartItem.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, definitionId, name, price, quantity);
    }
}
