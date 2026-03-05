package app.barbman.core.repositories.sales;

import app.barbman.core.dto.history.SaleDetailDTO;
import app.barbman.core.dto.history.SaleHistoryDTO;
import app.barbman.core.model.sales.Sale;
import app.barbman.core.repositories.GenericRepository;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.util.List;

public interface SaleRepository extends GenericRepository<Sale, Integer> {

    void save(Sale sale, EntityManager em);
    void update(Sale sale, EntityManager em);
    void delete(Integer id, EntityManager em);

    List<SaleHistoryDTO> findSalesHistory(LocalDate from, LocalDate to);
    SaleDetailDTO findSaleHeaderDetail(int saleId);
    double sumTotalByPaymentMethodAndPeriod(int paymentMethodId, LocalDate start, LocalDate end);
    double sumTotalByPeriod(LocalDate start, LocalDate end);
}
