package app.barbman.core.model.cashbox;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cashbox_openings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class CashboxOpening {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(name = "period_start_date")
    private LocalDate periodStartDate;

    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt;

    @Column(name = "opened_by_user_id", nullable = false)
    private Integer openedByUserId;

    @Column(name = "cash_amount", nullable = false)
    private double cashAmount;

    @Column(name = "bank_amount", nullable = false)
    private double bankAmount;

    private String notes;

    @Column(name = "closed")
    @Builder.Default
    private boolean closed = false;
}
