package app.barbman.core.service.history;

import app.barbman.core.dto.history.SaleHistoryDTO;

import java.util.List;

public class ProductHistoryService {
    public List<SaleHistoryDTO> getProductHistory() {
        return productHistoryRepository.findAll();
    }
}
