package app.barbman.core.model.cashbox;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cashbox_closures")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class CashboxClosure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(name = "opening_id", nullable = false)
    private Integer openingId;

    @Column(name = "closed_at", nullable = false)
    private LocalDateTime closedAt;

    @Column(name = "closed_by_user_id", nullable = false)
    private Integer closedByUserId;

    @Column(name = "expected_cash", nullable = false)
    private double expectedCash;

    @Column(name = "expected_bank", nullable = false)
    private double expectedBank;

    @Column(name = "actual_cash", nullable = false)
    private double actualCash;

    @Column(name = "actual_bank", nullable = false)
    private double actualBank;

    @Column(name = "cash_discrepancy", nullable = false)
    private double cashDiscrepancy;

    @Column(name = "bank_discrepancy", nullable = false)
    private double bankDiscrepancy;

    private String notes;
}
