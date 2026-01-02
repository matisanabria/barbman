package app.barbman.core.model.human;

import java.util.Objects;

/**
 * User payment type:
 * 0 = Undefined (default)
 * 1 = Commission-based (param1 = commission rate, e.g., 0.5 = 50%)
 * 2 = Base salary + commission (param1 = base salary, param2 = commission rate)
 * 3 = Fixed salary (param1 = fixed amount)
 * 4 = Special case (custom logic)
 */
public class User {

    public enum PayFrequency {
        DAILY,
        WEEKLY,
        BIWEEKLY,
        MONTHLY
    }

    private int id;
    private String name;
    private String role; // admin, user
    private String pin;

    private int paymentType;
    private PayFrequency payFrequency;

    private double param1;
    private double param2;

    /* =========================
       CONSTRUCTORS
       ========================= */

    // Empty constructor (DB / frameworks)
    public User() {}

    // Minimal constructor (new user)
    public User(String name, String role, String pin) {
        this.name = name;
        this.role = role;
        setPin(pin);

        this.paymentType = 0; // Undefined
        this.payFrequency = PayFrequency.WEEKLY; // default
        this.param1 = 0;
        this.param2 = 0;
    }

    // Full constructor (DB read)
    public User(
            int id,
            String name,
            String role,
            String pin,
            int paymentType,
            PayFrequency payFrequency,
            double param1,
            double param2
    ) {
        this.id = id;
        this.name = name;
        this.role = role;
        setPin(pin);
        setPaymentType(paymentType);
        this.payFrequency = payFrequency;
        this.param1 = param1;
        this.param2 = param2;
    }


    public int getId() { return id; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getPin() { return pin; }

    public int getPaymentType() { return paymentType; }
    public PayFrequency getPayFrequency() { return payFrequency; }

    public double getParam1() { return param1; }
    public double getParam2() { return param2; }


    public void setId(int id) { this.id = id; }

    public void setName(String name) { this.name = name; }

    public void setRole(String role) { this.role = role; }

    public void setPin(String pin) {
        if (!pin.matches("\\d{4}")) {
            throw new IllegalArgumentException("PIN must have exactly 4 digits.");
        }
        this.pin = pin;
    }

    public void setPaymentType(int paymentType) {
        if (paymentType < 0 || paymentType > 4) {
            throw new IllegalArgumentException("Invalid payment type.");
        }
        this.paymentType = paymentType;
    }

    public void setPayFrequency(PayFrequency payFrequency) {
        if (payFrequency == null) {
            throw new IllegalArgumentException("Pay frequency cannot be null.");
        }
        this.payFrequency = payFrequency;
    }

    public void setParam1(double param1) { this.param1 = param1; }

    public void setParam2(double param2) { this.param2 = param2; }

    /* =========================
       EQUALS / HASHCODE
       ========================= */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return id == user.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
