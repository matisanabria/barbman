package app.barbman.core.service.sales.products;

import app.barbman.core.model.sales.products.Product;
import app.barbman.core.repositories.sales.products.product.ProductRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Provides business logic for managing products in inventory.
 * Acts as an abstraction layer between controllers and the product repository.
 */
public class ProductService {

    private static final Logger logger = LogManager.getLogger(ProductService.class);
    private static final String PREFIX = "[PRODUCT-SERVICE]";

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /** Fetch all products (including out-of-stock). */
    public List<Product> getAll() {
        logger.info("{} Fetching all products...", PREFIX);
        List<Product> products = productRepository.findAll();
        logger.info("{} {} products loaded.", PREFIX, products.size());
        return products;
    }

    /** Fetch only products with stock > 0. */
    public List<Product> getAllInStock() {
        logger.info("{} Fetching products with stock > 0...", PREFIX);
        List<Product> list = productRepository.findAllInStock();
        logger.info("{} {} products available.", PREFIX, list.size());
        return list;
    }

    /** Find a product by ID. */
    public Product getById(int id) {
        return productRepository.findById(id);
    }

    /** Find a product by name. */
    public Product getByName(String name) {
        return productRepository.findByName(name);
    }

    /** Creates a new product entry. */
    public void save(Product p) {
        if (p == null) throw new IllegalArgumentException("Product cannot be null");
        logger.info("{} Saving product {}", PREFIX, p.getName());
        productRepository.save(p);
    }

    /** Updates an existing product. */
    public void update(Product p) {
        if (p == null || p.getId() == 0)
            throw new IllegalArgumentException("Invalid product for update");

        logger.info("{} Updating product ID {}", PREFIX, p.getId());
        productRepository.update(p);
    }

    /** Deletes a product by ID. */
    public void delete(int id) {
        logger.info("{} Deleting product ID {}", PREFIX, id);
        productRepository.delete(id);
    }
    public void softDelete(int id) {
        Product product = productRepository.findById(id);
        if (product != null) {
            product.setStock(0);  // Set stock to 0
            productRepository.update(product);
            logger.info("{} Product soft deleted (stock set to 0) -> ID {}", PREFIX, id);
        }
    }
}
