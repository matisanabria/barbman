package app.barbman.core.controller;

import app.barbman.core.dto.SueldoDTO;
import app.barbman.core.repositories.egresos.EgresosRepository;
import app.barbman.core.repositories.egresos.EgresosRepositoryImpl;
import app.barbman.core.repositories.serviciorealizado.ServicioRealizadoRepository;
import app.barbman.core.repositories.serviciorealizado.ServicioRealizadoRepositoryImpl;
import app.barbman.core.repositories.sueldos.SueldosRepository;
import app.barbman.core.repositories.sueldos.SueldosRepositoryImpl;
import app.barbman.core.service.sueldos.SueldosService;
import app.barbman.core.util.WindowManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador para la vista de sueldos.
 * Muestra una tabla con los sueldos de los barberos para la semana actual.
 * Permite ver detalles como producción total, monto liquidado y estado de pago.
 */
public class SueldosViewController implements Initializable {
    @FXML
    private TableView<SueldoDTO> sueldosTable; // Tabla que muestra barberos
    @FXML
    private TableColumn<SueldoDTO, String> colBarbero;
    @FXML
    private TableColumn<SueldoDTO, String> colProduccion;
    @FXML
    private TableColumn<SueldoDTO, String> colMonto;
    @FXML
    private TableColumn<SueldoDTO, String> colEstado;
    @FXML
    private TableColumn<SueldoDTO, String> colAccion;
    @FXML
    private Button btnRegistrarAdelanto;     // Botón para abrir ventana de registrar adelanto

    private static final Logger logger = LogManager.getLogger(SueldosViewController.class);
    // Formateador para mostrar números sin decimales
    private final DecimalFormat formateadorNumeros = new DecimalFormat("#,###");

    // Repositorios
    private final SueldosRepository sueldoRepo = new SueldosRepositoryImpl();
    private final ServicioRealizadoRepository servicioRealizadoRepository = new ServicioRealizadoRepositoryImpl();
    private final EgresosRepository egresosRepository = new EgresosRepositoryImpl();

    // Servicio de lógica de sueldos
    private final SueldosService sueldosService = new SueldosService(sueldoRepo, servicioRealizadoRepository, egresosRepository);

    /**
     * Metodo principal de inicialización de la vista.
     * Se ejecuta automáticamente al cargarse el FXML.
     * <p>
     * - Define el rango de fecha (lunes a sábado)
     * - Genera dinámicamente los sueldos
     * - Configura las columnas de la tabla
     * - Muestra los datos en pantalla
     *
     * @param location  ubicación del archivo FXML (no se usa)
     * @param resources recursos internacionales (no se usa)
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sueldosTable.getColumns().forEach(col -> col.setReorderable(false));
        sueldosTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Acción del botón para abrir la ventana de adelantos
        btnRegistrarAdelanto.setOnAction(e -> {
            logger.info("[SUELDO-VIEW] Abriendo ventana para registrar adelanto...");
            Stage currentStage = (Stage) sueldosTable.getScene().getWindow();
            WindowManager.openWindow(
                    "/app/barbman/core/view/adelantos-view.fxml",
                    "Registrar Adelanto",
                    currentStage
            );
            logger.info("[SUELDO-VIEW] Ventana de adelantos abierta.");
        });

        // Fecha actual
        LocalDate hoy = LocalDate.now();
        LocalDate lunes = hoy.with(java.time.DayOfWeek.MONDAY);
        LocalDate sabado = lunes.plusDays(5);
        logger.info("[SUELDO-VIEW] Generando sueldos semanales desde {} hasta {}", lunes, sabado);

        // Obtener sueldos de esta semana
        List<SueldoDTO> lista = sueldosService.genSueldoDTOSemanal(lunes, sabado);
        logger.info("[SUELDO-VIEW] Se generaron {} registros temporales para mostrar en la tabla", lista.size());

        // Configurar columnas
        colBarbero.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombreBarbero()));
        colProduccion.setCellValueFactory(cellData -> {
            double prod = cellData.getValue().getProduccionTotal();
            return new SimpleStringProperty(formateadorNumeros.format(prod) +  " Gs");
        });

        colMonto.setCellValueFactory(cellData -> {
            double monto = cellData.getValue().getMontoLiquidado();
            return new SimpleStringProperty(formateadorNumeros.format(monto) +  " Gs");
        });
        colEstado.setCellValueFactory(cellData -> {
            String estado = cellData.getValue().isPagado() ? "Pagado" : "Pendiente";
            return new SimpleStringProperty(estado);
        });

        // TODO: poner botones en la columna acción para pagar sueldo

        // Cargar en la tabla
        sueldosTable.getItems().setAll(lista);
        logger.info("[SUELDO-VIEW] Datos cargados correctamente en la tabla de sueldos.");
    }


}
