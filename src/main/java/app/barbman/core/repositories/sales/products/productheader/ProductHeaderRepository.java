package app.barbman.core.repositories.sales.products.productheader;

import app.barbman.core.model.sales.products.ProductHeader;
import app.barbman.core.repositories.GenericRepository;
import jakarta.persistence.EntityManager;

public interface ProductHeaderRepository extends GenericRepository<ProductHeader, Integer> {
    void save(ProductHeader header, EntityManager em);
    void update(ProductHeader header, EntityManager em);
    void delete(Integer id, EntityManager em);

    ProductHeader findBySaleId(int saleId);
}
