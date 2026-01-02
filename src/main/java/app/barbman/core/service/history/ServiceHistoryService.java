package app.barbman.core.service.history;

import app.barbman.core.dto.history.SaleHistoryDTO;

import java.util.List;

public class ServiceHistoryService {
    public List<SaleHistoryDTO> getServiceHistory() {
        return serviceHistoryRepository.findAll();
    }
}
