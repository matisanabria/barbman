package app.barbman.core.controller;

import app.barbman.core.dto.ResumenDTO;
import app.barbman.core.model.Barbero;
import app.barbman.core.model.CajaDiaria;
import app.barbman.core.repositories.barbero.BarberoRepository;
import app.barbman.core.repositories.barbero.BarberoRepositoryImpl;
import app.barbman.core.repositories.caja.CajaRepository;
import app.barbman.core.repositories.caja.CajaRepositoryImpl;
import app.barbman.core.repositories.egresos.EgresosRepository;
import app.barbman.core.repositories.egresos.EgresosRepositoryImpl;
import app.barbman.core.repositories.serviciorealizado.ServicioRealizadoRepository;
import app.barbman.core.repositories.serviciorealizado.ServicioRealizadoRepositoryImpl;
import app.barbman.core.service.caja.CajaService;
import app.barbman.core.util.NumberFormatUtil;
import app.barbman.core.util.WindowManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

    // Cierre semanal
    @FXML private ChoiceBox<String> choiceSemanas;
    @FXML private Label lblSemana;
    @FXML private Label lblIngresosSemana;
    @FXML private Label lblEgresosSemana;
    @FXML private VBox boxProduccionBarberos;

    private static final Logger logger = LogManager.getLogger(CajaController.class);

    private final CajaRepository cajaRepo = new CajaRepositoryImpl();
    private final ServicioRealizadoRepository serviciosRepo = new ServicioRealizadoRepositoryImpl();
    private final EgresosRepository egresosRepo = new EgresosRepositoryImpl();
    private final CajaService cajaService = new CajaService(cajaRepo, serviciosRepo, egresosRepo);
    private final BarberoRepository barberoRepo = new BarberoRepositoryImpl();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private CajaDiaria cierre;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // ================== CIERRE DIARIO ==================
        cargarFechas();

        choiceFechas.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                mostrarResumen(LocalDate.parse(newVal));
            }
        });

        if (!choiceFechas.getItems().isEmpty()) {
            String ultimaFecha = choiceFechas.getItems().stream().sorted()
                    .reduce((first, second) -> second).orElse(null);

            if (ultimaFecha != null) {
                choiceFechas.setValue(ultimaFecha);
                mostrarResumen(LocalDate.parse(ultimaFecha));
                logger.info("Seleccionada automáticamente la última fecha de cierre: {}", ultimaFecha);
            }
        } else {
            mostrarNoHayRegistros();
        }

        LocalDate hoy = LocalDate.now();
        btnCierreCaja.setDisable(cajaRepo.findByFecha(hoy) != null);

        btnCierreCaja.setOnAction(e -> onCierreCaja());

        // ================== CIERRE SEMANAL ==================
        cargarSemanas();

        choiceSemanas.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                try {
                    String[] partes = newVal.split(" -> ");
                    LocalDate desde = LocalDate.parse(partes[0].trim(), DATE_FORMATTER);
                    LocalDate hasta = LocalDate.parse(partes[1].trim(), DATE_FORMATTER);

                    mostrarResumenSemanal(desde, hasta);
                } catch (Exception e) {
                    logger.error("Error al parsear rango de semana '{}': {}", newVal, e.getMessage(), e);
                }
            }
        });

        if (!choiceSemanas.getItems().isEmpty()) {
            String ultimaSemana = choiceSemanas.getItems().stream().sorted()
                    .reduce((first, second) -> second).orElse(null);

            if (ultimaSemana != null) {
                choiceSemanas.setValue(ultimaSemana);

                String[] partes = ultimaSemana.split(" -> ");
                LocalDate desde = LocalDate.parse(partes[0].trim(), DATE_FORMATTER);
                LocalDate hasta = LocalDate.parse(partes[1].trim(), DATE_FORMATTER);

                mostrarResumenSemanal(desde, hasta);
                logger.info("Seleccionada automáticamente la última semana de cierre: {}", ultimaSemana);
            }
        }

        logger.info("Vista de Caja inicializada con cierres diarios y semanales.");
    }

    private void cargarFechas() {
        List<CajaDiaria> cierres = cajaRepo.findAll();
        List<String> fechas = cierres.stream()
                .map(c -> c.getFecha().toString())
                .collect(Collectors.toList());
        choiceFechas.setItems(FXCollections.observableArrayList(fechas));
    }

    private void mostrarResumen(LocalDate fecha) {
        CajaDiaria caja = cajaRepo.findByFecha(fecha);

        if (caja == null) {
            mostrarNoHayRegistros();
            return;
        }

        lblFecha.setText("Fecha: " + caja.getFecha().format(DATE_FORMATTER));
        lblIngresos.setText("Ingresos totales: " + NumberFormatUtil.format(caja.getIngresosTotal()) + " Gs");
        lblEgresos.setText("Egresos totales: " + NumberFormatUtil.format(caja.getEgresosTotal()) + " Gs");
        lblSaldoFinal.setText("Saldo final: " + NumberFormatUtil.format(caja.getSaldoFinal()) + " Gs");

        lblEfectivo.setText("- Efectivo: " + NumberFormatUtil.format(caja.getEfectivo()) + " Gs");
        lblTransferencia.setText("- Transferencia: " + NumberFormatUtil.format(caja.getTransferencia()) + " Gs");
        lblPOS.setText("- POS: " + NumberFormatUtil.format(caja.getPos()) + " Gs");

        logger.info("Mostrando resumen de caja para fecha {}", fecha);
    }

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

    private void refrescarVista() {
        cargarFechas();
        if (!choiceFechas.getItems().isEmpty()) {
            String ultimaFecha = choiceFechas.getItems().stream().sorted()
                    .reduce((first, second) -> second).orElse(null);

            if (ultimaFecha != null) {
                choiceFechas.setValue(ultimaFecha);
                mostrarResumen(LocalDate.parse(ultimaFecha));
            }
        } else {
            mostrarNoHayRegistros();
        }

        cargarSemanas();
        if (!choiceSemanas.getItems().isEmpty()) {
            String ultimaSemana = choiceSemanas.getItems().stream().sorted()
                    .reduce((first, second) -> second).orElse(null);

            if (ultimaSemana != null) {
                choiceSemanas.setValue(ultimaSemana);

                String[] partes = ultimaSemana.split(" -> ");
                LocalDate desde = LocalDate.parse(partes[0].trim(), DATE_FORMATTER);
                LocalDate hasta = LocalDate.parse(partes[1].trim(), DATE_FORMATTER);

                mostrarResumenSemanal(desde, hasta);
            }
        }

        logger.info("Vista de Caja refrescada (cierres diarios y semanales).");
    }

    private void mostrarResumenSemanal(LocalDate desde, LocalDate hasta) {
        try {
            ResumenDTO resumen = cajaService.calcularResumenSemanal(desde, hasta);

            lblSemana.setText("Semana: " + desde.format(DATE_FORMATTER) + " -> " + hasta.format(DATE_FORMATTER));
            lblIngresosSemana.setText("Ingresos totales: " + NumberFormatUtil.format(resumen.getIngresosTotal()) + " Gs");
            lblEgresosSemana.setText("Egresos totales: " + NumberFormatUtil.format(resumen.getEgresosTotal()) + " Gs");

            boxProduccionBarberos.getChildren().clear();
            List<Barbero> barberos = barberoRepo.findAll();

            for (Barbero b : barberos) {
                double produccion = serviciosRepo.getProduccionSemanalPorBarbero(b.getId(), desde, hasta);
                Label lbl = new Label("- " + b.getNombre() + ": " + NumberFormatUtil.format(produccion) + " Gs");
                lbl.getStyleClass().add("caja-label");
                boxProduccionBarberos.getChildren().add(lbl);
            }

            if (barberos.isEmpty()) {
                Label lbl = new Label("No hay barberos registrados.");
                lbl.getStyleClass().add("caja-label");
                boxProduccionBarberos.getChildren().add(lbl);
            }

            logger.info("Resumen semanal mostrado para {} - {}", desde, hasta);

        } catch (Exception e) {
            lblSemana.setText(" No hay registros");
            lblIngresosSemana.setText("Ingresos totales: --");
            lblEgresosSemana.setText("Egresos totales: --");
            boxProduccionBarberos.getChildren().clear();

            logger.error("Error al mostrar resumen semanal: {}", e.getMessage(), e);
        }
    }

    private void cargarSemanas() {
        List<CajaDiaria> cierres = cajaRepo.findAll();

        if (cierres.isEmpty()) {
            choiceSemanas.setItems(FXCollections.observableArrayList());
            logger.warn("No hay cierres diarios registrados, no se pueden calcular semanas.");
            return;
        }

        LocalDate minFecha = cierres.stream().map(CajaDiaria::getFecha).min(LocalDate::compareTo).orElse(LocalDate.now());
        LocalDate maxFecha = cierres.stream().map(CajaDiaria::getFecha).max(LocalDate::compareTo).orElse(LocalDate.now());

        LocalDate inicio = minFecha.with(java.time.DayOfWeek.MONDAY);
        List<String> semanas = new ArrayList<>();

        while (!inicio.isAfter(maxFecha)) {
            LocalDate fin = inicio.plusDays(6);

            // Quitamos el número de semana → solo mostramos el rango
            String label = String.format("%s -> %s",
                    inicio.format(DATE_FORMATTER),
                    fin.format(DATE_FORMATTER));

            semanas.add(label);
            inicio = inicio.plusWeeks(1);
        }

        choiceSemanas.setItems(FXCollections.observableArrayList(semanas));
        logger.info("Semanas cargadas (sin número): {}", semanas);
    }
}
