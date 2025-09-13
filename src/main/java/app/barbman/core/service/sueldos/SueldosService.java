package app.barbman.core.service.sueldos;

import app.barbman.core.dto.SueldoDTO;
import app.barbman.core.model.Barbero;
import app.barbman.core.model.Egreso;
import app.barbman.core.model.Sueldo;
import app.barbman.core.repositories.barbero.BarberoRepository;
import app.barbman.core.repositories.barbero.BarberoRepositoryImpl;
import app.barbman.core.repositories.egresos.EgresosRepository;
import app.barbman.core.repositories.serviciorealizado.ServicioRealizadoRepository;
import app.barbman.core.repositories.sueldos.SueldosRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para gestionar el pago de sueldos a barberos.
 * Incluye lógica para calcular montos según diferentes tipos de cobro.
 */
public class SueldosService {
    private final SueldosRepository sueldosRepository;
    private static final List<String> FORMAS_VALIDAS = List.of("efectivo", "transferencia");
    private static final Logger logger = LogManager.getLogger(SueldosService.class);
    private final ServicioRealizadoRepository servicioRealizadoRepository;
    private final BarberoRepository barberoRepository = new BarberoRepositoryImpl();
    private final EgresosRepository egresosRepository;

    public SueldosService(SueldosRepository repo, ServicioRealizadoRepository servicioRealizadoRepo, EgresosRepository egresosRepository) {
        this.sueldosRepository = repo;
        this.servicioRealizadoRepository = servicioRealizadoRepo;
        this.egresosRepository = egresosRepository;
    }

    /**
     * Paga un sueldo a un barbero y registra el egreso correspondiente.
     *
     * @param sueldo    Sueldo a pagar (debe tener barberoId, fechas y monto ya calculados)
     * @param formaPago Forma de pago ("efectivo" o "transferencia")
     * @throws IllegalArgumentException si los datos son inválidos
     * @throws IllegalStateException    si el sueldo ya fue pagado
     */
    public void pagarSueldo(Sueldo sueldo, String formaPago, double bono) {
        if (sueldo == null) throw new IllegalArgumentException("Sueldo vacío");
        if (formaPago == null || formaPago.isBlank()) throw new IllegalArgumentException("Forma de pago inválida");
        // Verificar si ya se pagó este sueldo
        if (isPagado(sueldo.getBarberoId(), sueldo.getFechaInicioSemana())) {
            throw new IllegalStateException("Este barbero ya tiene un sueldo registrado esta semana.");
        }

        sueldo.setMontoLiquidado(sueldo.getMontoLiquidado() + bono);
        sueldo.setFechaPago(LocalDate.now());
        sueldo.setFormaPago(formaPago);
        sueldosRepository.save(sueldo);

        String descripcion = "Pago de sueldo a barbero ID " + sueldo.getBarberoId() +
                " (semana del " + sueldo.getFechaInicioSemana() + " al " + sueldo.getFechaFinSemana() + ")";

        Egreso egreso = new Egreso(
                descripcion,
                sueldo.getMontoLiquidado(),
                LocalDate.now(),
                "sueldo",
                formaPago
        );
        egresosRepository.save(egreso);

        logger.info("[SUELDOS] Sueldo pagado y egreso registrado para barbero ID {}", sueldo.getBarberoId());
    }

