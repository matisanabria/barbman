package app.barbman.core.service.services;

import app.barbman.core.repositories.services.service.ServiceRepository;

import java.time.LocalDate;

/**
 * Handles general services-related operations.
 * Crud operations for services.
 */
public class ServicesService {

    // Repo used to access persisted services data.
    private final ServiceRepository serviceRepository;

    // Constructor injection
    public ServicesService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    /**
     * Returns the production of a user within a date range.
     *
     * PRODUCTION definition (business rule):
     * Production is the sum of the TOTAL field of all services
     * performed by a user between two dates.
     *
     * This method does NOT calculate anything by itself,
     * it delegates the data access to the repository
     * and gives the returned number its business meaning.
     *
     * @param userId the employee (barber) ID
     * @param from   start date (inclusive)
     * @param to     end date (inclusive)
     * @return total production amount
     */
    public double getProductionByUserAndDateRange(int userId, LocalDate from, LocalDate to) {
        return serviceRepository.sumServiceTotalsByUserAndDateRange(userId, from, to);
    }
}
