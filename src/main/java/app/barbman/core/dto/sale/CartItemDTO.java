package app.barbman.core.dto.sale;

import java.util.Objects;

public class CartItemDTO {

    public enum ItemType { SERVICE, PRODUCT }

    private ItemType type;
    private int definitionId;
    private String name;
    private double price;
    private int quantity;

    public CartItemDTO(ItemType type, int definitionId, String name, double price) {
        this.type = type;
        this.definitionId = definitionId;
        this.name = name;
        this.price = price;
        this.quantity = 1;
    }

    public double getSubtotal() {
        return price * quantity;
    }

    public void increment() { quantity++; }

    public void decrement() {
        if (quantity > 1) quantity--;
    }

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
        CartItemDTO cartItemDTO = (CartItemDTO) o;
        return definitionId == cartItemDTO.definitionId && Double.compare(price, cartItemDTO.price) == 0 && quantity == cartItemDTO.quantity && type == cartItemDTO.type && Objects.equals(name, cartItemDTO.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, definitionId, name, price, quantity);
    }

    @Override
    public String toString() {
        return "CartItemDTO{" +
                "type=" + type +
                ", definitionId=" + definitionId +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                '}';
    }
}

