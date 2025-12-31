package app.barbman.core.model.sales.products;

/**
 * Represents a single line item inside a product sale.
 *
 * Each ProductSaleItem links a specific product to a sale, storing
 * the quantity sold and the final unit price applied at the moment
 * of checkout. This allows historical pricing and supports sales with
 * multiple products or variable product pricing.
 */
public class ProductSaleItem {

    private int id;
    private int productHeaderId;
    private int productId;
    private int quantity;
    private double unitPrice;
    private double itemTotal;

    // For creating new records
    public ProductSaleItem(int productHeaderId, int productId, int quantity, double unitPrice, double itemTotal) {
        this.productHeaderId = productHeaderId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.itemTotal = itemTotal;
    }

    // For reading from DB
    public ProductSaleItem(int id, int productHeaderId, int productId,
                           int quantity, double unitPrice, double itemTotal) {
        this(productHeaderId, productId, quantity, unitPrice, itemTotal);
        this.id = id;
    }

    // Getters / Setters
    public int getId() { return id; }
    public int getProductHeaderId() { return productHeaderId; }
    public int getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
    public double getItemTotal() { return itemTotal; }

    public void setId(int id) { this.id = id; }
    public void setProductHeaderId(int productHeaderId) { this.productHeaderId = productHeaderId; }
    public void setProductId(int productId) { this.productId = productId; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    public void setItemTotal(double itemTotal) { this.itemTotal = itemTotal; }

    @Override
    public String toString() {
        return "ProductSaleItem{" +
                "id=" + id +
                ", productHeaderId=" + productHeaderId +
                ", productId=" + productId +
                ", quantity=" + quantity +
                ", itemTotal=" + itemTotal +
                '}';
    }
}
