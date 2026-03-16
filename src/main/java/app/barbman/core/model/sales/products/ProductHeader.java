package app.barbman.core.model.sales.products;

import jakarta.persistence.*;
import lombok.*;

/**
 * Header for products sold within a sale. Links to the sales table.
 * Maps to the {@code product_sales} table.
 */
@Entity
@Table(name = "product_sales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class ProductHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private int id;

    @Column(name = "sale_id", nullable = false)
    private int saleId;

    @Column(nullable = false)
    private double subtotal;
}
