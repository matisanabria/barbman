package app.barbman.core.model.sales.products;

import jakarta.persistence.*;
import lombok.*;

/**
 * A single line item within a product sale.
 * Stores the price at the time of sale to preserve historical accuracy.
 */
@Entity
@Table(name = "product_sale_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class ProductSaleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private int id;

    @Column(name = "product_header_id", nullable = false)
    private int productHeaderId;

    @Column(name = "product_id", nullable = false)
    private int productId;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false)
    private double unitPrice;

    @Column(name = "item_total", nullable = false)
    private double itemTotal;
}
