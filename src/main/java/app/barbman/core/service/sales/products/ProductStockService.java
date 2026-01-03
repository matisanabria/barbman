package app.barbman.core.service.sales.products;

import app.barbman.core.dto.salecart.SaleCartDTO;
import app.barbman.core.dto.salecart.SaleCartItemDTO;
import app.barbman.core.repositories.sales.products.product.ProductRepository;
import app.barbman.core.repositories.sales.products.product.ProductRepositoryImpl;

import java.sql.Connection;

public class ProductStockService {

    private final ProductRepository productRepo =
            new ProductRepositoryImpl();

    public void decreaseStockFromCart(
            SaleCartDTO cart,
            Connection conn
    ) throws Exception {

        for (SaleCartItemDTO item : cart.getCartItems()) {

            if (item.getType() != SaleCartItemDTO.ItemType.PRODUCT) continue;

            productRepo.decreaseStock(
                    item.getReferenceId(), // product_id
                    item.getQuantity(),
                    conn
            );
        }
    }
}
