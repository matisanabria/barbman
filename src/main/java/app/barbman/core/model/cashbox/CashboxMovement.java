package app.barbman.core.model.cashbox;

import java.time.LocalDateTime;
import java.util.Objects;

public class CashboxMovement {

    private Integer id;

    private String movementType;   // SALE, EXPENSE, OPENING, CLOSURE, ADJUSTMENT
    private String direction;      // IN, OUT

    private double amount;

    private Integer paymentMethodId;

    private String referenceType;  // SALE, EXPENSE, CASHBOX_OPENING, etc.
    private Integer referenceId;

    private String description;

    private Integer userId;

    private LocalDateTime occurredAt;
    private LocalDateTime createdAt;

    public CashboxMovement() {}

    public CashboxMovement(
            String movementType,
            String direction,
            double amount,
            Integer paymentMethodId,
            String referenceType,
            Integer referenceId,
            String description,
            Integer userId,
            LocalDateTime occurredAt
    ) {
        this.movementType = movementType;
        this.direction = direction;
        this.amount = amount;
        this.paymentMethodId = paymentMethodId;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.description = description;
        this.userId = userId;
        this.occurredAt = occurredAt;
    }

    public CashboxMovement(
            Integer id,
            String movementType,
            String direction,
            double amount,
            Integer paymentMethodId,
            String referenceType,
            Integer referenceId,
            String description,
            Integer userId,
            LocalDateTime occurredAt,
            LocalDateTime createdAt
    ) {
        this(movementType, direction, amount, paymentMethodId,
                referenceType, referenceId, description, userId, occurredAt);
        this.id = id;
        this.createdAt = createdAt;
    }

    // Getters
    public Integer getId() { return id; }
    public String getMovementType() { return movementType; }
    public String getDirection() { return direction; }
    public double getAmount() { return amount; }
    public Integer getPaymentMethodId() { return paymentMethodId; }
    public String getReferenceType() { return referenceType; }
    public Integer getReferenceId() { return referenceId; }
    public String getDescription() { return description; }
    public Integer getUserId() { return userId; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setId(Integer id) { this.id = id; }
    public void setMovementType(String movementType) { this.movementType = movementType; }
    public void setDirection(String direction) { this.direction = direction; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setPaymentMethodId(Integer paymentMethodId) { this.paymentMethodId = paymentMethodId; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }
    public void setReferenceId(Integer referenceId) { this.referenceId = referenceId; }
    public void setDescription(String description) { this.description = description; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CashboxMovement)) return false;
        CashboxMovement that = (CashboxMovement) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
