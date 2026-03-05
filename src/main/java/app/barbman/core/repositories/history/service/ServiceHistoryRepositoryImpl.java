package app.barbman.core.repositories.history.service;

import app.barbman.core.dto.history.ServiceDetailDTO;
import app.barbman.core.infrastructure.HibernateUtil;
import jakarta.persistence.EntityManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class ServiceHistoryRepositoryImpl implements ServiceHistoryRepository {

    private static final Logger logger = LogManager.getLogger(ServiceHistoryRepositoryImpl.class);

    private static final String SQL = """
        SELECT sd.displayName, si.quantity, si.unit_price, si.item_total
        FROM service_item si
        JOIN service_header sh ON si.service_header_id = sh.id
        JOIN service_definition sd ON si.service_definition_id = sd.id
        WHERE sh.sale_id = :saleId
        ORDER BY si.id
        """;

    @Override
    public List<ServiceDetailDTO> findServicesBySaleId(int saleId) {
        List<ServiceDetailDTO> list = new ArrayList<>();
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            List<Object[]> rows = em.createNativeQuery(SQL)
                    .setParameter("saleId", saleId)
                    .getResultList();
            for (Object[] row : rows) {
                ServiceDetailDTO dto = new ServiceDetailDTO();
                dto.setName((String) row[0]);
                dto.setQuantity(((Number) row[1]).intValue());
                dto.setUnitPrice(((Number) row[2]).doubleValue());
                dto.setTotal(((Number) row[3]).doubleValue());
                list.add(dto);
            }
        } catch (Exception e) {
            logger.error("[ServiceHistoryRepositoryImpl] Error loading service history for saleId={}: {}",
                    saleId, e.getMessage());
        }
        return list;
    }
}
