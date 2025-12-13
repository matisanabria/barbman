package app.barbman.core.dto.sale;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CheckoutDTO {

    private int userId;
    private Integer clientId;
    private LocalDate date;
    private int paymentMethod;
    private String notes;

    private final List<CartItemDTO> cartItems = new ArrayList<>();

    public CheckoutDTO(int userId) {
        this.userId = userId;
        this.date = LocalDate.now();
    }

    // ========================
    // CART MANAGEMENT
    // ========================

    public List<CartItemDTO> getCartItems() {
        return cartItems;
    }

    // ----------------------------------------
    // ADD SERVICE (explicit)
    // ----------------------------------------
    public void addService(int definitionId, String name, double price) {
        addItem(CartItemDTO.ItemType.SERVICE, definitionId, name, price);
    }

    // ----------------------------------------
    // ADD PRODUCT (explicit)
    // ----------------------------------------
    public void addProduct(int productId, String name, double price) {
        addItem(CartItemDTO.ItemType.PRODUCT, productId, name, price);
    }

    // ----------------------------------------
    // GENERIC INTERNAL ADD
    // ----------------------------------------
    public void addItem(CartItemDTO.ItemType type,
                        int definitionId,
                        String name,
                        double price) {

        for (CartItemDTO item : cartItems) {
            if (item.getDefinitionId() == definitionId &&
                    item.getType() == type &&
                    item.getPrice() == price) {
                item.increment();
                return;
            }
        }

        cartItems.add(new CartItemDTO(type, definitionId, name, price));
    }

    // ----------------------------------------
    // REMOVE
    // ----------------------------------------
    public void removeSingleUnit(CartItemDTO target) {
        CartItemDTO found = findItem(target);
        if (found == null) return;

        found.decrement();

        if (found.getQuantity() < 1) {
            cartItems.remove(found);
        }
    }

    public void removeItem(CartItemDTO target) {
        CartItemDTO found = findItem(target);
        if (found != null) {
            cartItems.remove(found);
        }
    }

    private CartItemDTO findItem(CartItemDTO target) {
        for (CartItemDTO item : cartItems) {
            if (item.equals(target)) return item;
        }
        return null;
    }

    // ========================
    // TOTAL
    // ========================
    public double getTotal() {
        return cartItems.stream()
                .mapToDouble(CartItemDTO::getSubtotal)
                .sum();
    }

    // ========================
    // GETTERS & SETTERS
    // ========================
    public int getUserId() { return userId; }

    public Integer getClientId() { return clientId; }
    public void setClientId(Integer clientId) { this.clientId = clientId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public int getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(int paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
