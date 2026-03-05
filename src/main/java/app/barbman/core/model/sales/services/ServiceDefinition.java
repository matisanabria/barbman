package app.barbman.core.model.sales.services;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "service_definition")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class ServiceDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private int id;

    @Column(name = "displayName", nullable = false, unique = true)
    private String name;

    @Column(name = "base_price", nullable = false)
    private double basePrice;

    @Column(nullable = false)
    private boolean available = true;
}
