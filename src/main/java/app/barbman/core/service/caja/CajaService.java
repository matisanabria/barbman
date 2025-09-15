package app.barbman.core.service.caja;

import app.barbman.core.model.CajaDiaria;
import app.barbman.core.model.Egreso;
import app.barbman.core.model.ServicioRealizado;
import app.barbman.core.repositories.DbBootstrap;
import app.barbman.core.repositories.caja.CajaRepository;
import app.barbman.core.repositories.egresos.EgresosRepository;
import app.barbman.core.repositories.serviciorealizado.ServicioRealizadoRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.util.List;

public class CajaService {
    private static final Logger logger = LogManager.getLogger(CajaService.class);

    private final CajaRepository cajaRepo;
    private final ServicioRealizadoRepository serviciosRepo;
    private final EgresosRepository egresosRepo;

    public CajaService(CajaRepository cajaRepo,
                       ServicioRealizadoRepository serviciosRepo,
                       EgresosRepository egresosRepo) {
        this.cajaRepo = cajaRepo;
        this.serviciosRepo = serviciosRepo;
        this.egresosRepo = egresosRepo;
    }

    /**
     * Calcula los totales de ingresos/egresos y saldos de un día.
     * @param fecha Fecha a calcular
     * @return CajaDiaria lista para guardar o mostrar
     */
    public CajaDiaria calcularCierre(LocalDate fecha) {
        // 1. Traer servicios y egresos del día
        List<ServicioRealizado> servicios = serviciosRepo.searchByDateRange(fecha, fecha);
        List<Egreso> egresos = egresosRepo.searchByDateRange(fecha, fecha);

        // 2. Totales
        // Convierte la lista en un Stream, transforma cada ServicioRealizado a su precio (double)
        // usando una referencia de metodo (::), y suma todos los valores resultantes.
        double ingresosTotal = servicios.stream().mapToDouble(ServicioRealizado::getPrecio).sum();
        double egresosTotal = egresos.stream().mapToDouble(Egreso::getMonto).sum();

        // Ingresos por forma de pago
        double ingresosEfectivo = servicios.stream()
                .filter(s -> "efectivo".equalsIgnoreCase(s.getFormaPago()))
                .mapToDouble(ServicioRealizado::getPrecio).sum();

        double ingresosTransferencia = servicios.stream()
                .filter(s -> "transferencia".equalsIgnoreCase(s.getFormaPago()))
                .mapToDouble(ServicioRealizado::getPrecio).sum();

        double ingresosPOS = servicios.stream()
                .filter(s -> "pos".equalsIgnoreCase(s.getFormaPago()))
                .mapToDouble(ServicioRealizado::getPrecio).sum();

        // Egresos por forma de pago
        double egresosEfectivo = egresos.stream()
                .filter(e -> "efectivo".equalsIgnoreCase(e.getFormaPago()))
                .mapToDouble(Egreso::getMonto).sum();

        double egresosTransferencia = egresos.stream()
                .filter(e -> "transferencia".equalsIgnoreCase(e.getFormaPago()))
                .mapToDouble(Egreso::getMonto).sum();

        // 3. Buscar caja anterior
        CajaDiaria anterior = cajaRepo.findUltimaCajaAntes(fecha);

        // 4. Cálculos acumulados
        double saldoFinal = (ingresosTotal - egresosTotal) +
                (anterior != null ? anterior.getSaldoFinal() : 0);

        double efectivo = (ingresosEfectivo - egresosEfectivo) +
                (anterior != null ? anterior.getEfectivo() : 0);

        double transferencia = (ingresosTransferencia - egresosTransferencia) +
                (anterior != null ? anterior.getTransferencia() : 0);

        double pos = ingresosPOS; // no se acumulan los numeros de POS

        // 5. Armar objeto
        return new CajaDiaria(fecha, ingresosTotal, egresosTotal,
                saldoFinal, efectivo, transferencia, pos);
    }

    /**
     * Guarda un cierre en la base de datos, si no existe ya.
     */
    public void guardarCierre(CajaDiaria cierre) {
        // Validar que no exista cierre para esa fecha
        CajaDiaria existente = cajaRepo.findByFecha(cierre.getFecha());
        if (existente != null) {
            logger.warn("Ya existe cierre de caja para {}", cierre.getFecha());
            throw new IllegalStateException("Cierre de caja ya realizado");
        }

        cajaRepo.save(cierre);
        DbBootstrap.backupDatabase();
        logger.info("Cierre de caja guardado para {}", cierre.getFecha());
    }
}
