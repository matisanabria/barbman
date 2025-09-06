package app.barbman.core.service.sueldos;

import app.barbman.core.model.Barbero;
import app.barbman.core.model.Egreso;
import app.barbman.core.model.Sueldo;
import app.barbman.core.repositories.barbero.BarberoRepository;
import app.barbman.core.repositories.sueldos.SueldosRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.util.List;

import static app.barbman.core.service.egresos.EgresosService.egresosRepository;

public class SueldosService {
    private final SueldosRepository sueldosRepository;
    private final BarberoRepository barberoRepository;
    private static final List<String> FORMAS_VALIDAS = List.of("efectivo", "transferencia");
    private static final Logger logger = LogManager.getLogger(SueldosService.class);

    public SueldosService(SueldosRepository repo, BarberoRepository barberoRepository) {
        this.sueldosRepository = repo;
        this.barberoRepository = barberoRepository;
    }

    public void pagarSueldo(int barberoId, double monto, String formaPago) {
        if (barberoId <= 0) throw new IllegalArgumentException("Debe seleccionar un barbero.");
        if (monto <= 0) throw new IllegalArgumentException("El monto debe ser mayor a cero.");
        if (formaPago == null || formaPago.isBlank()) throw new IllegalArgumentException("Debe seleccionar una forma de pago.");

        if (!FORMAS_VALIDAS.contains(formaPago.toLowerCase())) {
            throw new IllegalArgumentException("Forma de pago inválida.");
        }

        Barbero barbero = barberoRepository.findById(barberoId);
        if (barbero == null) {
            throw new IllegalArgumentException("Barbero no encontrado.");
        }

        // Calcular semana actual (lunes a sábado)
        LocalDate hoy = LocalDate.now();
        LocalDate inicioSemana = hoy.with(java.time.DayOfWeek.MONDAY);
        LocalDate finSemana = hoy.with(java.time.DayOfWeek.SATURDAY);

        String tipoCobroSnapshot = String.valueOf(barbero.getTipoCobro());
        Sueldo sueldo = new Sueldo(
                barberoId,
                inicioSemana,
                finSemana,
                0.0,                // producción total (en beta la omitimos o la cargás antes)
                monto,              // monto liquidado
                tipoCobroSnapshot, // snapshot del tipo de cobro actual
                hoy,                // fecha de pago
                formaPago
        );
        sueldosRepository.save(sueldo);

        // Crear egreso
        String descripcion = "Pago de sueldo a " + barbero.getNombre() + " (semana del " +
                inicioSemana + " al " + finSemana + ")";
        Egreso egreso = new Egreso(
                descripcion,
                monto,
                hoy,
                "sueldo",
                formaPago
        );
        egresosRepository.save(egreso);


        logger.info("Sueldo pagado a {} por Gs. {} con forma de pago '{}'",
                barbero.getNombre(), monto, formaPago);
    }

}
