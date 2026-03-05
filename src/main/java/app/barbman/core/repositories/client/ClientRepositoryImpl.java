package app.barbman.core.repositories.client;

import app.barbman.core.model.human.Client;
import app.barbman.core.repositories.AbstractHibernateRepository;

public class ClientRepositoryImpl extends AbstractHibernateRepository<Client, Integer>
        implements ClientRepository {

    public ClientRepositoryImpl() {
        super(Client.class);
    }
}
