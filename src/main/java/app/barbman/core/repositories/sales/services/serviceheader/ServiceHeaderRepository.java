package app.barbman.core.repositories.sales.services.serviceheader;

import app.barbman.core.model.sales.services.ServiceHeader;
import app.barbman.core.repositories.GenericRepository;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;

public interface ServiceHeaderRepository extends GenericRepository<ServiceHeader, Integer> {
    double sumServiceTotalsByUserAndDateRange(int barberId, LocalDate from, LocalDate to);

    void save(ServiceHeader s, EntityManager em);
    void update(ServiceHeader s, EntityManager em);
    void delete(Integer id, EntityManager em);

    ServiceHeader findBySaleId(int saleId);
}
