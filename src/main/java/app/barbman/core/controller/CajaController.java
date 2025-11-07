package app.barbman.core.controller;

import app.barbman.core.model.Expense;
import app.barbman.core.model.services.Service;
import app.barbman.core.model.User;
import app.barbman.core.repositories.expense.ExpenseRepositoryImpl;
import app.barbman.core.repositories.users.UsersRepository;
import app.barbman.core.repositories.users.UsersRepositoryImpl;
import app.barbman.core.repositories.expense.ExpenseRepository;
import app.barbman.core.repositories.services.service.ServiceRepository;
import app.barbman.core.repositories.services.service.ServiceRepositoryImpl;

import app.barbman.core.util.NumberFormatterUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class CajaController implements Initializable {
    // ======= Caja diaria =======
    @FXML private ChoiceBox<String> choiceFechas;
    @FXML private Label lblFechaDiaria;
    @FXML private Label lblIngresosDiaria;
    @FXML private Label lblEgresosDiaria;
    @FXML private VBox boxProduccionBarberosDiaria;

    // ======= Caja semanal =======
    @FXML private ChoiceBox<String> choiceSemanas;
    @FXML private Label lblSemana;
    @FXML private Label lblIngresosSemana;
    @FXML private Label lblEgresosSemana;
    @FXML private VBox boxProduccionBarberos;

    private static final Logger logger = LogManager.getLogger(CajaController.class);

    private final ServiceRepository serviciosRepo = new ServiceRepositoryImpl();
    private final ExpenseRepository egresosRepo = new ExpenseRepositoryImpl();
    private final UsersRepository barberoRepo = new UsersRepositoryImpl();
   // private final CajaService cajaService = new CajaService(serviciosRepo, egresosRepo);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarFechasDiario();
        choiceFechas.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
               // mostrarResumenDiario(LocalDate.parse(newVal, DATE_FORMATTER));
            }
        });

        if (!choiceFechas.getItems().isEmpty()) {
            String ultimaFecha = choiceFechas.getItems().get(0);
            choiceFechas.setValue(ultimaFecha);
            //mostrarResumenDiario(LocalDate.parse(ultimaFecha, DATE_FORMATTER));
        } else {
            mostrarNoHayRegistrosDiario();
        }

        cargarSemanasSemanal();
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
            String ultimaSemana = choiceSemanas.getItems().get(0);
            choiceSemanas.setValue(ultimaSemana);

            String[] partes = ultimaSemana.split(" -> ");
            LocalDate desde = LocalDate.parse(partes[0].trim(), DATE_FORMATTER);
            LocalDate hasta = LocalDate.parse(partes[1].trim(), DATE_FORMATTER);

            mostrarResumenSemanal(desde, hasta);
        }

        logger.info("Vista de Caja inicializada con cierres diarios y semanales (formato simple).");
    }

    // ======= Métodos DIARIOS =======
    private void cargarFechasDiario() {
        List<Service> servicios = serviciosRepo.findAll();
        List<Expense> expenses = egresosRepo.findAll();

        Set<LocalDate> fechasUnicas = new HashSet<>();
        servicios.forEach(s -> fechasUnicas.add(s.getDate()));
        expenses.forEach(e -> fechasUnicas.add(e.getDate()));

        List<String> fechasFormateadas = fechasUnicas.stream()
                .sorted(Comparator.reverseOrder())
                .map(f -> f.format(DATE_FORMATTER))
                .collect(Collectors.toList());

        choiceFechas.setItems(FXCollections.observableArrayList(fechasFormateadas));
    }
