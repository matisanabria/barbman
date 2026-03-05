package app.barbman.core.repositories.sales.products.product;

import app.barbman.core.model.sales.products.Product;
import app.barbman.core.repositories.GenericRepository;
import jakarta.persistence.EntityManager;

import java.util.List;

public interface ProductRepository extends GenericRepository<Product, Integer> {

    Product findByName(String name);

    List<Product> findAllInStock();

    void decreaseStock(int productId, int quantity, EntityManager em);
}
