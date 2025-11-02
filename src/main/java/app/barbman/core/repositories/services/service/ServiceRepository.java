package app.barbman.core.repositories.services.service;

import app.barbman.core.model.services.Service;
import app.barbman.core.repositories.GenericRepository;


import java.time.LocalDate;

public interface ServiceRepository extends GenericRepository<Service, Integer> {
    double getProduccionSemanalPorBarbero(int barberoId, LocalDate desde, LocalDate hasta);
}