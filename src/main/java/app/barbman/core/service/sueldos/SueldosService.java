package app.barbman.core.service.sueldos;

import app.barbman.core.dto.SalaryDTO;
import app.barbman.core.model.Salary;
import app.barbman.core.model.User;
import app.barbman.core.model.Egreso;
import app.barbman.core.repositories.users.UsersRepository;
import app.barbman.core.repositories.users.UsersRepositoryImpl;
import app.barbman.core.repositories.egresos.EgresosRepository;
import app.barbman.core.repositories.serviciorealizado.ServicioRealizadoRepository;
import app.barbman.core.repositories.salaries.SalariesRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para gestionar el pago de salaries a barberos.
 * Incluye lógica para calcular montos según diferentes tipos de cobro.
 */
public class SueldosService {
    private final SalariesRepository salariesRepository;
    private static final List<String> FORMAS_VALIDAS = List.of("efectivo", "transferencia");
    private static final Logger logger = LogManager.getLogger(SueldosService.class);
    private final ServicioRealizadoRepository servicioRealizadoRepository;
    private final UsersRepository usersRepository = new UsersRepositoryImpl();
    private final EgresosRepository egresosRepository;

    public SueldosService(SalariesRepository repo, ServicioRealizadoRepository servicioRealizadoRepo, EgresosRepository egresosRepository) {
        this.salariesRepository = repo;
        this.servicioRealizadoRepository = servicioRealizadoRepo;
        this.egresosRepository = egresosRepository;
    }

    /**
     * Paga un salary a un barbero y registra el egreso correspondiente.
     *
     * @param salary    Salary a pagar (debe tener barberoId, fechas y monto ya calculados)
     * @param paymentMethodId Forma de pago ("efectivo" o "transferencia")
     * @throws IllegalArgumentException si los datos son inválidos
     * @throws IllegalStateException    si el salary ya fue pagado
     */
    public void pagarSueldo(Salary salary, int paymentMethodId, double bono) {
        if (salary == null) throw new IllegalArgumentException("Salary vacío");
        // Verificar si ya se pagó este salary
        if (isPagado(salary.getUserId(), salary.getWeekStartDate())) {
            throw new IllegalStateException("Este barbero ya tiene un salary registrado esta semana.");
        }

        salary.setAmountPaid(salary.getAmountPaid() + bono);
        salary.setPayDate(LocalDate.now());
        salary.setPaymentMethodId(paymentMethodId);
        salariesRepository.save(salary);

        String descripcion = "Pago de salary a barbero ID " + salary.getUserId() +
                " (semana del " + salary.getWeekStartDate() + " al " + salary.getWeekEndDate() + ")";

        Egreso egreso = new Egreso(
                descripcion,
                salary.getAmountPaid(),
                LocalDate.now(),
                "salary",
                "efectivo"//paymentMethodId //FIXME: set Egreso payment method to int
        );
        egresosRepository.save(egreso);

        logger.info("[SUELDOS] Salary pagado y egreso registrado para barbero ID {}", salary.getUserId());
    }