/*
    private void mostrarResumenDiario(LocalDate fecha) {
        logger.info("Mostrando resumen diario para la fecha: {}", fecha);

        ResumenDTO resumen = cajaService.calcularResumenDiario(fecha);

        lblFechaDiaria.setText("Fecha: " + fecha.format(DATE_FORMATTER));
        lblIngresosDiaria.setText("Ingresos totales: " + NumberFormatterUtil.format(resumen.getIngresosTotal()) + " Gs");
        lblEgresosDiaria.setText("Egresos totales: " + NumberFormatterUtil.format(resumen.getEgresosTotal()) + " Gs");

        boxProduccionBarberosDiaria.getChildren().clear();
        List<User> users = barberoRepo.findAll();
        for (User b : users) {
            double produccion = serviciosRepo.getProduccionSemanalPorBarbero(b.getId(), fecha, fecha);
            Label lbl = new Label("- " + b.getName() + ": " + NumberFormatterUtil.format(produccion) + " Gs");
            lbl.getStyleClass().add("caja-label");
            boxProduccionBarberosDiaria.getChildren().add(lbl);
            logger.debug("Producción para barbero {} el {}: {}", b.getName(), fecha, produccion);
        }
        if (users.isEmpty()) {
            Label lbl = new Label("No hay users registrados.");
            lbl.getStyleClass().add("caja-label");
            boxProduccionBarberosDiaria.getChildren().add(lbl);
            logger.warn("No hay users registrados en la fecha: {}", fecha);
        }
    }
*/
    private void mostrarNoHayRegistrosDiario() {
        lblFechaDiaria.setText("⚠ No hay registros");
        lblIngresosDiaria.setText("Ingresos totales: --");
        lblEgresosDiaria.setText("Egresos totales: --");
        boxProduccionBarberosDiaria.getChildren().clear();
        Label lbl = new Label("No hay barberos registrados.");
        lbl.getStyleClass().add("caja-label");
        boxProduccionBarberosDiaria.getChildren().add(lbl);
        logger.warn("No hay registros de caja diaria para la fecha seleccionada.");
    }

    // ======= Métodos SEMANALES =======
    private void cargarSemanasSemanal() {
        List<Service> servicios = serviciosRepo.findAll();
        List<Expense> expenses = egresosRepo.findAll();

        Set<LocalDate> fechasUnicas = new HashSet<>();
        servicios.forEach(s -> fechasUnicas.add(s.getDate()));
        expenses.forEach(e -> fechasUnicas.add(e.getDate()));

        if (fechasUnicas.isEmpty()) {
            choiceSemanas.setItems(FXCollections.observableArrayList());
            logger.warn("No hay registros para calcular semanas.");
            return;
        }

        LocalDate minFecha = fechasUnicas.stream().min(LocalDate::compareTo).orElse(LocalDate.now());
        LocalDate maxFecha = fechasUnicas.stream().max(LocalDate::compareTo).orElse(LocalDate.now());

        LocalDate inicio = minFecha.with(java.time.DayOfWeek.MONDAY);
        List<String> semanas = new ArrayList<>();

        while (!inicio.isAfter(maxFecha)) {
            LocalDate fin = inicio.plusDays(6);
            String label = String.format("%s -> %s",
                    inicio.format(DATE_FORMATTER),
                    fin.format(DATE_FORMATTER));
            semanas.add(label);
            inicio = inicio.plusWeeks(1);
        }

        Collections.reverse(semanas);
        choiceSemanas.setItems(FXCollections.observableArrayList(semanas));
        logger.info("Semanas cargadas: {}", semanas);
    }

    private void mostrarResumenSemanal(LocalDate desde, LocalDate hasta) {
        List<Service> serviciosRango = serviciosRepo.findAll().stream()
                .filter(s -> !s.getDate().isBefore(desde) && !s.getDate().isAfter(hasta))
                .collect(Collectors.toList());
        List<Expense> egresosRangos = egresosRepo.findAll().stream()
                .filter(e -> !e.getDate().isBefore(desde) && !e.getDate().isAfter(hasta))
                .collect(Collectors.toList());

        //double totalIngresos = serviciosRango.stream().mapToDouble(Service::getPrice).sum();
        double totalEgresos = egresosRangos.stream().mapToDouble(Expense::getAmount).sum();

        lblSemana.setText("Semana: " + desde.format(DATE_FORMATTER) + " al " + hasta.format(DATE_FORMATTER));
        //lblIngresosSemana.setText("Ingresos totales: " + NumberFormatterUtil.format(totalIngresos) + " Gs");
        lblEgresosSemana.setText("Egresos totales: " + NumberFormatterUtil.format(totalEgresos) + " Gs");

        boxProduccionBarberos.getChildren().clear();
        List<User> users = barberoRepo.findAll();
        for (User b : users) {
            double produccion = serviciosRepo.getProduccionSemanalPorBarbero(b.getId(), desde, hasta);
            Label lbl = new Label("- " + b.getName() + ": " + NumberFormatterUtil.format(produccion) + " Gs");
            lbl.getStyleClass().add("caja-label");
            boxProduccionBarberos.getChildren().add(lbl);
        }

        if (users.isEmpty()) {
            Label lbl = new Label("No hay users registrados.");
            lbl.getStyleClass().add("caja-label");
            boxProduccionBarberos.getChildren().add(lbl);
        }

        logger.info("Resumen semanal mostrado para {} - {}", desde, hasta);
    }
}