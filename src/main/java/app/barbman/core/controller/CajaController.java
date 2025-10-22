package app.barbman.core.controller;

import app.barbman.core.dto.ResumenDTO;
import app.barbman.core.model.User;
import app.barbman.core.model.Egreso;
import app.barbman.core.model.ServicioRealizado;
import app.barbman.core.repositories.users.UsersRepository;
import app.barbman.core.repositories.users.UsersRepositoryImpl;
import app.barbman.core.repositories.egresos.EgresosRepository;
import app.barbman.core.repositories.egresos.EgresosRepositoryImpl;
import app.barbman.core.repositories.serviciorealizado.ServicioRealizadoRepository;
import app.barbman.core.repositories.serviciorealizado.ServicioRealizadoRepositoryImpl;
import app.barbman.core.service.caja.CajaService;
import app.barbman.core.util.NumberFormatUtil;
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

    private final ServicioRealizadoRepository serviciosRepo = new ServicioRealizadoRepositoryImpl();
    private final EgresosRepository egresosRepo = new EgresosRepositoryImpl();
    private final UsersRepository barberoRepo = new UsersRepositoryImpl();
    private final CajaService cajaService = new CajaService(serviciosRepo, egresosRepo);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarFechasDiario();
        choiceFechas.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                mostrarResumenDiario(LocalDate.parse(newVal, DATE_FORMATTER));
            }
        });

        if (!choiceFechas.getItems().isEmpty()) {
            String ultimaFecha = choiceFechas.getItems().get(0);
            choiceFechas.setValue(ultimaFecha);
            mostrarResumenDiario(LocalDate.parse(ultimaFecha, DATE_FORMATTER));
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
        List<ServicioRealizado> servicios = serviciosRepo.findAll();
        List<Egreso> egresos = egresosRepo.findAll();

        Set<LocalDate> fechasUnicas = new HashSet<>();
        servicios.forEach(s -> fechasUnicas.add(s.getFecha()));
        egresos.forEach(e -> fechasUnicas.add(e.getFecha()));

        List<String> fechasFormateadas = fechasUnicas.stream()
                .sorted(Comparator.reverseOrder())
                .map(f -> f.format(DATE_FORMATTER))
                .collect(Collectors.toList());

        choiceFechas.setItems(FXCollections.observableArrayList(fechasFormateadas));
    }

    private void mostrarResumenDiario(LocalDate fecha) {
        logger.info("Mostrando resumen diario para la fecha: {}", fecha);

        ResumenDTO resumen = cajaService.calcularResumenDiario(fecha);

        lblFechaDiaria.setText("Fecha: " + fecha.format(DATE_FORMATTER));
        lblIngresosDiaria.setText("Ingresos totales: " + NumberFormatUtil.format(resumen.getIngresosTotal()) + " Gs");
        lblEgresosDiaria.setText("Egresos totales: " + NumberFormatUtil.format(resumen.getEgresosTotal()) + " Gs");

        boxProduccionBarberosDiaria.getChildren().clear();
        List<User> users = barberoRepo.findAll();
        for (User b : users) {
            double produccion = serviciosRepo.getProduccionSemanalPorBarbero(b.getId(), fecha, fecha);
            Label lbl = new Label("- " + b.getName() + ": " + NumberFormatUtil.format(produccion) + " Gs");
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
        List<ServicioRealizado> servicios = serviciosRepo.findAll();
        List<Egreso> egresos = egresosRepo.findAll();

        Set<LocalDate> fechasUnicas = new HashSet<>();
        servicios.forEach(s -> fechasUnicas.add(s.getFecha()));
        egresos.forEach(e -> fechasUnicas.add(e.getFecha()));

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
        List<ServicioRealizado> serviciosRango = serviciosRepo.findAll().stream()
                .filter(s -> !s.getFecha().isBefore(desde) && !s.getFecha().isAfter(hasta))
                .collect(Collectors.toList());
        List<Egreso> egresosRango = egresosRepo.findAll().stream()
                .filter(e -> !e.getFecha().isBefore(desde) && !e.getFecha().isAfter(hasta))
                .collect(Collectors.toList());

        double totalIngresos = serviciosRango.stream().mapToDouble(ServicioRealizado::getPrecio).sum();
        double totalEgresos = egresosRango.stream().mapToDouble(Egreso::getMonto).sum();

        lblSemana.setText("Semana: " + desde.format(DATE_FORMATTER) + " al " + hasta.format(DATE_FORMATTER));
        lblIngresosSemana.setText("Ingresos totales: " + NumberFormatUtil.format(totalIngresos) + " Gs");
        lblEgresosSemana.setText("Egresos totales: " + NumberFormatUtil.format(totalEgresos) + " Gs");

        boxProduccionBarberos.getChildren().clear();
        List<User> users = barberoRepo.findAll();
        for (User b : users) {
            double produccion = serviciosRepo.getProduccionSemanalPorBarbero(b.getId(), desde, hasta);
            Label lbl = new Label("- " + b.getName() + ": " + NumberFormatUtil.format(produccion) + " Gs");
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