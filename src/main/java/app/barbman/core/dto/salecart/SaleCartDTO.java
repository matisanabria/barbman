package app.barbman.core.dto.salecart;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// DTO representing a sale cart on memory before being persisted.
// It contains items (services/products) to be sold, user, client, date, payment method, and notes.
public class SaleCartDTO {

    private int userId;
    private int selectedUserId;// The user performing the sale
    private Integer clientId; // The client for whom the sale is made (nullable)
    private LocalDate date;
    private int paymentMethod;
    private String notes;

    private final List<SaleCartItemDTO> cartItems = new ArrayList<>();

    public SaleCartDTO(int userId) {
        this.userId = userId;
        this.date = LocalDate.now();
    }

    public List<SaleCartItemDTO> getCartItems() {
        return cartItems;
    }



    /**
     * Add Service or Product to the cart.
     * Both methods will call addItem internally.
     */
    public void addService(int definitionId, String name, double price) {
        addItem(SaleCartItemDTO.ItemType.SERVICE, definitionId, name, price);
    }

    public void addProduct(int productId, String name, double price) {
        addItem(SaleCartItemDTO.ItemType.PRODUCT, productId, name, price);
    }
    /**
     * Generic method to add an item to the cart.
     * If an identical item (same type, definitionId, and price) exists, increments its quantity.
     * Otherwise, adds a new item to the cart.
     */
    public void addItem(SaleCartItemDTO.ItemType type, int definitionId, String name, double price){
        for (SaleCartItemDTO item : cartItems) {
            if (item.getReferenceId() == definitionId && item.getType() == type && item.getUnitPrice() == price) {
                item.increment(); // Increment quantity
                return; // end method
            }
        }
        cartItems.add(new SaleCartItemDTO(type, definitionId, name, price));
    }



    /**
     * Remove just ONE unit of the specified item from the cart.
     * If the item's quantity reaches zero, it is removed from the cart entirely.
     */
    public void removeSingleUnit(SaleCartItemDTO target) {
        SaleCartItemDTO found = findItem(target);
        if (found == null) return;

        found.decrement();

        if (found.getQuantity() < 1) {
            cartItems.remove(found);
        }
    }
    /**
     * Remove the specified item entirely from the cart.
     */
    public void removeItem(SaleCartItemDTO target) {
        SaleCartItemDTO found = findItem(target);
        if (found != null) {
            cartItems.remove(found);
        }
    }

    /**
     * Find an item in the cart that matches the target item.
     * @param target
     * @return
     */
    private SaleCartItemDTO findItem(SaleCartItemDTO target) {
        for (SaleCartItemDTO item : cartItems) {
            if (item.equals(target)) return item;
        }
        return null;
    }

    //
    // Get items total
    //
    public double getTotal() {
        return cartItems.stream()
                .mapToDouble(SaleCartItemDTO::getItemTotal)
                .sum();
    }

    public int getUserId() { return userId; }

    public Integer getClientId() { return clientId; }
    public void setClientId(Integer clientId) { this.clientId = clientId; }


    public int getSelectedUserId() {
        return selectedUserId;
    }

    public void setSelectedUserId(int selectedUserId) {
        this.selectedUserId = selectedUserId;
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public int getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(int paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
