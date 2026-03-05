package app.barbman.core.model.salaries;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "salaries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Salary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private int id;

    @Column(name = "user_id", nullable = false)
    private int userId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "total_production", nullable = false)
    private double totalProduction;

    @Column(name = "amount_paid", nullable = false)
    private double amountPaid;

    @Column(name = "pay_type_snapshot", nullable = false)
    private int payTypeSnapshot;

    @Column(name = "pay_date")
    private LocalDate payDate;

    @Column(name = "payment_method_id", nullable = false)
    private int paymentMethodId;

    @Column(name = "expense_id")
    private Integer expenseId;
}
