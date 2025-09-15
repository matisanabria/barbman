package app.barbman.core.controller;

import app.barbman.core.model.CajaDiaria;
import app.barbman.core.repositories.caja.CajaRepository;
import app.barbman.core.repositories.caja.CajaRepositoryImpl;
import app.barbman.core.repositories.egresos.EgresosRepository;
import app.barbman.core.repositories.egresos.EgresosRepositoryImpl;
import app.barbman.core.repositories.serviciorealizado.ServicioRealizadoRepository;
import app.barbman.core.repositories.serviciorealizado.ServicioRealizadoRepositoryImpl;
import app.barbman.core.service.caja.CajaService;
import app.barbman.core.util.WindowManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CajaController implements Initializable {
    @FXML private ChoiceBox<String> choiceFechas;
    @FXML private Button btnCierreCaja;

    // Labels del ticket
    @FXML private Label lblFecha;
    @FXML private Label lblIngresos;
    @FXML private Label lblEgresos;
    @FXML private Label lblSaldoFinal;
    @FXML private Label lblEfectivo;
    @FXML private Label lblTransferencia;
    @FXML private Label lblPOS;

    private static final Logger logger = LogManager.getLogger(CajaController.class);

    private final CajaRepository cajaRepo = new CajaRepositoryImpl();
    private final ServicioRealizadoRepository serviciosRepo = new ServicioRealizadoRepositoryImpl();
    private final EgresosRepository egresosRepo = new EgresosRepositoryImpl();
    private final CajaService cajaService = new CajaService(cajaRepo, serviciosRepo, egresosRepo);

    private CajaDiaria cierre; // el objeto calculado que se mostrará y luego guardará

    /**
     * Inicializa la vista Caja.
     * Carga las fechas disponibles y muestra el resumen correspondiente.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarFechas();

        choiceFechas.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                mostrarResumen(LocalDate.parse(newVal));
            }
        });

        // Seleccionar automáticamente el último cierre registrado
        if (!choiceFechas.getItems().isEmpty()) {
            String ultimaFecha = choiceFechas.getItems()
                    .stream()
                    .sorted() // ordena por fecha ascendente (yyyy-MM-dd funciona con orden lexicográfico)
                    .reduce((first, second) -> second) // obtiene el último
                    .orElse(null);

            if (ultimaFecha != null) {
                choiceFechas.setValue(ultimaFecha);
                mostrarResumen(LocalDate.parse(ultimaFecha));
                logger.info("Seleccionada automáticamente la última fecha de cierre: {}", ultimaFecha);
            }
        } else {
            mostrarNoHayRegistros();
        }

        // Deshabilitar el botón si ya existe un cierre de hoy
        LocalDate hoy = LocalDate.now();
        if (cajaRepo.findByFecha(hoy) != null) {
            btnCierreCaja.setDisable(true);
            logger.info("Botón de cierre deshabilitado porque ya existe cierre para {}", hoy);
        } else {
            btnCierreCaja.setDisable(false);
        }

        btnCierreCaja.setOnAction(e -> onCierreCaja());
        logger.info("Vista de Caja inicializada.");
    }

    /**
     * Carga todas las fechas de cierres de caja en el ChoiceBox.
     */
    private void cargarFechas() {
        List<CajaDiaria> cierres = cajaRepo.findAll();
        List<String> fechas = cierres.stream()
                .map(c -> c.getFecha().toString())
                .collect(Collectors.toList());
        choiceFechas.setItems(FXCollections.observableArrayList(fechas));
    }

    /**
     * Muestra en los labels los datos de un cierre de caja.
     * @param fecha Fecha a mostrar
     */
    private void mostrarResumen(LocalDate fecha) {
        CajaDiaria caja = cajaRepo.findByFecha(fecha);

        if (caja == null) {
            mostrarNoHayRegistros();
            return;
        }

        lblFecha.setText("Fecha: " + caja.getFecha());
        lblIngresos.setText("Ingresos totales: " + (int) caja.getIngresosTotal() + " Gs");
        lblEgresos.setText("Egresos totales: " + (int) caja.getEgresosTotal() + " Gs");
        lblSaldoFinal.setText("Saldo final: " + (int) caja.getSaldoFinal() + " Gs");

        lblEfectivo.setText("- Efectivo: " + (int) caja.getEfectivo() + " Gs");
        lblTransferencia.setText("- Transferencia: " + (int) caja.getTransferencia() + " Gs");
        lblPOS.setText("- POS: " + (int) caja.getPos() + " Gs");

        logger.info("Mostrando resumen de caja para fecha {}", fecha);
    }

    /**
     * Muestra un aviso cuando no hay registros.
     */
    private void mostrarNoHayRegistros() {
        lblFecha.setText("⚠ No hay registros");
        lblIngresos.setText("Ingresos totales: --");
        lblEgresos.setText("Egresos totales: --");
        lblSaldoFinal.setText("Saldo final: --");
        lblEfectivo.setText("- Efectivo: --");
        lblTransferencia.setText("- Transferencia: --");
        lblPOS.setText("- POS: --");
        logger.warn("No hay registros de caja para la fecha seleccionada.");
    }

    /**
     * Maneja el evento del botón "Cierre de caja".
     * Calcula el cierre de hoy y lo muestra en los labels.
     * (Más adelante se conecta con la vista CierreCajaView para confirmación).
     */
    @FXML
    private void onCierreCaja() {
        LocalDate hoy = LocalDate.now();
        try {
            CajaDiaria cierre = cajaService.calcularCierre(hoy);

            WindowManager.openModal("/app/barbman/core/view/cierre-caja-preview.fxml", controller -> {
                if (controller instanceof CierreCajaController cierreCtrl) {
                    cierreCtrl.setData(cierre, cajaService, this::refrescarVista);
                }
            });

        } catch (Exception e) {
            mostrarNoHayRegistros();
            logger.error("Error al calcular cierre de caja: {}", e.getMessage(), e);
        }
    }

    /**
     * Refresca la vista principal de Caja luego de guardar un cierre.
     */
    private void refrescarVista() {
        cargarFechas();
        // seleccionar automáticamente el último registro
        if (!choiceFechas.getItems().isEmpty()) {
            String ultimaFecha = choiceFechas.getItems()
                    .stream()
                    .sorted()
                    .reduce((first, second) -> second)
                    .orElse(null);

            if (ultimaFecha != null) {
                choiceFechas.setValue(ultimaFecha);
                mostrarResumen(LocalDate.parse(ultimaFecha));
            }
        }
    }

}
