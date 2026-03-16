package app.barbman.core.repositories.sales.products.product;

import app.barbman.core.model.sales.products.Product;
import jakarta.persistence.EntityManager;

import java.util.List;

public interface ProductRepository {

    Product findByName(String name);

    List<Product> findAllInStock();

    void decreaseStock(int productId, int quantity, EntityManager em);

    Product findById(Integer id);
    List<Product> findAll();
    void save(Product entity);
    void update(Product entity);
    void delete(Integer id);
}
