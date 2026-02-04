package app.barbman.core.repositories.sales;

import app.barbman.core.dto.history.SaleDetailDTO;
import app.barbman.core.dto.history.SaleHistoryDTO;
import app.barbman.core.model.sales.Sale;
import app.barbman.core.repositories.GenericRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface SaleRepository extends GenericRepository<Sale, Integer> {

    void save(Sale sale, Connection conn) throws SQLException;
    void update(Sale sale, Connection conn) throws SQLException;
    void delete(Integer id, Connection conn) throws SQLException;

    List<SaleHistoryDTO> findSalesHistory(LocalDate from, LocalDate to);
    SaleDetailDTO findSaleHeaderDetail(int saleId);
    /**
     * Sum total amount by payment method and date range.
     */
    double sumTotalByPaymentMethodAndPeriod(int paymentMethodId, LocalDate start, LocalDate end);
    /**
     * Sum total amount for all sales within a date range.
     */
    double sumTotalByPeriod(LocalDate start, LocalDate end);
}
