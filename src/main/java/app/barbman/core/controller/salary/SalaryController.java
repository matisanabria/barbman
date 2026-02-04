package app.barbman.core.controller.salary;

import app.barbman.core.dto.SalaryDTO;
import app.barbman.core.model.human.User;
import app.barbman.core.repositories.expense.ExpenseRepositoryImpl;
import app.barbman.core.repositories.salaries.advance.AdvanceRepositoryImpl;
import app.barbman.core.repositories.salaries.salaries.SalariesRepositoryImpl;
import app.barbman.core.repositories.sales.services.serviceheader.ServiceHeaderRepositoryImpl;
import app.barbman.core.repositories.users.UsersRepositoryImpl;
import app.barbman.core.service.expenses.ExpensesService;
import app.barbman.core.service.salaries.SalariesService;
import app.barbman.core.service.salaries.advances.AdvancesService;
import app.barbman.core.service.salaries.period.SalaryPeriodResolver;
import app.barbman.core.service.sales.services.ServiceHeaderService;
import app.barbman.core.service.users.UsersService;
import app.barbman.core.util.AlertUtil;
import app.barbman.core.util.NumberFormatterUtil;
import app.barbman.core.util.window.WindowManager;
import app.barbman.core.util.window.WindowRequest;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Controller for the Salaries view.
 * Displays a table with employee salaries for the current week/period.
 * Allows viewing details such as total production, amount to be paid, and payment status.
 */
public class SalaryController implements Initializable {

    private static final Logger logger = LogManager.getLogger(SalaryController.class);
    private static final String PREFIX = "[SALARY-VIEW]";

    // ============================================================
    // FXML COMPONENTS
    // ============================================================

    @FXML private TableView<SalaryDTO> sueldosTable;
    @FXML private TableColumn<SalaryDTO, String> colBarbero;
    @FXML private TableColumn<SalaryDTO, String> colProduccion;
    @FXML private TableColumn<SalaryDTO, String> colAdelantos;
    @FXML private TableColumn<SalaryDTO, String> colMonto;
    @FXML private TableColumn<SalaryDTO, String> colEstado;
    @FXML private TableColumn<SalaryDTO, Void> colAccion;

    @FXML private Button btnRegistrarAdelanto;
    @FXML private TextField searchField;

    // Stats labels
    @FXML private Label lblPeriodoActual;
    @FXML private Label lblTotalEmpleados;
    @FXML private Label lblPendientes;
    @FXML private Label lblPagados;

    // ============================================================
    // SERVICES & REPOSITORIES
    // ============================================================

    private final SalariesService salariesService;
    private final AdvancesService advancesService;
    private final UsersService usersService;

    private ObservableList<SalaryDTO> allSalaries;
    private LocalDate currentPeriodReference;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public SalaryController() {
        // Initialize repositories
        var salariesRepo = new SalariesRepositoryImpl();
        var expenseRepo = new ExpenseRepositoryImpl();
        var advanceRepo = new AdvanceRepositoryImpl();
        var serviceHeaderRepo = new ServiceHeaderRepositoryImpl();
        var userRepo = new UsersRepositoryImpl();

        // Initialize services
        var expensesService = new ExpensesService(expenseRepo);
        this.advancesService = new AdvancesService();
        var serviceHeaderService = new ServiceHeaderService(serviceHeaderRepo);
        var periodResolver = new SalaryPeriodResolver();
        this.usersService = new UsersService(userRepo);

        this.salariesService = new SalariesService(
                salariesRepo,
                expensesService,
                advancesService,
                serviceHeaderService,
                periodResolver
        );

        this.currentPeriodReference = LocalDate.now();
    }

    // ============================================================
    // INITIALIZATION
    // ============================================================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("{} Initializing Salary view...", PREFIX);

        setupTable();
        setupSearchFilter();
        setupButtons();
        loadData();
        updateStats();

