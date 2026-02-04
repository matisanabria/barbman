    package app.barbman.core.repositories.sales.services.serviceheader;

    import app.barbman.core.model.sales.services.ServiceHeader;
    import app.barbman.core.repositories.GenericRepository;


    import java.sql.Connection;
    import java.sql.SQLException;
    import java.time.LocalDate;

    public interface ServiceHeaderRepository extends GenericRepository<ServiceHeader, Integer> {
        double sumServiceTotalsByUserAndDateRange(int barberoId, LocalDate desde, LocalDate hasta);

        // Extended methods with shared connection
        void save(ServiceHeader s, Connection conn) throws SQLException;
        void update(ServiceHeader s, Connection conn) throws SQLException;
        void delete(Integer id, Connection conn) throws SQLException;

        /**
         * Finds the service header for a given sale.
         */
        ServiceHeader findBySaleId(int saleId);
    }