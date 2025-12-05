package app.barbman.core.service.clients;

import app.barbman.core.model.Client;
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
     * Registers a new client after validating required fields and RUC rules (if region is PY).
     */
    public void registerClient(String name, String document, String phone, String email, String notes) {

        // ------- Requisitos mínimos -------
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Client name is required.");

        if (phone == null || phone.isBlank())
            throw new IllegalArgumentException("Client phone number is required.");

        // ------- Validación de RUC según región -------
        if (document != null && !document.isBlank() && SessionManager.isParaguay()) {
            if (!RucValidator.isValidParaguayRuc(document)) {
                throw new IllegalArgumentException("RUC inválido para Paraguay.");
            }
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
}
