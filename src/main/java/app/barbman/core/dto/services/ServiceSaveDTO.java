package app.barbman.core.dto.services;

import app.barbman.core.model.services.Service;
import app.barbman.core.model.services.ServiceItem;

import java.util.List;

public class ServiceSaveDTO {

    private final Service service;
    private final List<ServiceItem> items;

    public ServiceSaveDTO(Service service, List<ServiceItem> items) {
        this.service = service;
        this.items = items;
    }

    public Service getService() {
        return service;
    }

    public List<ServiceItem> getItems() {
        return items;
    }

    public boolean isReadyToSave() {
        return service != null
                && items != null
                && !items.isEmpty()
                && service.getTotal() > 0;
    }
}