    /**
     * Calcula el sueldo de un barbero para la semana actual.
     *
     * @param barbero Barbero al que se le calcula el sueldo
     * @param bono    Bono adicional a sumar al sueldo (puede ser 0)
     * @return Sueldo calculado (sin fecha de pago ni forma de pago)
     * @throws IllegalArgumentException si el barbero es inválido
     */
    public Sueldo calcularSueldo(Barbero barbero, double bono) {
        if (barbero == null || barbero.getId() <= 0) {
            throw new IllegalArgumentException("Barbero inválido.");
        }

        int tipo = barbero.getTipoCobro();
        double param1 = barbero.getParam1();
        double param2 = barbero.getParam2();

        // Rango semanal
        LocalDate[] semana = obtenerRangoSemanaActual();
        LocalDate inicioSemana = semana[0];
        LocalDate finSemana = semana[1];

        // Producción y adelantos
        double produccion = servicioRealizadoRepository.getProduccionSemanalPorBarbero(barbero.getId(), inicioSemana, finSemana);
        double adelantos = egresosRepository.getTotalAdelantos(barbero.getId(), inicioSemana, finSemana);

        logger.info("[SUELDOS] Calculando sueldo para barbero ID {} (tipoCobro={}, param1={}, param2={}, bono={})",
                barbero.getId(), tipo, param1, param2, bono);
        // Calcular sueldo base
        double montoCalculado = switch (tipo) {
            case 0 -> 0.0; // No cobra
            case 1 -> calcularPorProduccionSemanal(produccion, param1);
            case 2 -> calcularSueldoBaseMasPorcentaje(produccion, param1, param2);
            case 3 -> param1;
            case 4 -> calcularSueldoEspecial(produccion, param1, param2);
            default -> throw new IllegalStateException("Tipo de cobro no definido");
        };

        // Monto final con bono y adelantos
        double montoFinal = montoCalculado + bono - adelantos;

        // Evitar sueldo negativo
        if (montoFinal < 0) {
            // Calculamos la diferencia que queda pendiente
            double deudaPendiente = Math.abs(montoFinal);
            logger.warn("[SUELDOS] El sueldo calculado para barbero ID {} es negativo ({}), se ajusta a 0.",
                    barbero.getId(), montoFinal);

            // Ajustamos el sueldo a 0 para que no se registre sueldo negativo
            montoFinal = 0;

            // Calculamos la fecha del próximo lunes (finSemana es sábado)
            LocalDate lunesProximo = finSemana.plusDays(2); // finSemana es sábado

            // Creamos un egreso tipo "adelanto" para registrar la deuda pendiente como adelanto automático
            String descripcion = "Saldo pendiente arrastrado (barbero ID " + barbero.getId() + ")";
            Egreso egresoPendiente = new Egreso(
                    descripcion,
                    deudaPendiente,
                    lunesProximo,
                    "adelanto",
                    "n/a"    // Marcamos como generado por el sistema
            );

            // Registramos el egreso en la base de datos
            egresosRepository.save(egresoPendiente);
            logger.info("[SUELDOS] Egreso adelantado automático generado: Gs. {}, fecha: {}", deudaPendiente, lunesProximo);
        }

        return new Sueldo(
                barbero.getId(),
                inicioSemana,
                finSemana,
                produccion,
                montoFinal,
                tipo,
                null, // Fecha de pago se asignará en el paso final
                null  // Forma de pago también se asigna al pagar
        );
    }

    /**
     * Obtiene el rango de fechas de la semana actual (lunes a sábado).
     *
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

    /**
     * Verifica si un barbero ya tiene un sueldo pagado en la semana indicada.
     *
     * @param barberoId    ID del barbero
     * @param semanaInicio Fecha de inicio de la semana (lunes)
     * @return true si ya tiene un sueldo registrado, false si no
     */
    public boolean isPagado(int barberoId, LocalDate semanaInicio) {
        return sueldosRepository.findByBarberoAndFecha(barberoId, semanaInicio) != null;
    }

    /**
     * Genera una lista de SueldoDTO para todos los barberos en el rango semanal dado.
     * Incluye producción, monto a liquidar y estado de pago.
     * Se utiliza para mostrar en la vista de sueldos.
     *
     * @param inicio Fecha de inicio de la semana (lunes)
     * @param fin    Fecha de fin de la semana (sábado)
     * @return Lista de SueldoDTO con información para la vista
     */
    public List<SueldoDTO> genSueldoDTOSemanal(LocalDate inicio, LocalDate fin){
        List<SueldoDTO> lista = new ArrayList<>();
        List<Barbero> barberos = barberoRepository.findAll();

        for (Barbero barbero : barberos) {
            int barberoId = barbero.getId();
            String nombre = barbero.getNombre();

            // Producción semanal
            double produccion = servicioRealizadoRepository.getProduccionSemanalPorBarbero(barberoId, inicio, fin);

            // Calcular sueldo base (sin bonos)
            Sueldo sueldoTemp = calcularSueldo(barbero, 0);

            // Consultar adelantos
            double adelantos = egresosRepository.getTotalAdelantos(barberoId, inicio, fin);

            // Aplicar descuento de adelantos
            double montoFinal = sueldoTemp.getMontoLiquidado() - adelantos;

            // Verificar si ya está pagado
            boolean yaPagado = isPagado(barberoId, inicio);
            int sueldoId = yaPagado ? sueldosRepository.findByBarberoAndFecha(barberoId, inicio).getId() : 0;

            SueldoDTO dto = new SueldoDTO();
            dto.setBarberoId(barberoId);
            dto.setNombreBarbero(nombre);
            dto.setProduccionTotal(produccion);
            dto.setMontoLiquidado(montoFinal);
            dto.setPagado(yaPagado);
            dto.setSueldoId(sueldoId);

            lista.add(dto);
        }

        return lista;
    }
}
