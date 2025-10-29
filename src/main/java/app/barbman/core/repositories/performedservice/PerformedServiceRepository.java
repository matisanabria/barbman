package app.barbman.core.repositories.performedservice;

import app.barbman.core.model.PerformedService;
import app.barbman.core.repositories.GenericRepository;


import java.time.LocalDate;

public interface PerformedServiceRepository extends GenericRepository<PerformedService, Integer> {
    double getProduccionSemanalPorBarbero(int barberoId, LocalDate desde, LocalDate hasta);
}