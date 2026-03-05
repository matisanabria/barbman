package app.barbman.core.model.cashbox;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
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

    @Column(name = "period_start_date", nullable = false, unique = true)
    private LocalDate periodStartDate;

    @Column(name = "period_end_date", nullable = false)
    private LocalDate periodEndDate;

    @Column(name = "closed_at", nullable = false)
    private LocalDateTime closedAt;

    @Column(name = "closed_by_user_id", nullable = false)
    private Integer closedByUserId;

    @Column(name = "expected_cash", nullable = false)
    private double expectedCash;

    @Column(name = "expected_bank", nullable = false)
    private double expectedBank;

    @Column(name = "expected_total", nullable = false)
    private double expectedTotal;

    private String notes;
}