    /**
     * Calcula el sueldo de un user para la semana actual.
     *
     * @param user User al que se le calcula el sueldo
     * @param bono    Bono adicional a sumar al sueldo (puede ser 0)
     * @return Salary calculado (sin fecha de pago ni forma de pago)
     * @throws IllegalArgumentException si el user es inválido
     */
    public Salary calcularSueldo(User user, double bono) {
        if (user == null || user.getId() <= 0) {
            throw new IllegalArgumentException("User inválido.");
        }

        int tipo = user.getPaymentType();
        double param1 = user.getParam1();
        double param2 = user.getParam2();

        // Rango semanal
        LocalDate[] semana = obtenerRangoSemanaActual();
        LocalDate inicioSemana = semana[0];
        LocalDate finSemana = semana[1];

        // Producción y adelantos
        double produccion = servicioRealizadoRepository.getProduccionSemanalPorBarbero(user.getId(), inicioSemana, finSemana);
        double adelantos = egresosRepository.getTotalAdelantos(user.getId(), inicioSemana, finSemana);

        logger.info("[SUELDOS] Calculando sueldo para user ID {} (tipoCobro={}, param1={}, param2={}, bono={})",
                user.getId(), tipo, param1, param2, bono);
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
            logger.warn("[SUELDOS] El sueldo calculado para user ID {} es negativo ({}), se ajusta a 0.",
                    user.getId(), montoFinal);

            // Ajustamos el sueldo a 0 para que no se registre sueldo negativo
            montoFinal = 0;

            // Calculamos la fecha del próximo lunes (finSemana es sábado)
            LocalDate lunesProximo = finSemana.plusDays(2); // finSemana es sábado

            // Creamos un egreso tipo "adelanto" para registrar la deuda pendiente como adelanto automático
            String descripcion = "Saldo pendiente arrastrado (user ID " + user.getId() + ")";
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

        return new Salary(
                user.getId(),
                inicioSemana,
                finSemana,
                produccion,
                montoFinal,
                tipo,
                null, // Fecha de pago se asignará en el paso final
                0  // Forma de pago también se asigna al pagar
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
     * Si la producción por porcentaje es menor al umbral mínimo, se paga el umbral.
     * Si es mayor, se paga la producción por porcentaje.
     * Param1 = umbral mínimo
     * Param2 = porcentaje (0.0 a 1.0)
     */
    private double calcularSueldoEspecial(double produccion, double umbralMinimo, double porcentaje) {
        logger.info("[SUELDOS] Calculando sueldo especial: produccion = {}, umbralMinimo = {}, porcentaje = {}",
                produccion, umbralMinimo, porcentaje);
        logger.info("[SUELDOS] Calculando sueldo especial...");
        logger.info("   • Producción total: {}", produccion);
        logger.info("   • Porcentaje aplicado: {}%", porcentaje * 100);
        logger.info("   • Umbral mínimo garantizado: {}", umbralMinimo);

        double calculado = produccion * porcentaje;
        double sueldoEspecial = Math.max(umbralMinimo, calculado);

        if (calculado > umbralMinimo) {
            logger.info("El 50% de la producción ({}) supera el umbral ({}). Se paga: {}", calculado, umbralMinimo, sueldoEspecial);
        } else {
            logger.info("El 50% de la producción ({}) NO supera el umbral ({}). Se garantiza el mínimo: {}", calculado, umbralMinimo, sueldoEspecial);
        }

        logger.info("=> Resultado final para el barbero: {}", sueldoEspecial);

        if ((produccion * porcentaje) < umbralMinimo) {
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
        return salariesRepository.findByBarberoAndFecha(barberoId, semanaInicio) != null;
    }

    /**
     * Genera una lista de SalaryDTO para todos los barberos en el rango semanal dado.
     * Incluye producción, monto a liquidar y estado de pago.
     * Se utiliza para mostrar en la vista de salaries.
     *
     * @param inicio Fecha de inicio de la semana (lunes)
     * @param fin    Fecha de fin de la semana (sábado)
     * @return Lista de SalaryDTO con información para la vista
     */
    public List<SalaryDTO> genSueldoDTOSemanal(LocalDate inicio, LocalDate fin){
        List<SalaryDTO> lista = new ArrayList<>();
        List<User> users = usersRepository.findAll();

        for (User user : users) {
            int barberoId = user.getId();
            String nombre = user.getName();

            // Producción semanal
            double produccion = servicioRealizadoRepository.getProduccionSemanalPorBarbero(barberoId, inicio, fin);

            // Calcular sueldo base (sin bonos)
            Salary salaryTemp = calcularSueldo(user, 0);

            // Consultar adelantos
            double adelantos = egresosRepository.getTotalAdelantos(barberoId, inicio, fin);

            // Aplicar descuento de adelantos
            double montoFinal = salaryTemp.getAmountPaid() - adelantos;

            // Verificar si ya está pagado
            boolean yaPagado = isPagado(barberoId, inicio);
            int sueldoId = yaPagado ? salariesRepository.findByBarberoAndFecha(barberoId, inicio).getId() : 0;

            SalaryDTO dto = new SalaryDTO();
            dto.setUserId(barberoId);
            dto.setUsername(nombre);
            dto.setTotalProduction(produccion);
            dto.setAmountPaid(montoFinal);
            dto.setPaymentStatus(yaPagado);
            dto.setSalaryId(sueldoId);

            lista.add(dto);
        }

        return lista;
    }
}
