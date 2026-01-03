package app.barbman.core.repositories.history.product;

import app.barbman.core.dto.history.ProductDetailDTO;

import java.util.List;

public interface ProductHistoryRepository {

    List<ProductDetailDTO> findProductsBySaleId(int saleId);
}
