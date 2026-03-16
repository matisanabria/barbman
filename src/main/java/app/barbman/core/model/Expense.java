package app.barbman.core.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Expense types:
 *   supply   — restocking inventory
 *   service  — cleaning, rent, electricity
 *   purchase — furniture, tools
 *   tax      — taxes, fees, licenses
 *   other    — miscellaneous
 *   salary   — employee wages (set automatically by the salary module)
 *   advance  — advance payments to employees (set automatically)
 */
@Entity
@Table(name = "expenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private int id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String type;

    @Column(name = "payment_method_id", nullable = false)
    private int paymentMethodId;
}