        logger.info("{} Salary view initialized successfully", PREFIX);
    }

    // ============================================================
    // TABLE SETUP
    // ============================================================

    private void setupTable() {
        // Prevent column reordering
        sueldosTable.getColumns().forEach(col -> col.setReorderable(false));
        sueldosTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Configure columns
        colBarbero.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getUsername())
        );

        colProduccion.setCellValueFactory(data ->
                new SimpleStringProperty(
                        NumberFormatterUtil.format(data.getValue().getProduction()) + " Gs"
                )
        );

        colAdelantos.setCellValueFactory(data ->
                new SimpleStringProperty(
                        NumberFormatterUtil.format(data.getValue().getAdvances()) + " Gs"
                )
        );

        colMonto.setCellValueFactory(data ->
                new SimpleStringProperty(
                        NumberFormatterUtil.format(data.getValue().getFinalAmount()) + " Gs"
                )
        );

        colEstado.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().isPaid() ? "Pagado" : "Pendiente")
        );

        // Style cells
        applyCellStyles();

        // Setup action buttons column
        setupActionColumn();
    }

    private void applyCellStyles() {
        colBarbero.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    getStyleClass().add("sueldos-cell-empleado");
                }
            }
        });

        colProduccion.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    getStyleClass().add("sueldos-cell-produccion");
                }
            }
        });

        colAdelantos.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    getStyleClass().add("sueldos-cell-adelantos");
                }
            }
        });

        colMonto.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    getStyleClass().add("sueldos-cell-monto");
                }
            }
        });

        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("sueldos-badge");

                    if (item.equals("Pagado")) {
                        badge.getStyleClass().add("sueldos-badge-pagado");
                    } else {
                        badge.getStyleClass().add("sueldos-badge-pendiente");
                    }

                    setGraphic(badge);
                    setText(null);
                }
            }
        });
    }

    private void setupActionColumn() {
        colAccion.setCellFactory(col -> new TableCell<>() {
            private final Button btnPagar = new Button("💵 Pagar");
            private final Button btnVer = new Button("👁 Ver");
            private final HBox container = new HBox(8);

            {
                btnPagar.getStyleClass().add("sueldos-btn-pagar");
                btnVer.getStyleClass().add("sueldos-btn-ver");
                container.setAlignment(Pos.CENTER);
                container.getChildren().addAll(btnPagar, btnVer);

                btnPagar.setOnAction(event -> {
                    SalaryDTO dto = getTableView().getItems().get(getIndex());
                    handlePaySalary(dto);
                });

                btnVer.setOnAction(event -> {
                    SalaryDTO dto = getTableView().getItems().get(getIndex());
                    handleViewDetails(dto);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    SalaryDTO dto = getTableView().getItems().get(getIndex());

                    // Disable pay button if already paid
                    btnPagar.setDisable(dto.isPaid());

                    setGraphic(container);
                }
            }
        });
    }

    // ============================================================
    // SEARCH FILTER
    // ============================================================

    private void setupSearchFilter() {
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                filterTable(newVal);
            });
        }
    }

    private void filterTable(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            sueldosTable.setItems(allSalaries);
        } else {
            ObservableList<SalaryDTO> filtered = allSalaries.filtered(dto ->
                    dto.getUsername().toLowerCase().contains(searchText.toLowerCase())
            );
            sueldosTable.setItems(filtered);
        }
        updateStats();
    }

    // ============================================================
    // BUTTON HANDLERS
    // ============================================================

    private void setupButtons() {
        if (btnRegistrarAdelanto != null) {
            btnRegistrarAdelanto.setOnAction(e -> handleRegistrarAdelanto());
        }
    }

    private void handleRegistrarAdelanto() {
        logger.info("{} Opening advance registration window...", PREFIX);

        try {
            AdvancesController controller = (AdvancesController) WindowManager.openModal(
                    WindowRequest.builder()
                            .fxml("/app/barbman/core/view/advances-view.fxml")
                            .title("Registrar Adelanto")
                            .css("/app/barbman/core/style/advances-view.css")
                            .owner((Stage) sueldosTable.getScene().getWindow())
                            .modal(true)
                            .resizable(false)
                            .returnController(true)
                            .build()
            );

            if (controller != null) {
                controller.setParentController(this);
            }

        } catch (Exception e) {
            logger.error("{} Error opening advances dialog: {}", PREFIX, e.getMessage(), e);
            AlertUtil.showError("Error", "No se pudo abrir el diálogo de adelantos.");
        }
    }

    private void handlePaySalary(SalaryDTO dto) {
        logger.info("{} Opening payment confirmation dialog for user: {}", PREFIX, dto.getUsername());

        try {
            // Open modal using WindowManager
            ConfirmSalaryController controller = (ConfirmSalaryController) WindowManager.openModal(
                    WindowRequest.builder()
                            .fxml("/app/barbman/core/view/confirm-salary-view.fxml")
                            .title("Confirmar Pago")
                            .css("/app/barbman/core/style/confirm-salary.css")
                            .owner((Stage) sueldosTable.getScene().getWindow())
                            .modal(true)
                            .resizable(false)
                            .returnController(true)
                            .build()
            );

            // Set data in the controller
            if (controller != null) {
                controller.setSalaryDTO(dto);
                controller.setParentController(this);
            }

            logger.info("{} Payment dialog closed", PREFIX);

        } catch (Exception e) {
            logger.error("{} Error opening payment dialog: {}", PREFIX, e.getMessage(), e);
            AlertUtil.showError(
                    "Error",
                    "No se pudo abrir el diálogo de pago: " + e.getMessage()
            );
        }
    }

    private void handleViewDetails(SalaryDTO dto) {
        logger.info("{} Viewing details for user: {}", PREFIX, dto.getUsername());

        // TODO: Open detailed view window

        String details = String.format(
                """
                DETALLES DEL SUELDO
                
                Empleado: %s
                Período: %s - %s
                
                Producción: %s Gs
                Adelantos: %s Gs
                Monto calculado: %s Gs
                Monto final: %s Gs
                
                Estado: %s
                """,
                dto.getUsername(),
                dto.getPeriodStart().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                dto.getPeriodEnd().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                NumberFormatterUtil.format(dto.getProduction()),
                NumberFormatterUtil.format(dto.getAdvances()),
                NumberFormatterUtil.format(dto.getCalculatedAmount()),
                NumberFormatterUtil.format(dto.getFinalAmount()),
                dto.isPaid() ? "Pagado" : "Pendiente"
        );

        AlertUtil.showInfo("Detalles del Sueldo", details);
    }

    // ============================================================
    // DATA LOADING
    // ============================================================

    private void loadData() {
        logger.info("{} Loading salary data for current period...", PREFIX);

        try {
            // Get all employees (admins and users, exclude superadmin)
            List<User> employees = usersService.getAllUsers().stream()
                    .filter(u -> Objects.equals(u.getRole(), "user") || Objects.equals(u.getRole(), "admin")) // 1=Admin, 2=User/Barber
                    .toList();

            // Build DTOs for each employee
            List<SalaryDTO> dtos = employees.stream()
                    .map(user -> salariesService.buildSalaryDTO(user, currentPeriodReference))
                    .toList();

            allSalaries = FXCollections.observableArrayList(dtos);
            sueldosTable.setItems(allSalaries);

            // Update period label
            if (!dtos.isEmpty() && lblPeriodoActual != null) {
                SalaryDTO first = dtos.get(0);
                lblPeriodoActual.setText(String.format(
                        "%s - %s",
                        first.getPeriodStart().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        first.getPeriodEnd().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                ));
            }

            logger.info("{} Loaded {} salary records", PREFIX, dtos.size());

        } catch (Exception e) {
            logger.error("{} Error loading salary data: {}", PREFIX, e.getMessage(), e);
            AlertUtil.showError(
                    "Error al Cargar Datos",
                    "No se pudieron cargar los datos de sueldos: " + e.getMessage()
            );
        }
    }

    // ============================================================
    // STATISTICS UPDATE
    // ============================================================

    private void updateStats() {
        if (allSalaries == null || allSalaries.isEmpty()) {
            setStatsToZero();
            return;
        }

        int total = allSalaries.size();
        long pagados = allSalaries.stream().filter(SalaryDTO::isPaid).count();
        long pendientes = total - pagados;

        if (lblTotalEmpleados != null) {
            lblTotalEmpleados.setText(String.valueOf(total));
        }

        if (lblPagados != null) {
            lblPagados.setText(String.valueOf(pagados));
        }

        if (lblPendientes != null) {
            lblPendientes.setText(String.valueOf(pendientes));
        }

        logger.debug("{} Stats updated: total={}, paid={}, pending={}",
                PREFIX, total, pagados, pendientes);
    }

    private void setStatsToZero() {
        if (lblTotalEmpleados != null) lblTotalEmpleados.setText("0");
        if (lblPagados != null) lblPagados.setText("0");
        if (lblPendientes != null) lblPendientes.setText("0");
    }

    // ============================================================
    // PUBLIC METHODS
    // ============================================================

    /**
     * Reloads the salary table data.
     * Can be called from external controllers after operations.
     */
    public void reloadData() {
        logger.info("{} Reloading salary data...", PREFIX);
        loadData();
        updateStats();
    }

    // ============================================================
    // ALERT HELPERS
    // ============================================================

    private boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        var result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
