package app.barbman.core.model.sales.products;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private int id;

    @Column(name = "displayName", nullable = false, unique = true)
    private String name;

    @Column(name = "cost_price", nullable = false)
    private double costPrice;

    @Column(name = "unit_price", nullable = false)
    private double unitPrice;

    @Column(nullable = false)
    private int stock;

    private String category;
    private String brand;

    @Column(name = "image_path")
    private String imagePath;

    @Builder.Default
    @Column
    private String notes = "";
}
