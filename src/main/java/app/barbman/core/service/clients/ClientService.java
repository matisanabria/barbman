package app.barbman.core.service.clients;

import app.barbman.core.model.human.Client;
import app.barbman.core.repositories.client.ClientRepository;
import app.barbman.core.util.RucValidator;
import app.barbman.core.util.SessionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class ClientService {

    private static final Logger logger = LogManager.getLogger(ClientService.class);
    private static final String PREFIX = "[CLIENT-SERVICE]";

    private final ClientRepository repo;

    public ClientService(ClientRepository repo) {
        this.repo = repo;
    }

    /**
     * Registers a new client after validating required fields.
     * Only name is required. Phone, email, document, and notes are optional.
     */
    public void registerClient(String name, String document, String phone, String email, String notes) {

        // Only name is required
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Client name is required.");
        }

        Client c = new Client(name, document, phone, email, notes, true);
        repo.save(c);

        logger.info("{} Client registered -> {} (ID {})", PREFIX, name, c.getId());
    }

    public List<Client> findAll() {
        return repo.findAll();
    }

    public Client findById(int id) {
        return repo.findById(id);
    }

    public void update(Client c) {
        repo.update(c);
        logger.info("{} Client updated -> {}", PREFIX, c.getId());
    }

    public void delete(int id) {
        repo.delete(id);
        logger.info("{} Client deleted -> {}", PREFIX, id);
    }
    public void softDelete(int id) {
        Client client = repo.findById(id);
        if (client != null) {
            client.setActive(false);  // Soft delete
            repo.update(client);
            logger.info("{} Client soft deleted (set inactive) -> ID {}", PREFIX, id);
        }
    }
}
