package app.barbman.core.service.pin;

import app.barbman.core.model.User;
import app.barbman.core.repositories.users.UsersRepositoryImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Servicio encargado de gestionar PIN's y de la autenticación mediante ellos
 */
public class PinService {
    private static final Logger logger = LogManager.getLogger(PinService.class);
    private static final UsersRepositoryImpl BARBERO_REPOSITORY_IMPLEMENTS = new UsersRepositoryImpl();

    public static User getSesion(String PIN) {
        logger.info("Login : Recibiendo PIN");
        return BARBERO_REPOSITORY_IMPLEMENTS.findByPin(PIN);
    }



}
