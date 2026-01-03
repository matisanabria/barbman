package app.barbman.core.repositories.history.service;

import app.barbman.core.dto.history.ServiceDetailDTO;

import java.util.List;

public interface ServiceHistoryRepository {

    List<ServiceDetailDTO> findServicesBySaleId(int saleId);
}