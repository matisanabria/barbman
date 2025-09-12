package app.barbman.core.service.egresos;

import app.barbman.core.model.Egreso;
import app.barbman.core.repositories.egresos.EgresosRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;

/**
 * Servicio para gestionar egresos.
 * Permite agregar nuevos egresos y validar los datos ingresados.
 */
public class EgresosService {

    private static final Logger logger = LogManager.getLogger(EgresosService.class);

    public static EgresosRepository egresosRepository;
    public EgresosService(EgresosRepository repo) {
        egresosRepository = repo;
    }

    /**
     * Agrega un nuevo egreso al sistema.
     * @param tipo Tipo de egreso (obligatorio)
     * @param monto Monto del egreso, debe ser mayor a cero (obligatorio)
     * @param descripcion Descripción del egreso, opcional, pero no debe superar 500 caracteres
     * @param formaPago Método de pago utilizado (obligatorio)
     */
    public void addEgreso(String tipo, double monto, String descripcion, String formaPago) {
        if (tipo == null || tipo.isBlank()) throw new IllegalArgumentException("Debe seleccionar un tipo de egreso.");
        if (formaPago == null || formaPago.isBlank()) throw new IllegalArgumentException("Debe seleccionar el metodo de pago.");
        if (monto <= 0) throw new IllegalArgumentException("El monto debe ser mayor a cero.");
        if (descripcion != null && descripcion.length() > 500) {
            throw new IllegalArgumentException("La descripción no debe superar los 500 caracteres.");
        }
        LocalDate hoy = LocalDate.now();

        Egreso egreso = new Egreso(descripcion, monto, hoy, tipo,formaPago);
        egresosRepository.save(egreso);

        logger.info("Egreso registrado -> Tipo: {}, Monto: {}, Forma de pago: {}, Fecha: {}",
                tipo, monto, formaPago, hoy);
    }

    /**
     * Registra un adelanto para un barbero específico.
     * @param barberoId ID del barbero que recibe el adelanto
     * @param monto Monto del adelanto, debe ser mayor a cero
     * @param formaPago Método de pago utilizado para el adelanto
     */
    public void addAdelanto(int barberoId, double monto, String formaPago){
        var hoy = LocalDate.now();
        String descripcion = String.format(
                "Adelanto | barberoID: %d | fecha %s | %s",
                barberoId, hoy.toString(), formaPago
        );

        Egreso egreso = new Egreso();
        egreso.setMonto(monto);
        egreso.setTipo("adelanto");
        egreso.setDescripcion(descripcion);
        egreso.setFecha(hoy);

        egresosRepository.save(egreso);

        logger.info("Adelanto registrado -> Barbero {}, Monto {}, Método {}, Fecha {}",
                barberoId, monto, formaPago, hoy);
    }
}
