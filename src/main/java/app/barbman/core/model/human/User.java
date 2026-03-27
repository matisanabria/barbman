package app.barbman.core.model.human;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents an application user (barber or admin).
 *
 * Payment type semantics:
 *   0 = Undefined
 *   1 = Commission-based       (param1 = commission rate, e.g. 0.5 = 50%)
 *   2 = Base salary+commission (param1 = base, param2 = commission rate)
 *   3 = Fixed salary           (param1 = fixed amount)
 *   4 = Special / custom
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class User {

    public enum PayFrequency { DAILY, WEEKLY, BIWEEKLY, MONTHLY }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private int id;

    @Column(name = "displayName", nullable = false)
    @ToString.Include
    private String name;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false, unique = true)
    private String pin;

    @Column(name = "payment_type", nullable = false)
    private int paymentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "pay_frequency", nullable = false)
    private PayFrequency payFrequency = PayFrequency.WEEKLY;

    @Column(name = "param_1")
    private Double param1;

    @Column(name = "param_2")
    private Double param2;

    @Column(name = "avatar_path")
    private String avatarPath = "default.png";

    // ---- Custom setters with validation ----

    public void setPin(String pin) {
        if (!pin.matches("\\d{4}")) {
            throw new IllegalArgumentException("PIN must have exactly 4 digits.");
        }
        this.pin = pin;
    }

    public void setPaymentType(int paymentType) {
        if (paymentType < 0 || paymentType > 4) {
            throw new IllegalArgumentException("Invalid payment type: " + paymentType);
        }
        this.paymentType = paymentType;
    }

    public void setPayFrequency(PayFrequency payFrequency) {
        if (payFrequency == null) throw new IllegalArgumentException("Pay frequency cannot be null.");
        this.payFrequency = payFrequency;
    }

    public void setAvatarPath(String avatarPath) {
        this.avatarPath = (avatarPath != null && !avatarPath.isBlank()) ? avatarPath : "default.png";
    }
}
