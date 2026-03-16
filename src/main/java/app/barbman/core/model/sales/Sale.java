package app.barbman.core.model.sales;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Represents a completed sales transaction.
 * Groups services and/or products into a single cash operation.
 */
@Entity
@Table(name = "sales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private int id;

    @Column(name = "user_id", nullable = false)
    private int userId;

    @Column(name = "client_id")
    private Integer clientId;

    @Column(name = "payment_method_id", nullable = false)
    private int paymentMethodId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private double total;
}
