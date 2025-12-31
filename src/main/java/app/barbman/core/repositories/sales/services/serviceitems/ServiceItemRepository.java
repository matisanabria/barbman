package app.barbman.core.repositories.sales.services.serviceitems;

import app.barbman.core.model.sales.services.ServiceItem;
import app.barbman.core.repositories.GenericRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface ServiceItemRepository extends GenericRepository<ServiceItem, Integer> {
    List<ServiceItem> findByServiceId(int serviceId);

    // transactional methods
    void save(ServiceItem item, Connection conn) throws SQLException;
    void update(ServiceItem item, Connection conn) throws SQLException;
    void delete(Integer id, Connection conn) throws SQLException;
}
