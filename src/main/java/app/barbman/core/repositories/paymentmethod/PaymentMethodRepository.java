package app.barbman.core.repositories.paymentmethod;

import app.barbman.core.model.PaymentMethod;
import java.util.List;

public interface PaymentMethodRepository {
    List<PaymentMethod> findAll();
    PaymentMethod findById(Integer id);
    PaymentMethod findByName(String name);
}
