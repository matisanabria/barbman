package app.barbman.core.service.services;

import app.barbman.core.dto.services.ServiceDTO;
import app.barbman.core.dto.services.ServiceHistoryDTO;
import app.barbman.core.model.PaymentMethod;
import app.barbman.core.model.User;
import app.barbman.core.model.services.Service;
import app.barbman.core.model.services.ServiceItem;
import app.barbman.core.repositories.DbBootstrap;
import app.barbman.core.repositories.paymentmethod.PaymentMethodRepository;
import app.barbman.core.repositories.paymentmethod.PaymentMethodRepositoryImpl;
import app.barbman.core.repositories.services.service.ServiceRepository;
import app.barbman.core.repositories.services.service.ServiceRepositoryImpl;
import app.barbman.core.repositories.services.servicedefinition.ServiceDefinitionRepository;
import app.barbman.core.repositories.services.servicedefinition.ServiceDefinitionRepositoryImpl;
import app.barbman.core.repositories.services.serviceitems.ServiceItemRepository;
import app.barbman.core.repositories.services.serviceitems.ServiceItemRepositoryImpl;
import app.barbman.core.repositories.users.UsersRepository;
import app.barbman.core.repositories.users.UsersRepositoryImpl;
import app.barbman.core.util.NumberFormatterUtil;
import app.barbman.core.util.TextFormatterUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ServicesService {
    private static final Logger logger = LogManager.getLogger(ServicesService.class);
    private static final String PREFIX = "[SERVICES-SERVICE]";

    private final ServiceRepository serviceRepo = new ServiceRepositoryImpl();
    private final ServiceItemRepository itemRepo = new ServiceItemRepositoryImpl();
    private final ServiceDefinitionRepository defRepo = new ServiceDefinitionRepositoryImpl();
    private final UsersRepository userRepo = new UsersRepositoryImpl();
    private final PaymentMethodRepository paymentRepo = new PaymentMethodRepositoryImpl();

    /**
     * Saves a complete Service (header + items) inside a database transaction.
     * If any error occurs during the process, the transaction is rolled back.
     *
     * @param dto ServiceDTO containing the Service and all its associated ServiceItems
     */

    public void saveServiceWithItems(ServiceDTO dto) {
        if (dto == null || !dto.isReadyToSave()) {
            logger.warn("{} Invalid or incomplete DTO. Aborting save operation.", PREFIX);
            throw new IllegalArgumentException("ServiceDTO is incomplete or invalid.");
        }

        Connection conn = null;

        try {
            // Open connection and start transaction
            conn = DbBootstrap.connect();
            conn.setAutoCommit(false); // Sets auto-commit to false, we will confirm transaction manually
            logger.info("{} Starting new transaction for Service save.", PREFIX);

            // Save the main Service record (header)
            Service service = dto.getService();
            serviceRepo.save(service, conn); // Saves service and sets its ID
            logger.info("{} Service header saved successfully (ID={}).", PREFIX, service.getId());

            // Save each ServiceItem linked to the generated service ID
            for (ServiceItem item : dto.getItems()) {
                item.setServiceId(service.getId()); // Gets ID and sets to item
                itemRepo.save(item);
                logger.debug("{} ServiceItem saved -> TypeID={}, Price={} Gs.",
                        PREFIX, item.getServiceTypeId(), item.getPrice());
            }

            // Commit the transaction
            conn.commit();
            logger.info("{} Transaction committed successfully. Service ID={} saved with {} items.",
                    PREFIX, service.getId(), dto.getItems().size());

        } catch (Exception e) {
            logger.error("{} Error saving service with items: {}", PREFIX, e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback transaction on error
                    logger.warn("{} Transaction rolled back due to error.", PREFIX);
                } catch (SQLException ex) {
                    logger.error("{} Failed to rollback transaction: {}", PREFIX, ex.getMessage());
                }
            }
            throw new RuntimeException("Error while saving service with items.", e);
        } finally {
            // Restore connection state and close
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Restore auto-commit
                    conn.close(); // Close connection
                    logger.debug("{} Database connection closed successfully.", PREFIX);
                } catch (SQLException e) {
                    logger.error("{} Failed to close DB connection: {}", PREFIX, e.getMessage());
                }
            }
        }
    }

    /**
     * Fetches all services and maps them to ServiceHistoryDTO for UI display.
     *
     * @return List of ServiceHistoryDTO containing formatted service information
     */
    public List<ServiceHistoryDTO> getServiceHistory() {
        List<Service> allServices = serviceRepo.findAll();
        List<ServiceHistoryDTO> dtoList = new ArrayList<>();

        for (Service s : allServices) {
            User user = userRepo.findById(s.getUserId());
            PaymentMethod pay = paymentRepo.findById(s.getPaymentMethodId());
            List<ServiceItem> items = itemRepo.findByServiceId(s.getId());

            // join service names with " + "
            String joined = items.stream()
                    .map(i -> defRepo.findById(i.getServiceTypeId()).getName())
                    .collect(Collectors.joining(" + "));

            dtoList.add(new ServiceHistoryDTO(
                    TextFormatterUtil.capitalizeFirstLetter(user.getName()),        // E.g., "Juan"
                    joined,                                                         // E.g., "Corte + Afeitado"
                    TextFormatterUtil.capitalizeFirstLetter(pay.getName()),         // E.g., "Cash"
                    NumberFormatterUtil.format(s.getTotal()) + " Gs",               // E.g., "150.000 Gs"
                    s.getDate().toString(),                                         // E.g., "2024-10-05"
                    s.getNotes()                                                    // E.g., "Cliente habitual"
            ));
        }

        Collections.reverse(dtoList); // show most recent first
        return dtoList;
    }

}
