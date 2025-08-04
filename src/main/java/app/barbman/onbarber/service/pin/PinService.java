package app.barbman.onbarber.service.pin;

import java.util.logging.Logger;
import app.barbman.onbarber.controller.PinController;
import app.barbman.onbarber.model.Barbero;
import app.barbman.onbarber.repository.BarberoRepository;
import app.barbman.onbarber.util.LoggerUtil;

/**
 * Servicio encargado de gestionar PIN's y de la autenticación
 * mediante ellos
 */
public class PinService {
    private static final Logger logger = LoggerUtil.getLogger(PinService.class);
    private static final BarberoRepository barberoRepository = new BarberoRepository();
    private static final PinController pinController = new PinController();

    public static Barbero getSesion(String PIN) {
        return barberoRepository.getBarberoWithPin(PIN);
    }




}
