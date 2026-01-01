package app.barbman.core.dto.salecart;

import java.util.Objects;

public class SaleCartItemDTO {

    public enum ItemType { SERVICE, PRODUCT }

    private ItemType type;
    private int referenceId; // service_definition_id or product_id
    private String displayName;
    private double unitPrice;
    private int quantity;

    public SaleCartItemDTO(ItemType type, int referenceId, String displayName, double unitPrice) {
        this.type = type;
        this.referenceId = referenceId;
        this.displayName = displayName;
        this.unitPrice = unitPrice;
        this.quantity = 1; // Starts from 1. Can be changed with methods
    }

    public ItemType getType() {
        return type;
    }
    public void setType(ItemType type) {
        this.type = type;
    }
    public int getReferenceId() {
        return referenceId;
    }
    public void setReferenceId(int referenceId) {
        this.referenceId = referenceId;
    }
    public String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    public double getUnitPrice() {
        return unitPrice;
    }
    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }
    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void increment() {
        quantity++;
    }
    public void decrement() {
        if (quantity > 1) quantity--;
    }
    public double getItemTotal() {
        return unitPrice * quantity;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SaleCartItemDTO that = (SaleCartItemDTO) o;
        return referenceId == that.referenceId && Double.compare(unitPrice, that.unitPrice) == 0 && quantity == that.quantity && type == that.type && Objects.equals(displayName, that.displayName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, referenceId, displayName, unitPrice, quantity);
    }


}

