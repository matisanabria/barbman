package app.barbman.core.service.caja;

import app.barbman.core.dto.ResumenDTO;
import app.barbman.core.model.Egreso;
import app.barbman.core.model.ServicioRealizado;
import app.barbman.core.repositories.egresos.EgresosRepository;
import app.barbman.core.repositories.serviciorealizado.ServicioRealizadoRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.util.List;

public class CajaService {
    private static final Logger logger = LogManager.getLogger(CajaService.class);

    private final ServicioRealizadoRepository serviciosRepo;
    private final EgresosRepository egresosRepo;

    public CajaService(ServicioRealizadoRepository serviciosRepo,
                       EgresosRepository egresosRepo) {
        this.serviciosRepo = serviciosRepo;
        this.egresosRepo = egresosRepo;
    }

    /**
     * Calcula resumen diario de caja (ingresos, egresos).
     *
     * @param fecha Fecha para la cual se calcula el resumen
     * @return ResumenDTO con ingresos y egresos del día
     */
    public ResumenDTO calcularResumenDiario(LocalDate fecha) {
        logger.info("Calculando resumen diario para {}", fecha);

        try {
            List<ServicioRealizado> servicios = serviciosRepo.searchByDateRange(fecha, fecha);
            List<Egreso> egresos = egresosRepo.searchByDateRange(fecha, fecha);

            double ingresosTotal = servicios.stream().mapToDouble(ServicioRealizado::getPrecio).sum();
            double egresosTotal = egresos.stream().mapToDouble(Egreso::getMonto).sum();

            // El resto de campos no aplican, se dejan en 0
            return new ResumenDTO(
                    fecha, fecha,
                    ingresosTotal, egresosTotal,
                    0, 0, 0, 0
            );
        } catch (Exception e) {
            logger.error("Error al calcular resumen diario para {}: {}", fecha, e.getMessage(), e);
            throw new RuntimeException("No se pudo calcular el resumen diario", e);
        }
    }

    /**
     * Calcula un resumen de caja en un rango semanal.
     * Solo refleja el total de ingresos y egresos,
     * sin desglose por forma de pago ni saldo acumulado.
     *
     * @param desde Fecha de inicio de la semana (inclusive)
     * @param hasta Fecha de fin de la semana (inclusive)
     * @return ResumenDTO con ingresos y egresos del rango
     */
    public ResumenDTO calcularResumenSemanal(LocalDate desde, LocalDate hasta) {
        logger.info("Calculando cierre semanal desde {} hasta {}", desde, hasta);

        try {
            // 1. Traer servicios y egresos en el rango
            List<ServicioRealizado> servicios = serviciosRepo.searchByDateRange(desde, hasta);
            List<Egreso> egresos = egresosRepo.searchByDateRange(desde, hasta);

            // 2. Totales
            double ingresosTotal = servicios.stream()
                    .mapToDouble(ServicioRealizado::getPrecio)
                    .sum();

            double egresosTotal = egresos.stream()
                    .mapToDouble(Egreso::getMonto)
                    .sum();

            logger.debug("Ingresos semanales calculados: {}", ingresosTotal);
            logger.debug("Egresos semanales calculados: {}", egresosTotal);

            // 3. Crear DTO (los demás campos van en 0 porque no aplican en cierre semanal)
            return new ResumenDTO(
                    desde, hasta,
                    ingresosTotal, egresosTotal,
                    0, // saldoFinal no aplica
                    0, // efectivo no aplica
                    0, // transferencia no aplica
                    0  // pos no aplica
            );

        } catch (Exception e) {
            logger.error("Error al calcular cierre semanal entre {} y {}: {}", desde, hasta, e.getMessage(), e);
            throw new RuntimeException("No se pudo calcular el cierre semanal", e);
        }
    }

}
