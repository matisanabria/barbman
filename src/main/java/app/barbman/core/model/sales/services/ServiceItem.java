package app.barbman.core.model.sales.services;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "service_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class ServiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private int id;

    @Column(name = "service_header_id", nullable = false)
    private int serviceHeaderId;

    @Column(name = "service_definition_id", nullable = false)
    private int serviceDefinitionId;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false)
    private double unitPrice;

    @Column(name = "item_total", nullable = false)
    private double itemTotal;
}
