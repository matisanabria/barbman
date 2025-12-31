package app.barbman.core.repositories.sales.products.product;

import app.barbman.core.model.sales.products.Product;
import app.barbman.core.repositories.GenericRepository;

import java.util.List;

/**
 * Repository interface for Product records.
 */
public interface ProductRepository extends GenericRepository<Product, Integer> {

    Product findByName(String name);

    List<Product> findAllInStock();
}