package app.barbman.onbarber.service.pin;

import app.barbman.onbarber.model.Barbero;
import app.barbman.onbarber.repositories.barbero.BarberoRepositoryImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Servicio encargado de gestionar PIN's y de la autenticación mediante ellos
 */
public class PinService {
    private static final Logger logger = LogManager.getLogger(PinService.class);
    private static final BarberoRepositoryImpl BARBERO_REPOSITORY_IMPLEMENTS = new BarberoRepositoryImpl();

    public static Barbero getSesion(String PIN) {
        logger.info("Login : Recibiendo PIN");
        return BARBERO_REPOSITORY_IMPLEMENTS.findByPin(PIN);
    }



}
