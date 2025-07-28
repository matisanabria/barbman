package app.barbman.onbarber.service.pin;

import app.barbman.onbarber.util.LoggerUtil;

import java.util.logging.Logger;

/**
 * Servicio para chequear entrada de PIN en pantalla de bloqueo.
 * Recibe PIN, lo valida, chequea si coincide con algún barbero.
 */
public class PinValidator {
    private static final Logger logger = LoggerUtil.getLogger(PinValidator.class);
    String PIN= "pepe";

    void validator(){
        if(PIN.length()==4){

        }else{
            logger.warning("PIN inválido");
        }
    }



}
