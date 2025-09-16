package app.barbman.core.service.caja;

import app.barbman.core.model.CajaDiaria;
import app.barbman.core.repositories.caja.CajaRepository;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

public class CajaResumenService {

    private final CajaRepository cajaRepository;

    public CajaResumenService(CajaRepository cajaRepository) {
        this.cajaRepository = cajaRepository;
    }

    // Resumen diario
    public CajaDiaria getResumenDiario(LocalDate fecha) {
        return cajaRepository.findByFecha(fecha);
    }

    // Resumen semanal (lunes a domingo)
    public List<CajaDiaria> getResumenSemanal(LocalDate fechaReferencia) {
        LocalDate inicioSemana = fechaReferencia.with(java.time.DayOfWeek.MONDAY);
        LocalDate finSemana = fechaReferencia.with(java.time.DayOfWeek.SUNDAY);

        return cajaRepository.findAll().stream()
                .filter(c -> !c.getFecha().isBefore(inicioSemana) && !c.getFecha().isAfter(finSemana))
                .collect(Collectors.toList());
    }

    // Resumen mensual (1 al último día del mes)
    public List<CajaDiaria> getResumenMensual(LocalDate fechaReferencia) {
        LocalDate inicioMes = fechaReferencia.withDayOfMonth(1);
        LocalDate finMes = fechaReferencia.with(TemporalAdjusters.lastDayOfMonth());

        return cajaRepository.findAll().stream()
                .filter(c -> !c.getFecha().isBefore(inicioMes) && !c.getFecha().isAfter(finMes))
                .collect(Collectors.toList());
    }
}
