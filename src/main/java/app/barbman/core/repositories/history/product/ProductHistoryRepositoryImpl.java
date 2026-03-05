package app.barbman.core.repositories.history.product;

import app.barbman.core.dto.history.ProductDetailDTO;
import app.barbman.core.infrastructure.HibernateUtil;
import jakarta.persistence.EntityManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class ProductHistoryRepositoryImpl implements ProductHistoryRepository {

    private static final Logger logger = LogManager.getLogger(ProductHistoryRepositoryImpl.class);

    private static final String SQL = """
        SELECT p.displayName, psi.quantity, psi.unit_price, psi.item_total
        FROM product_sale_items psi
        JOIN product_sales ps ON psi.product_header_id = ps.id
        JOIN products p ON psi.product_id = p.id
        WHERE ps.sale_id = :saleId
        ORDER BY psi.id
        """;

    @Override
    public List<ProductDetailDTO> findProductsBySaleId(int saleId) {
        List<ProductDetailDTO> list = new ArrayList<>();
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            List<Object[]> rows = em.createNativeQuery(SQL)
                    .setParameter("saleId", saleId)
                    .getResultList();
            for (Object[] row : rows) {
                ProductDetailDTO dto = new ProductDetailDTO();
                dto.setName((String) row[0]);
                dto.setQuantity(((Number) row[1]).intValue());
                dto.setUnitPrice(((Number) row[2]).doubleValue());
                dto.setTotal(((Number) row[3]).doubleValue());
                list.add(dto);
            }
        } catch (Exception e) {
            logger.error("[ProductHistoryRepositoryImpl] Error loading product history for saleId={}: {}",
                    saleId, e.getMessage());
        }
        return list;
    }
}
