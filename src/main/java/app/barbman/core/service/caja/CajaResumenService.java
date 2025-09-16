package app.barbman.core.service.caja;

import app.barbman.core.dto.ResumenDTO;
import app.barbman.core.model.CajaDiaria;
import app.barbman.core.repositories.caja.CajaRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

public class CajaResumenService {

    private static final Logger logger = LogManager.getLogger(CajaResumenService.class);
    private final CajaRepository cajaRepository;

    public CajaResumenService(CajaRepository cajaRepository) {
        this.cajaRepository = cajaRepository;
    }

    /**
     * Genera un resumen semanal (lunes a domingo) a partir de una fecha de referencia.
     *
     * @param fechaReferencia Fecha desde la cual se calcula la semana.
     * @return ResumenDTO con ingresos, egresos, saldo y desglose de pagos.
     */
    public ResumenDTO getResumenSemanal(LocalDate fechaReferencia) {
        LocalDate inicioSemana = fechaReferencia.with(java.time.DayOfWeek.MONDAY);
        LocalDate finSemana = fechaReferencia.with(java.time.DayOfWeek.SUNDAY);

        logger.info("[CAJA-RESUMEN] Calculando resumen semanal: {} a {}", inicioSemana, finSemana);

        List<CajaDiaria> cajas = cajaRepository.findAll().stream()
                .filter(c -> !c.getFecha().isBefore(inicioSemana) && !c.getFecha().isAfter(finSemana))
                .toList();

        logger.debug("[CAJA-RESUMEN] Se encontraron {} registros de caja en la semana.", cajas.size());

        return buildResumen(inicioSemana, finSemana, cajas);
    }

    /**
     * Genera un resumen mensual (1 al último día del mes) a partir de una fecha de referencia.
     *
     * @param fechaReferencia Fecha desde la cual se calcula el mes.
     * @return ResumenDTO con ingresos, egresos, saldo y desglose de pagos.
     */
    public ResumenDTO getResumenMensual(LocalDate fechaReferencia) {
        LocalDate inicioMes = fechaReferencia.withDayOfMonth(1);
        LocalDate finMes = fechaReferencia.with(TemporalAdjusters.lastDayOfMonth());

        logger.info("[CAJA-RESUMEN] Calculando resumen mensual: {} a {}", inicioMes, finMes);

        List<CajaDiaria> cajas = cajaRepository.findAll().stream()
                .filter(c -> !c.getFecha().isBefore(inicioMes) && !c.getFecha().isAfter(finMes))
                .toList();

        logger.debug("[CAJA-RESUMEN] Se encontraron {} registros de caja en el mes.", cajas.size());

        return buildResumen(inicioMes, finMes, cajas);
    }

    /**
     * Construye un DTO de resumen financiero a partir de un rango de fechas y sus registros de caja.
     */
    private ResumenDTO buildResumen(LocalDate desde, LocalDate hasta, List<CajaDiaria> cajas) {
        double ingresos = cajas.stream().mapToDouble(CajaDiaria::getIngresosTotal).sum();
        double egresos = cajas.stream().mapToDouble(CajaDiaria::getEgresosTotal).sum();
        double efectivo = cajas.stream().mapToDouble(CajaDiaria::getEfectivo).sum();
        double transferencia = cajas.stream().mapToDouble(CajaDiaria::getTransferencia).sum();
        double pos = cajas.stream().mapToDouble(CajaDiaria::getPos).sum();
        double saldo = ingresos - egresos;

        logger.info("[CAJA-RESUMEN] Resumen generado de {} a {} -> Ingresos: {}, Egresos: {}, Saldo: {}",
                desde, hasta, ingresos, egresos, saldo);

        return new ResumenDTO(desde, hasta, ingresos, egresos, saldo, efectivo, transferencia, pos);
    }
}
