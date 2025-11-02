package app.barbman.core.controller.salary;

import app.barbman.core.dto.SalaryDTO;
import app.barbman.core.repositories.expense.ExpenseRepository;
import app.barbman.core.repositories.expense.ExpenseRepositoryImpl;
import app.barbman.core.repositories.services.service.ServiceRepository;
import app.barbman.core.repositories.services.service.ServiceRepositoryImpl;
import app.barbman.core.repositories.salaries.SalariesRepository;
import app.barbman.core.repositories.salaries.SalariesRepositoryImpl;
import app.barbman.core.service.sueldos.SueldosService;
import app.barbman.core.util.NumberFormatterUtil;
import app.barbman.core.util.WindowManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador para la vista de salaries.
 * Muestra una tabla con los salaries de los barberos para la semana actual.
 * Permite ver detalles como producción total, monto liquidado y estado de pago.
 */
public class SueldosController implements Initializable {
    @FXML
    private TableView<SalaryDTO> sueldosTable; // Tabla que muestra barberos
    @FXML
    private TableColumn<SalaryDTO, String> colBarbero;
    @FXML
    private TableColumn<SalaryDTO, String> colProduccion;
    @FXML
    private TableColumn<SalaryDTO, String> colMonto;
    @FXML
    private TableColumn<SalaryDTO, String> colEstado;
    @FXML
    private TableColumn<SalaryDTO, String> colAccion;
    @FXML
    private Button btnRegistrarAdelanto;     // Botón para abrir ventana de registrar adelanto

    private static final Logger logger = LogManager.getLogger(SueldosController.class);

    // Repositorios
    private final SalariesRepository sueldoRepo = new SalariesRepositoryImpl();
    private final ServiceRepository serviceRepository = new ServiceRepositoryImpl();
    private final ExpenseRepository expenseRepository = new ExpenseRepositoryImpl();

    // Servicio de lógica de salaries
    private final SueldosService sueldosService = new SueldosService(sueldoRepo, serviceRepository, expenseRepository);

    /**
     * Metodo principal de inicialización de la vista.
     * Se ejecuta automáticamente al cargarse el FXML.
     * <p>
     * - Define el rango de fecha (lunes a sábado)
     * - Genera dinámicamente los salaries
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
        logger.info("[SUELDO-VIEW] Generando salaries semanales desde {} hasta {}", lunes, sabado);

        // Obtener salaries de esta semana
        List<SalaryDTO> lista = sueldosService.genSueldoDTOSemanal(lunes, sabado);
        logger.info("[SUELDO-VIEW] Se generaron {} registros temporales para mostrar en la tabla", lista.size());

        // Configurar columnas
        colBarbero.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));
        colProduccion.setCellValueFactory(cellData -> {
            double prod = cellData.getValue().getTotalProduction();
            return new SimpleStringProperty(NumberFormatterUtil.format(prod) +  " Gs");
        });

        colMonto.setCellValueFactory(cellData -> {
            double monto = cellData.getValue().getAmountPaid();
            return new SimpleStringProperty(NumberFormatterUtil.format(monto) +  " Gs");
        });
        colEstado.setCellValueFactory(cellData -> {
            String estado = cellData.getValue().isPaymentStatus() ? "Pagado" : "Pendiente";
            return new SimpleStringProperty(estado);
        });

        // Columna con botón para pagar sueldo
        colAccion.setCellFactory(col -> new TableCell<>() {
            private final Button btnPagar = new Button("Pagar");

            {
                btnPagar.setOnAction(event -> {
                    SalaryDTO dto = getTableView().getItems().get(getIndex());
                    Stage currentStage = (Stage) sueldosTable.getScene().getWindow();

                    // abrir ventana y obtener controller
                    ConfirmSalaryController controller =
                            WindowManager.openWindowWithController(
                                    "/app/barbman/core/view/confirm-salary-view.fxml",
                                    "Pagar Salary",
                                    currentStage
                            );

                    // pasar el DTO al controller
                    controller.setSueldoDTO(dto);
                    controller.setParentController(SueldosController.this);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    SalaryDTO dto = getTableView().getItems().get(getIndex());
                    if (dto.isPaymentStatus()) {
                        setGraphic(new Label("✔ Pagado"));
                    } else {
                        setGraphic(btnPagar);
                    }
                }
            }
        });

        // Cargar en la tabla
        sueldosTable.getItems().setAll(lista);
        logger.info("[SUELDO-VIEW] Datos cargados correctamente en la tabla de salaries.");
    }

    /**
     * Vuelve a cargar los datos de la tabla de salaries.
     */
    public void recargarTabla() {
        LocalDate hoy = LocalDate.now();
        LocalDate lunes = hoy.with(java.time.DayOfWeek.MONDAY);
        LocalDate sabado = lunes.plusDays(5);

        List<SalaryDTO> lista = sueldosService.genSueldoDTOSemanal(lunes, sabado);
        sueldosTable.getItems().setAll(lista);

        logger.info("[SUELDO-VIEW] Tabla de salaries recargada ({}) registros.", lista.size());
    }

}
