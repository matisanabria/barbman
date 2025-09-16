package app.barbman.core.controller;

import app.barbman.core.dto.ResumenDTO;
import app.barbman.core.repositories.caja.CajaRepository;
import app.barbman.core.repositories.caja.CajaRepositoryImpl;
import app.barbman.core.service.caja.CajaResumenService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

public class CajaResumenController implements Initializable {

    // ChoiceBox
    @FXML private ChoiceBox<String> choiceSemanas;
    @FXML private ChoiceBox<String> choiceMeses;

    // Labels Semanal
    @FXML private Label lblSemanaTitulo;
    @FXML private Label lblIngresosSem;
    @FXML private Label lblEgresosSem;
    @FXML private Label lblSaldoSem;
    @FXML private Label lblEfectivoSem;
    @FXML private Label lblTransferenciaSem;
    @FXML private Label lblPOSSem;

    // Labels Mensual
    @FXML private Label lblMesTitulo;
    @FXML private Label lblIngresosMes;
    @FXML private Label lblEgresosMes;
    @FXML private Label lblSaldoMes;
    @FXML private Label lblEfectivoMes;
    @FXML private Label lblTransferenciaMes;
    @FXML private Label lblPOSMes;

    private static final Logger logger = LogManager.getLogger(CajaResumenController.class);

    private final CajaRepository cajaRepo = new CajaRepositoryImpl();
    private final CajaResumenService resumenService = new CajaResumenService(cajaRepo);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarSemanas();
        cargarMeses();

        choiceSemanas.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                LocalDate fecha = LocalDate.parse(newVal.split(" ")[0]); // inicio semana
                mostrarResumenSemanal(fecha);
            }
        });

        choiceMeses.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                LocalDate fecha = LocalDate.parse(newVal + "-01"); // yyyy-MM
                mostrarResumenMensual(fecha);
            }
        });

        logger.info("[CAJA-RESUMEN] Vista inicializada.");
    }

    private void cargarSemanas() {
        List<LocalDate> fechas = cajaRepo.findAll().stream()
                .map(c -> c.getFecha())
                .distinct()
                .sorted()
                .toList();

        // Agrupamos por semana (lunes-domingo)
        Set<String> semanas = fechas.stream()
                .map(f -> {
                    LocalDate inicio = f.with(java.time.DayOfWeek.MONDAY);
                    LocalDate fin = f.with(java.time.DayOfWeek.SUNDAY);
                    return inicio + " - " + fin;
                })
                .collect(Collectors.toCollection(TreeSet::new));

        choiceSemanas.setItems(FXCollections.observableArrayList(semanas));

        if (!semanas.isEmpty()) {
            String ultimaSemana = ((TreeSet<String>) semanas).last();
            choiceSemanas.setValue(ultimaSemana);
            LocalDate fechaInicio = LocalDate.parse(ultimaSemana.split(" ")[0]);
            mostrarResumenSemanal(fechaInicio);
        }
    }

    private void cargarMeses() {
        List<LocalDate> fechas = cajaRepo.findAll().stream()
                .map(c -> c.getFecha().withDayOfMonth(1))
                .distinct()
                .sorted()
                .toList();

        List<String> meses = fechas.stream()
                .map(f -> f.getYear() + "-" + String.format("%02d", f.getMonthValue()))
                .toList();

        choiceMeses.setItems(FXCollections.observableArrayList(meses));

        if (!meses.isEmpty()) {
            String ultimoMes = meses.get(meses.size() - 1);
            choiceMeses.setValue(ultimoMes);
            LocalDate fecha = LocalDate.parse(ultimoMes + "-01");
            mostrarResumenMensual(fecha);
        }
    }

    private void mostrarResumenSemanal(LocalDate fecha) {
        ResumenDTO resumen = resumenService.getResumenSemanal(fecha);

        if (resumen == null) {
            lblSemanaTitulo.setText("Semana: ⚠ Sin registros");
            lblIngresosSem.setText("Ingresos totales: --");
            lblEgresosSem.setText("Egresos totales: --");
            lblSaldoSem.setText("Saldo final: --");
            lblEfectivoSem.setText("- Efectivo: --");
            lblTransferenciaSem.setText("- Transferencia: --");
            lblPOSSem.setText("- POS: --");
            return;
        }

        lblSemanaTitulo.setText("Semana: " + resumen.getDesde() + " a " + resumen.getHasta());
        lblIngresosSem.setText("Ingresos totales: " + (int) resumen.getIngresosTotal() + " Gs");
        lblEgresosSem.setText("Egresos totales: " + (int) resumen.getEgresosTotal() + " Gs");
        lblSaldoSem.setText("Saldo final: " + (int) resumen.getSaldoFinal() + " Gs");

        lblEfectivoSem.setText("- Efectivo: " + (int) resumen.getEfectivo() + " Gs");
        lblTransferenciaSem.setText("- Transferencia: " + (int) resumen.getTransferencia() + " Gs");
        lblPOSSem.setText("- POS: " + (int) resumen.getPos() + " Gs");

        logger.info("[CAJA-RESUMEN] Mostrando semanal {} -> {}", resumen.getDesde(), resumen.getHasta());
    }

    private void mostrarResumenMensual(LocalDate fecha) {
        ResumenDTO resumen = resumenService.getResumenMensual(fecha);

        if (resumen == null) {
            lblMesTitulo.setText("Mes: ⚠ Sin registros");
            lblIngresosMes.setText("Ingresos totales: --");
            lblEgresosMes.setText("Egresos totales: --");
            lblSaldoMes.setText("Saldo final: --");
            lblEfectivoMes.setText("- Efectivo: --");
            lblTransferenciaMes.setText("- Transferencia: --");
            lblPOSMes.setText("- POS: --");
            return;
        }

        String mesNombre = fecha.getMonth().getDisplayName(TextStyle.FULL, new Locale("es")).toUpperCase();
        lblMesTitulo.setText("Mes: " + mesNombre + " " + fecha.getYear());

        lblIngresosMes.setText("Ingresos totales: " + (int) resumen.getIngresosTotal() + " Gs");
        lblEgresosMes.setText("Egresos totales: " + (int) resumen.getEgresosTotal() + " Gs");
        lblSaldoMes.setText("Saldo final: " + (int) resumen.getSaldoFinal() + " Gs");

        lblEfectivoMes.setText("- Efectivo: " + (int) resumen.getEfectivo() + " Gs");
        lblTransferenciaMes.setText("- Transferencia: " + (int) resumen.getTransferencia() + " Gs");
        lblPOSMes.setText("- POS: " + (int) resumen.getPos() + " Gs");

        logger.info("[CAJA-RESUMEN] Mostrando mensual {} -> {}", resumen.getDesde(), resumen.getHasta());
    }
}
