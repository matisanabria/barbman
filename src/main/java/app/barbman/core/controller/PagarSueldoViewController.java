package app.barbman.core.controller;

import app.barbman.core.dto.SueldoDTO;
import app.barbman.core.model.Barbero;
import app.barbman.core.model.Sueldo;
import app.barbman.core.repositories.barbero.BarberoRepository;
import app.barbman.core.repositories.barbero.BarberoRepositoryImpl;
import app.barbman.core.repositories.egresos.EgresosRepository;
import app.barbman.core.repositories.egresos.EgresosRepositoryImpl;
import app.barbman.core.repositories.serviciorealizado.ServicioRealizadoRepository;
import app.barbman.core.repositories.serviciorealizado.ServicioRealizadoRepositoryImpl;
import app.barbman.core.repositories.sueldos.SueldosRepository;
import app.barbman.core.repositories.sueldos.SueldosRepositoryImpl;
import app.barbman.core.service.sueldos.SueldosService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;

/**
 * Controlador para la ventana de pago de sueldos.
 * Recibe un SueldoDTO desde la tabla de sueldos y permite registrar el pago.
 */
public class PagarSueldoViewController implements Initializable {

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


    private static final Logger logger = LogManager.getLogger(PagarSueldoViewController.class);
    private final DecimalFormat formateador = new DecimalFormat("#,###");

    private final SueldosRepository sueldosRepository = new SueldosRepositoryImpl();
    private final ServicioRealizadoRepository servicioRealizadoRepository = new ServicioRealizadoRepositoryImpl();
    private final EgresosRepository egresosRepository = new EgresosRepositoryImpl();
    private final BarberoRepository barberoRepository = new BarberoRepositoryImpl();
    private final SueldosService sueldosService = new SueldosService(sueldosRepository, servicioRealizadoRepository, egresosRepository);

    // Barbero y sueldo seleccionado desde la tabla principal
    private SueldoDTO sueldoDTO;
    private Barbero barbero;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        formaPagoChoiceBox.setItems(FXCollections.observableArrayList("efectivo", "transferencia"));
        formaPagoChoiceBox.setValue("efectivo");

        // monto manual oculto por defecto
        montoManualField.setVisible(false);

        btnGuardar.setOnAction(e -> pagarSueldo());
        logger.info("[SUELDOS-PAGO] Vista inicializada.");
    }

    /**
     * Metodo para pasar datos desde la tabla principal.
     */
    public void setSueldoDTO(SueldoDTO dto) {
        this.sueldoDTO = dto;
        this.barbero = barberoRepository.findById(dto.getBarberoId());

        lblProduccion.setText(formateador.format(dto.getProduccionTotal()) + " Gs");
        lblAdelantos.setText(formateador.format(dto.getProduccionTotal() - dto.getMontoLiquidado()) + " Gs");
        lblSueldoFinal.setText(formateador.format(dto.getMontoLiquidado()) + " Gs");

        if (barbero != null && barbero.getTipoCobro() == 0) {
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
            if (barbero == null) {
                throw new IllegalArgumentException("No se encontró el barbero.");
            }

            double bono = 0;
            if (!bonoField.getText().isBlank()) {
                bono = Double.parseDouble(bonoField.getText().replace(".", ""));
            }

            double montoManual = -1;
            if (barbero.getTipoCobro() == 0) {
                if (montoManualField.getText().isBlank()) {
                    throw new IllegalArgumentException("Debe ingresar un monto manual para este barbero.");
                }
                montoManual = Double.parseDouble(montoManualField.getText().replace(".", ""));
            }

            Sueldo sueldo = sueldosService.calcularSueldo(barbero, bono);
            if (montoManual >= 0) {
                sueldo.setMontoLiquidado(montoManual);
            }

            String formaPago = formaPagoChoiceBox.getValue();
            sueldosService.pagarSueldo(sueldo, formaPago);

            logger.info("[SUELDOS-PAGO] Sueldo registrado correctamente -> Barbero: {}, Monto: {}, FormaPago: {}",
                    barbero.getNombre(), sueldo.getMontoLiquidado(), formaPago);

        } catch (Exception e) {
            logger.error("[SUELDOS-PAGO] Error al pagar sueldo: {}", e.getMessage(), e);
            mostrarAlerta("Error al pagar sueldo: " + e.getMessage());
        }

        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
