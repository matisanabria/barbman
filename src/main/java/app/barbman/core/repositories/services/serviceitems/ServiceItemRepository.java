package app.barbman.core.repositories.services.serviceitems;

import app.barbman.core.model.services.ServiceItem;
import app.barbman.core.repositories.GenericRepository;

import java.util.List;

public interface ServiceItemRepository extends GenericRepository<ServiceItem, Integer> {
    List<ServiceItem> findByServiceId(int serviceId);
}
