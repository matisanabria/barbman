package app.barbman.core.repositories.services.service;

import app.barbman.core.model.services.Service;
import app.barbman.core.repositories.GenericRepository;


import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;

public interface ServiceRepository extends GenericRepository<Service, Integer> {
    double getProduccionSemanalPorBarbero(int barberoId, LocalDate desde, LocalDate hasta);

    // Extended methods with shared connection
    void save(Service s, Connection conn) throws SQLException;
    void update(Service s, Connection conn) throws SQLException;
    void delete(Integer id, Connection conn) throws SQLException;
}