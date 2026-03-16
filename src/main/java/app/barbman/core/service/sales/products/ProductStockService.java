package app.barbman.core.service.sales.products;

import app.barbman.core.dto.salecart.SaleCartDTO;
import app.barbman.core.dto.salecart.SaleCartItemDTO;
import app.barbman.core.repositories.sales.products.product.ProductRepository;
import app.barbman.core.repositories.sales.products.product.ProductRepositoryImpl;
import jakarta.persistence.EntityManager;

public class ProductStockService {

    private final ProductRepository productRepo = new ProductRepositoryImpl();

    public void decreaseStockFromCart(SaleCartDTO cart, EntityManager em) {
        for (SaleCartItemDTO item : cart.getCartItems()) {
            if (item.getType() != SaleCartItemDTO.ItemType.PRODUCT) continue;

            productRepo.decreaseStock(item.getReferenceId(), item.getQuantity(), em);
        }
    }
}
