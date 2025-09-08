package app.barbman.core.service.sueldos;

import app.barbman.core.model.Barbero;
import app.barbman.core.model.Egreso;
import app.barbman.core.model.Sueldo;
import app.barbman.core.repositories.serviciorealizado.ServicioRealizadoRepository;
import app.barbman.core.repositories.sueldos.SueldosRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static app.barbman.core.service.egresos.EgresosService.egresosRepository;

/**
 * Servicio para gestionar el pago de sueldos a barberos.
 * Incluye lógica para calcular montos según diferentes tipos de cobro.
 */
public class SueldosService {
    private final SueldosRepository sueldosRepository;
    private static final List<String> FORMAS_VALIDAS = List.of("efectivo", "transferencia");
    private static final Logger logger = LogManager.getLogger(SueldosService.class);
    private final ServicioRealizadoRepository servicioRealizadoRepository;

    public SueldosService(SueldosRepository repo,  ServicioRealizadoRepository servicioRealizadoRepo) {
        this.sueldosRepository = repo;
        this.servicioRealizadoRepository = servicioRealizadoRepo;
    }

    public void pagarSueldo(Barbero barbero, String formaPago) {
        logger.info("[SUELDOS] Iniciando proceso de pago de sueldo para barbero ID: {}, nombre: {}", barbero.getId(), barbero.getNombre());

        if (barbero.getId() <= 0) throw new IllegalArgumentException("Debe seleccionar un barbero.");
        if (formaPago == null || formaPago.isBlank()) throw new IllegalArgumentException("Debe seleccionar una forma de pago.");
        if (!FORMAS_VALIDAS.contains(formaPago.toLowerCase())) throw new IllegalArgumentException("Forma de pago inválida.");
        if (barbero == null) throw new IllegalArgumentException("Barbero no encontrado.");


        // Obtener datos del barbero
        int tipo = barbero.getTipoCobro();
        double param1 = barbero.getParam1(); double param2 = barbero.getParam2();
        int tipoCobroSnapshot = barbero.getTipoCobro();
        logger.info("[SUELDOS] Tipo de cobro: {}, param1: {}, param2: {}", tipo, param1, param2);

        // Obtener rango de semana actual
        LocalDate[] semana = obtenerRangoSemanaActual();
        LocalDate inicioSemana = semana[0];
        LocalDate finSemana = semana[1];
        // Obtener producción semanal (para ciertos cálculos)
        double produccion = servicioRealizadoRepository.getProduccionSemanalPorBarbero(
                barbero.getId(),
                inicioSemana,
                finSemana
        );
        logger.info("[SUELDOS] Producción semanal del barbero {} entre {} y {}: Gs. {}", barbero.getNombre(), inicioSemana, finSemana, produccion);

        // Calcular monto según tipo de cobro
        double montoCalculado = 0;
        switch (tipo) {
            case 1 -> montoCalculado = calcularPorProduccionSemanal(produccion, param1); // Por producción
            case 2 -> montoCalculado = calcularSueldoBaseMasPorcentaje(produccion, param1, param2); // Sueldo base + %
            case 3 -> montoCalculado = param1; // Sueldo fijo semanal
            case 4 -> montoCalculado = calcularSueldoEspecial(produccion, param1, param2); // Sueldo especial
            case 0 -> throw new IllegalStateException("Tipo de cobro no definido...");
            default -> throw new IllegalStateException("Tipo de cobro no definido...");
        }
        logger.info("[SUELDOS] Monto de sueldo calculado para {}: Gs. {}", barbero.getNombre(), montoCalculado);

        Sueldo sueldo = new Sueldo(
                barbero.getId(),        // id del barbero
                inicioSemana,           // inicio de la semana
                finSemana,              // fin de la semana
                produccion,                // producción total de la semana
                montoCalculado,              // monto liquidado
                tipoCobroSnapshot,          // snapshot del tipo de cobro actual
                LocalDate.now(),                // fecha de pago
                formaPago                   // forma de pago
        );
        sueldosRepository.save(sueldo);
        logger.info("[SUELDOS] Registro de sueldo guardado en la base de datos para {}", barbero.getNombre());

        // Crear egreso
        String descripcion = "Pago de sueldo a " + barbero.getNombre() + " (semana del " +
                inicioSemana + " al " + finSemana + ")";
        Egreso egreso = new Egreso(
                descripcion,    // descripción
                montoCalculado, // monto
                LocalDate.now(),// fecha del egreso
                "sueldo",       // tipo
                formaPago       // forma de pago
        );
        egresosRepository.save(egreso);
        logger.info("[SUELDOS] Egreso registrado: {}", descripcion);

        logger.info("[SUELDOS] Proceso de pago finalizado correctamente para {}", barbero.getNombre());
    }

    /**
     * Obtiene el rango de fechas de la semana actual (lunes a sábado).
     * @return array con dos LocalDate: [0] = lunes, [1] = sábado
     */
    private LocalDate[] obtenerRangoSemanaActual() {
        LocalDate hoy = LocalDate.now();
        LocalDate lunes = hoy.with(DayOfWeek.MONDAY);
        LocalDate sabado = hoy.with(DayOfWeek.SATURDAY);
        return new LocalDate[]{lunes, sabado};
    }

    /**
     * Cálculo de sueldo por producción semanal.
     * Param1 = porcentaje (0.0 a 1.0)
     */
    private double calcularPorProduccionSemanal(double produccion, double porcentaje) {
        logger.info("[SUELDOS] Calculando sueldo por producción: produccion = {}, porcentaje = {}", produccion, porcentaje);
        return produccion * porcentaje;
    }

    /**
     * Cálculo de sueldo base más porcentaje de producción.
     * Param1 = sueldo base
     * Param2 = porcentaje (0.0 a 1.0)
     */
    private double calcularSueldoBaseMasPorcentaje(double produccion, double sueldoBase, double porcentaje) {
        logger.info("[SUELDOS] Calculando sueldo base más porcentaje: produccion = {}, sueldoBase = {}, porcentaje = {}",
                produccion, sueldoBase, porcentaje);
        return sueldoBase + (produccion * porcentaje);
    }

    /**
     * Cálculo de sueldo especial con umbral mínimo.
     * Param1 = umbral mínimo
     * Param2 = porcentaje (0.0 a 1.0)
     */
    private double calcularSueldoEspecial(double produccion, double umbralMinimo, double porcentaje) {
        logger.info("[SUELDOS] Calculando sueldo especial: produccion = {}, umbralMinimo = {}, porcentaje = {}",
                produccion, umbralMinimo, porcentaje);
        if (produccion < umbralMinimo) {
            return umbralMinimo;
        } else {
            return produccion * porcentaje;
        }
    }


}
