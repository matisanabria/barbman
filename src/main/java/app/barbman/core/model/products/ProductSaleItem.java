package app.barbman.core.model.products;

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
    private int saleId;
    private int productId;
    private int quantity;
    private double unitPrice;

    public ProductSaleItem(int saleId, int productId, int quantity, double unitPrice) {
        this.saleId = saleId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public ProductSaleItem(int id, int saleId, int productId, int quantity, double unitPrice) {
        this(saleId, productId, quantity, unitPrice);
        this.id = id;
    }

    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
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

    @Override
    public String toString() {
        return "ProductSaleItem{" +
                "id=" + id +
                ", productId=" + productId +
                ", quantity=" + quantity +
                '}';
    }
}
