package app.barbman.core.model;

import java.util.Objects;

/**
 * User payment type:
 * 0 = Undefined (default)
 * 1 = Commission-based (param1 = commission rate, e.g., 0.5 = 50%)
 * 2 = Base salary + commission (param1 = base salary, param2 = commission rate)
 * 3 = Fixed weekly salary (param1 = fixed amount, param2 = not used)
 * 4 = Special case (parameters interpreted according to custom logic)
 */

public class User {
    private int id;
    private String name;
    private String role; // "admin", "user"
    private String pin;
    private int paymentType;
    private double param1; // Parameter 1, meaning depends on paymentType
    private double param2; // Parameter 2, meaning depends on paymentType

    public User() {}

    // Constructor full
    public User(int id, String name, String role, String pin, int paymentType, double param1, double param2){
        this.id = id;
        this.name = name;
        this.role = role;
        this.pin = pin;
        this.paymentType = paymentType;
        this.param1 = param1;
        this.param2 = param2;
    }

    public User(String name, String role, String pin) {
        this.name = name;
        this.role = role;
        this.pin = pin;
        this.paymentType = 0;
        this.param1 = 0f;
        this.param2 = 0f;
    }

    // Getters
    public int getId() {return id;}
    public int getPaymentType() {return paymentType;}
    public String getName() {return name;}
    public String getRole() {return role;}
    public String getPin() {return pin;}
    public double getParam1() {return param1;}
    public double getParam2() {return param2;}

    // Setters
    public void setId(int id) {this.id = id;} // Para cargar desde DB
    public void setPaymentType(int paymentType) {
        if (User.this.paymentType < 0 || User.this.paymentType > 4) {
            throw new IllegalArgumentException("Tipo de cobro inválido.");
        }
        this.paymentType = paymentType;
    }
    public void setName(String name) {this.name = name;}
    public void setRole(String role) {this.role = role;}
    public void setPin(String pin) {
        if (!pin.matches("\\d{4}")) {
            throw new IllegalArgumentException("PIN only can have 4 digits.");
        }
        this.pin = pin;
    }
    public void setParam1(double param1) {this.param1 = param1;}
    public void setParam2(double param2) {this.param2 = param2;}

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id && paymentType == user.paymentType && Double.compare(param1, user.param1) == 0 && Double.compare(param2, user.param2) == 0 && Objects.equals(name, user.name) && Objects.equals(role, user.role) && Objects.equals(pin, user.pin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, paymentType, name, role, pin, param1, param2);
    }
}
