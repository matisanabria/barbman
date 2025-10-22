package app.barbman.core.controller;

import app.barbman.core.dto.SueldoDTO;
import app.barbman.core.model.Salary;
import app.barbman.core.model.User;
import app.barbman.core.repositories.users.UsersRepository;
import app.barbman.core.repositories.users.UsersRepositoryImpl;
import app.barbman.core.repositories.egresos.EgresosRepository;
import app.barbman.core.repositories.egresos.EgresosRepositoryImpl;
import app.barbman.core.repositories.serviciorealizado.ServicioRealizadoRepository;
import app.barbman.core.repositories.serviciorealizado.ServicioRealizadoRepositoryImpl;
import app.barbman.core.repositories.salaries.SalariesRepository;
import app.barbman.core.repositories.salaries.SalariesRepositoryImpl;
import app.barbman.core.service.sueldos.SueldosService;
import app.barbman.core.util.NumberFormatUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador para la ventana de pago de salaries.
 * Recibe un SueldoDTO desde la tabla de salaries y permite registrar el pago.
 */
public class PagarSueldoController implements Initializable {

    @FXML
    private ChoiceBox<String> formaPagoChoiceBox;
    @FXML
    private TextField bonoField;
    @FXML
    private TextField montoManualField;
    @FXML
    private Label lblProduccion;
    @FXML
    private Label lblAdelantos;
    @FXML
    private Label lblSueldoFinal;
    @FXML
    private Button btnGuardar;
    @FXML
    private Button btnCancelar;
    @FXML
    private VBox manualMontoBox; // monto manual


    private static final Logger logger = LogManager.getLogger(PagarSueldoController.class);

    private final SalariesRepository salariesRepository = new SalariesRepositoryImpl();
    private final ServicioRealizadoRepository servicioRealizadoRepository = new ServicioRealizadoRepositoryImpl();
    private final EgresosRepository egresosRepository = new EgresosRepositoryImpl();
    private final UsersRepository usersRepository = new UsersRepositoryImpl();
    private final SueldosService sueldosService = new SueldosService(salariesRepository, servicioRealizadoRepository, egresosRepository);
    private SueldosController parentController;

    // User y sueldo seleccionado desde la tabla principal
    private SueldoDTO sueldoDTO;
    private User user;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        formaPagoChoiceBox.setItems(FXCollections.observableArrayList("efectivo", "transferencia"));
        formaPagoChoiceBox.setValue("efectivo");

        // ocultamos el VBox completo al inicio
        manualMontoBox.setVisible(false);
        manualMontoBox.setManaged(false);

        NumberFormatUtil.applyToTextField(montoManualField);

        // Formateador para bono
        bonoField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.isBlank()) return;

            String digits = newValue.replaceAll("[^\\d]", "");
            if (digits.isEmpty()) {
                bonoField.setText("");
                return;
            }
            try {
                long valor = Long.parseLong(digits);
                String formateado = NumberFormatUtil.format(valor);
                if (!bonoField.getText().equals(formateado)) {
                    bonoField.setText(formateado);
                    bonoField.positionCaret(formateado.length());
                }
            } catch (NumberFormatException e) {
                logger.warn("[SUELDOS-PAGO] Valor no numérico en bonoField: {}", newValue);
            }
        });

        btnGuardar.setOnAction(e -> pagarSueldo());
        logger.info("[SUELDOS-PAGO] Vista inicializada.");
    }

    /**
     * Metodo para pasar datos desde la tabla principal.
     */
    public void setSueldoDTO(SueldoDTO dto) {
        this.sueldoDTO = dto;
        this.user = usersRepository.findById(dto.getBarberoId());

        // rango semanal
        java.time.LocalDate hoy = java.time.LocalDate.now();
        java.time.LocalDate lunes = hoy.with(java.time.DayOfWeek.MONDAY);
        java.time.LocalDate sabado = lunes.plusDays(5);

        double adelantos = egresosRepository.getTotalAdelantos(dto.getBarberoId(), lunes, sabado);

        lblProduccion.setText("Producción: " + NumberFormatUtil.format(dto.getProduccionTotal()) + " Gs");
        lblAdelantos.setText("Adelantos: " + NumberFormatUtil.format(adelantos) + " Gs");
        lblSueldoFinal.setText("Salary final: " + NumberFormatUtil.format(dto.getMontoLiquidado()) + " Gs");

        if (user != null && user.getPaymentType() == 0) {
            manualMontoBox.setVisible(true);
            manualMontoBox.setManaged(true);
            logger.info("[SUELDOS-PAGO] Tipo de cobro = 0, habilitado campo de monto manual.");
        }

    }

    @FXML
    private void onCancelar() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
        logger.info("[SUELDOS-PAGO] Ventana cerrada por el usuario.");
    }

    private void pagarSueldo() {
        try {
            if (user == null) {
                throw new IllegalArgumentException("No se encontró el user.");
            }

            double bono = 0;
            if (!bonoField.getText().isBlank()) {
                bono = Double.parseDouble(bonoField.getText().replace(".", ""));
            }

            double montoManual = -1;
            if (user.getPaymentType() == 0) {
                if (montoManualField.getText().isBlank()) {
                    throw new IllegalArgumentException("Debe ingresar un monto manual para este user.");
                }
                montoManual = Double.parseDouble(montoManualField.getText().replace(".", ""));
            }

            Salary salary = sueldosService.calcularSueldo(user, 0);
            if (montoManual >= 0) {
                salary.setAmountPaid(montoManual);
            }

            String formaPago = formaPagoChoiceBox.getValue();
            // Guardar pago con bono
            sueldosService.pagarSueldo(salary, formaPago, bono);

            logger.info("[SUELDOS-PAGO] Salary registrado correctamente -> User: {}, Monto: {}, FormaPago: {}",
                    user.getName(), salary.getAmountPaid(), formaPago);

        } catch (Exception e) {
            logger.error("[SUELDOS-PAGO] Error al pagar sueldo: {}", e.getMessage(), e);
            mostrarAlerta("Error al pagar sueldo: " + e.getMessage());
        }

        if (parentController != null) {
            parentController.recargarTabla();
        }

        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    public void setParentController(SueldosController parent) {
        this.parentController = parent;
    }

    private void mostrarAlerta(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
