package app.barbman.core.repositories.sales;

import app.barbman.core.dto.history.SaleDetailDTO;
import app.barbman.core.dto.history.SaleHistoryDTO;
import app.barbman.core.infrastructure.HibernateUtil;
import app.barbman.core.model.sales.Sale;
import app.barbman.core.repositories.AbstractHibernateRepository;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SaleRepositoryImpl extends AbstractHibernateRepository<Sale, Integer>
        implements SaleRepository {

    public SaleRepositoryImpl() {
        super(Sale.class);
    }

    @Override
    public List<SaleHistoryDTO> findSalesHistory(LocalDate from, LocalDate to) {
        String sql = """
            SELECT s.id, s.date, u.displayName, c.displayName, s.total, pm.displayName
            FROM sales s
            JOIN users u ON u.id = s.user_id
            LEFT JOIN clients c ON c.id = s.client_id
            JOIN payment_methods pm ON pm.id = s.payment_method_id
            WHERE s.date BETWEEN :from AND :to
            ORDER BY s.date DESC, s.id DESC
            """;

        List<SaleHistoryDTO> list = new ArrayList<>();
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            List<Object[]> rows = em.createNativeQuery(sql)
                    .setParameter("from", from.toString())
                    .setParameter("to", to.toString())
                    .getResultList();
            for (Object[] row : rows) {
                SaleHistoryDTO dto = new SaleHistoryDTO();
                dto.setSaleId(((Number) row[0]).intValue());
                dto.setDate(LocalDate.parse((String) row[1]));
                dto.setUserName((String) row[2]);
                dto.setClientName((String) row[3]);
                dto.setTotal(((Number) row[4]).doubleValue());
                dto.setPaymentMethod((String) row[5]);
                dto.setPaid(true);
                list.add(dto);
            }
        } catch (Exception e) {
            logger.error("[SaleRepositoryImpl] Error loading sales history: {}", e.getMessage());
        }
        return list;
    }

    @Override
    public SaleDetailDTO findSaleHeaderDetail(int saleId) {
        String sql = """
            SELECT s.id, s.date, u.displayName, c.displayName, pm.displayName, s.total
            FROM sales s
            JOIN users u ON u.id = s.user_id
            LEFT JOIN clients c ON c.id = s.client_id
            JOIN payment_methods pm ON pm.id = s.payment_method_id
            WHERE s.id = :id
            """;

        try (EntityManager em = HibernateUtil.createEntityManager()) {
            List<Object[]> rows = em.createNativeQuery(sql)
                    .setParameter("id", saleId)
                    .getResultList();
            if (!rows.isEmpty()) {
                Object[] row = rows.get(0);
                SaleDetailDTO dto = new SaleDetailDTO();
                dto.setSaleId(((Number) row[0]).intValue());
                dto.setDate(LocalDate.parse((String) row[1]));
                dto.setUserName((String) row[2]);
                dto.setClientName((String) row[3]);
                dto.setPaymentMethod((String) row[4]);
                dto.setTotal(((Number) row[5]).doubleValue());
                return dto;
            }
        } catch (Exception e) {
            logger.error("[SaleRepositoryImpl] Error loading sale detail {}: {}", saleId, e.getMessage());
        }
        return null;
    }

    @Override
    public double sumTotalByPaymentMethodAndPeriod(int paymentMethodId, LocalDate start, LocalDate end) {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            Double result = em.createQuery(
                    "SELECT SUM(s.total) FROM Sale s WHERE s.paymentMethodId = :pm AND s.date BETWEEN :start AND :end",
                    Double.class)
                    .setParameter("pm", paymentMethodId)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getSingleResult();
            return result != null ? result : 0.0;
        } catch (Exception e) {
            logger.error("[SaleRepositoryImpl] Error summing sales by payment method: {}", e.getMessage());
            return 0.0;
        }
    }

    @Override
    public double sumTotalByPeriod(LocalDate start, LocalDate end) {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            Double result = em.createQuery(
                    "SELECT SUM(s.total) FROM Sale s WHERE s.date BETWEEN :start AND :end",
                    Double.class)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getSingleResult();
            return result != null ? result : 0.0;
        } catch (Exception e) {
            logger.error("[SaleRepositoryImpl] Error summing sales by period: {}", e.getMessage());
            return 0.0;
        }
    }
}
