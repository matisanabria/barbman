package app.barbman.core.repositories.paymentmethod;

import app.barbman.core.infrastructure.HibernateUtil;
import app.barbman.core.model.PaymentMethod;
import app.barbman.core.repositories.AbstractHibernateRepository;
import jakarta.persistence.EntityManager;

public class PaymentMethodRepositoryImpl extends AbstractHibernateRepository<PaymentMethod, Integer>
        implements PaymentMethodRepository {

    public PaymentMethodRepositoryImpl() {
        super(PaymentMethod.class);
    }

    @Override
    public PaymentMethod findByName(String displayName) {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            return em.createQuery("FROM PaymentMethod WHERE name = :name", PaymentMethod.class)
                    .setParameter("name", displayName)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            logger.warn("[PaymentMethodRepositoryImpl] Error finding payment method by name '{}': {}",
                    displayName, e.getMessage());
            return null;
        }
    }
}
