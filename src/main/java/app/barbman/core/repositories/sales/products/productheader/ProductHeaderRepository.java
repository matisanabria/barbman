package app.barbman.core.repositories.sales.products.productheader;

import app.barbman.core.model.sales.products.ProductHeader;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;

public interface ProductHeaderRepository {
    void save(ProductHeader header, EntityManager em);
    void update(ProductHeader header, EntityManager em);
    void delete(Integer id, EntityManager em);

    ProductHeader findBySaleId(int saleId);

    double sumProductTotalsByUserAndDateRange(int userId, LocalDate from, LocalDate to);

    void delete(Integer id);
    java.util.List<ProductHeader> findAll();
}
