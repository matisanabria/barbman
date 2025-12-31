package app.barbman.core.model.sales.products;

/**
 * Represents the header for products sold within a sale.
 * Links to sales table for unified sales tracking.
 */
public class ProductHeader {

    private int id;
    private int saleId;
    private double subtotal;

    // For creating new records
    public ProductHeader(int saleId, double subtotal) {
        this.saleId = saleId;
        this.subtotal = subtotal;
    }

    // For reading from DB
    public ProductHeader(int id, int saleId, double subtotal) {
        this(saleId, subtotal);
        this.id = id;
    }

    // Getters
    public int getId() { return id; }
    public int getSaleId() { return saleId; }
    public double getSubtotal() { return subtotal; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setSaleId(int saleId) { this.saleId = saleId; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    @Override
    public String toString() {
        return "ProductHeader{" +
                "id=" + id +
                ", saleId=" + saleId +
                ", subtotal=" + subtotal +
                '}';
    }
}