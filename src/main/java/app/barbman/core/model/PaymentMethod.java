package app.barbman.core.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payment_methods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class PaymentMethod {

    @Id
    @EqualsAndHashCode.Include
    private int id;

    @Column(name = "displayName", nullable = false, unique = true)
    private String name;
}
