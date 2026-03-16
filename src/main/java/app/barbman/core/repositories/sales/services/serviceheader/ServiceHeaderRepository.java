package app.barbman.core.repositories.sales.services.serviceheader;

import app.barbman.core.model.sales.services.ServiceHeader;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.util.List;

public interface ServiceHeaderRepository {
    double sumServiceTotalsByUserAndDateRange(int barberId, LocalDate from, LocalDate to);

    void save(ServiceHeader s, EntityManager em);
    void update(ServiceHeader s, EntityManager em);
    void delete(Integer id, EntityManager em);

    ServiceHeader findBySaleId(int saleId);

    void delete(Integer id);
    List<ServiceHeader> findAll();
}
