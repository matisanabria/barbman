package app.barbman.core.service.paymentmethods;

import app.barbman.core.model.PaymentMethod;
import app.barbman.core.repositories.paymentmethod.PaymentMethodRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Handles logic related to payment methods (e.g. Cash, POS, Transfer).
 * Provides CRUD operations and acts as the abstraction between controller and repository.
 */
public class PaymentMethodsService {

    private static final Logger logger = LogManager.getLogger(PaymentMethodsService.class);
    private static final String PREFIX = "[PAYMENTMETHODS-SERVICE]";

    private final PaymentMethodRepository paymentMethodRepository;

    public PaymentMethodsService(PaymentMethodRepository paymentMethodRepository) {
        this.paymentMethodRepository = paymentMethodRepository;
    }

    /**
     * Fetch all payment methods from the repository.
     */
    public List<PaymentMethod> getAllPaymentMethods() {
        logger.info("{} Fetching all payment methods...", PREFIX);
        List<PaymentMethod> methods = paymentMethodRepository.findAll();
        logger.info("{} {} payment methods loaded.", PREFIX, methods.size());
        return methods;
    }

    /**
     * Find a single payment method by ID.
     */
    public PaymentMethod getPaymentMethodById(int id) {
        return paymentMethodRepository.findById(id);
    }

    /**
     * Find a payment method by name.
     */
    public PaymentMethod getPaymentMethodByName(String name) {
        return paymentMethodRepository.findByName(name);
    }

}
