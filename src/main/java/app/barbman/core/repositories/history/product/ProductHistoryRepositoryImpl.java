package app.barbman.core.repositories.history.product;

import app.barbman.core.dto.history.ProductDetailDTO;
import app.barbman.core.repositories.DbBootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;


public class ProductHistoryRepositoryImpl implements ProductHistoryRepository {

    private static final Logger logger = LogManager.getLogger(ProductHistoryRepositoryImpl.class);
    private static final String PREFIX = "[PRODUCT-HISTORY-REPO]";

    private static final String SQL = """
        SELECT
            p.displayName        AS name,
            psi.quantity         AS quantity,
            psi.unit_price       AS unit_price,
            psi.item_total       AS total
        FROM product_sale_items psi
        JOIN product_sales ps ON psi.product_header_id = ps.id
        JOIN products p ON psi.product_id = p.id
        WHERE ps.sale_id = ?
        ORDER BY psi.id
        """;

    @Override
    public List<ProductDetailDTO> findProductsBySaleId(int saleId) {

        List<ProductDetailDTO> list = new ArrayList<>();

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(SQL)) {

            ps.setInt(1, saleId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ProductDetailDTO dto = new ProductDetailDTO();
                dto.setName(rs.getString("name"));
                dto.setQuantity(rs.getInt("quantity"));
                dto.setUnitPrice(rs.getDouble("unit_price"));
                dto.setTotal(rs.getDouble("total"));
                list.add(dto);
            }

            logger.info("{} Loaded {} product items for saleId={}",
                    PREFIX, list.size(), saleId);

        } catch (Exception e) {
            logger.error("{} Error loading product history for saleId={}: {}",
                    PREFIX, saleId, e.getMessage());
        }

        return list;
    }
}
