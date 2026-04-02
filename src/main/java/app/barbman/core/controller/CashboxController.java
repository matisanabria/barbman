package app.barbman.core.controller;

import app.barbman.core.controller.cashbox.CashboxClosureController;
import app.barbman.core.dto.CashboxReportDTO;
import app.barbman.core.model.cashbox.CashboxOpening;
import app.barbman.core.repositories.cashbox.closure.CashboxClosureRepositoryImpl;
import app.barbman.core.repositories.cashbox.movement.CashboxMovementRepositoryImpl;
import app.barbman.core.repositories.cashbox.opening.CashboxOpeningRepositoryImpl;
import app.barbman.core.repositories.sales.products.productheader.ProductHeaderRepositoryImpl;
import app.barbman.core.repositories.sales.services.serviceheader.ServiceHeaderRepositoryImpl;
import app.barbman.core.repositories.users.UsersRepositoryImpl;
import app.barbman.core.service.cashbox.CashboxReportService;
import app.barbman.core.service.cashbox.CashboxService;
import app.barbman.core.util.NumberFormatterUtil;
import app.barbman.core.util.window.WindowManager;
import app.barbman.core.util.window.WindowRequest;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.chart.*;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

public class CashboxController implements Initializable {

    private static final Logger logger = LogManager.getLogger(CashboxController.class);
    private static final String PREFIX = "[CAJA-CONTROLLER]";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");

    // ============================================================
    // SERVICES
    // ============================================================

    private final CashboxReportService reportService;
    private final CashboxService cashboxService;

    // ============================================================
    // FXML - LIVE BALANCE
    // ============================================================

    @FXML private Label lblLiveOpenedSince;
    @FXML private Label lblLiveCash;
    @FXML private Label lblLiveBank;
    @FXML private Label lblLiveTotal;
    @FXML private Label lblLiveTotalIn;
    @FXML private Label lblLiveTotalOut;

    // ============================================================
    // FXML - DIARIO
    // ============================================================

    @FXML private DatePicker dateFechas;
    @FXML private Label lblFechaDiaria;
    @FXML private Label lblCashInDiaria;
    @FXML private Label lblCashOutDiaria;
    @FXML private Label lblBankInDiaria;
    @FXML private Label lblBankOutDiaria;
    @FXML private Label lblTotalInDiaria;
    @FXML private Label lblTotalOutDiaria;
    @FXML private Label lblTotalBalanceDiaria;
    @FXML private VBox boxProduccionDiaria;
    @FXML private VBox chartContainerDiario;

    // ============================================================
    // FXML - SEMANAL
    // ============================================================

    @FXML private ChoiceBox<String> choiceSemanas;
    @FXML private Label lblSemana;
    @FXML private Label lblCashInSemanal;
    @FXML private Label lblCashOutSemanal;
    @FXML private Label lblBankInSemanal;
    @FXML private Label lblBankOutSemanal;
    @FXML private Label lblTotalInSemanal;
    @FXML private Label lblTotalOutSemanal;
    @FXML private Label lblTotalBalanceSemanal;
    @FXML private VBox boxProduccionSemanal;
    @FXML private VBox chartContainerSemanal;

    // ============================================================
    // FXML - MENSUAL
    // ============================================================

    @FXML private ChoiceBox<String> choiceMeses;
    @FXML private Label lblMes;
    @FXML private Label lblCashInMensual;
    @FXML private Label lblCashOutMensual;
    @FXML private Label lblBankInMensual;
    @FXML private Label lblBankOutMensual;
    @FXML private Label lblTotalInMensual;
    @FXML private Label lblTotalOutMensual;
    @FXML private Label lblTotalBalanceMensual;
    @FXML private VBox boxProduccionMensual;
    @FXML private VBox chartContainerMensual;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public CashboxController() {
        var openingRepo = new CashboxOpeningRepositoryImpl();
        var closureRepo = new CashboxClosureRepositoryImpl();
        var movementRepo = new CashboxMovementRepositoryImpl();

        this.cashboxService = new CashboxService(openingRepo, closureRepo, movementRepo);
        this.reportService = new CashboxReportService(
                movementRepo,
                new ServiceHeaderRepositoryImpl(),
                new ProductHeaderRepositoryImpl(),
                new UsersRepositoryImpl(),
                openingRepo
        );
    }

    // ============================================================
    // INIT
    // ============================================================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("{} Initializing cashbox dashboard", PREFIX);

        refreshLiveBalance();
        setupDailyReport();
        setupWeeklyReport();
        setupMonthlyReport();

        logger.info("{} Dashboard initialized", PREFIX);
    }

    // ============================================================
    // LIVE BALANCE
    // ============================================================

    private void refreshLiveBalance() {
        CashboxOpening opening = cashboxService.getCurrentOpening();
        if (opening == null) {
            lblLiveOpenedSince.setText("Sin caja abierta");
            lblLiveCash.setText("0 Gs");
            lblLiveBank.setText("0 Gs");
            lblLiveTotal.setText("0 Gs");
            lblLiveTotalIn.setText("0 Gs");
            lblLiveTotalOut.setText("0 Gs");
            return;
        }

        lblLiveOpenedSince.setText("Abierta desde: " +
                opening.getOpenedAt().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")));

        double cash = cashboxService.getExpectedCash(opening.getId());
        double bank = cashboxService.getExpectedBank(opening.getId());
        double total = cash + bank;

        lblLiveCash.setText(NumberFormatterUtil.format(cash) + " Gs");
        lblLiveBank.setText(NumberFormatterUtil.format(bank) + " Gs");
        lblLiveTotal.setText(NumberFormatterUtil.format(total) + " Gs");

        try {
            CashboxReportDTO periodReport = reportService.getCurrentPeriodReport();
            lblLiveTotalIn.setText("+ " + NumberFormatterUtil.format(periodReport.getTotalIn()) + " Gs");
            lblLiveTotalOut.setText("- " + NumberFormatterUtil.format(periodReport.getTotalOut()) + " Gs");
        } catch (Exception e) {
            logger.error("{} Error loading live summary", PREFIX, e);
            lblLiveTotalIn.setText("0 Gs");
            lblLiveTotalOut.setText("0 Gs");
        }
    }

    // ============================================================
    // DAILY REPORT
    // ============================================================

    private void setupDailyReport() {
        List<LocalDate> dates = getAvailableDates();

        if (dates.isEmpty()) {
            showNoDailyData();
            return;
        }

        Set<LocalDate> availableSet = new HashSet<>(dates);

        dateFechas.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || !availableSet.contains(date));
            }
        });

        dateFechas.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && availableSet.contains(newVal)) {
                showDailyReport(newVal);
            }
        });

        LocalDate today = LocalDate.now();
        dateFechas.setValue(availableSet.contains(today) ? today : dates.get(0));
    }

    private void showDailyReport(LocalDate date) {
        try {
            CashboxReportDTO report = reportService.getDailyReport(date);

            lblFechaDiaria.setText(date.format(DATE_FORMATTER));

            lblCashInDiaria.setText("+ " + NumberFormatterUtil.format(report.getCashIn()) + " Gs");
            lblCashOutDiaria.setText("- " + NumberFormatterUtil.format(report.getCashOut()) + " Gs");

            lblBankInDiaria.setText("+ " + NumberFormatterUtil.format(report.getBankIn()) + " Gs");
            lblBankOutDiaria.setText("- " + NumberFormatterUtil.format(report.getBankOut()) + " Gs");

            lblTotalInDiaria.setText("+ " + NumberFormatterUtil.format(report.getTotalIn()) + " Gs");
            lblTotalOutDiaria.setText("- " + NumberFormatterUtil.format(report.getTotalOut()) + " Gs");
            lblTotalBalanceDiaria.setText(NumberFormatterUtil.format(report.getTotalBalance()) + " Gs");

            showProduction(boxProduccionDiaria, report);
            buildDailyPieChart(report);

        } catch (Exception e) {
            logger.error("{} Error showing daily report", PREFIX, e);
        }
    }

    private void showNoDailyData() {
        lblFechaDiaria.setText("No hay registros");
        dateFechas.setDisable(true);
    }

    // ============================================================
    // WEEKLY REPORT
    // ============================================================

    private void setupWeeklyReport() {
        List<LocalDate> weekStarts = getAvailableWeeks();

        if (weekStarts.isEmpty()) {
            showNoWeeklyData();
            return;
        }

        List<String> weekStrings = weekStarts.stream()
                .map(start -> {
                    LocalDate end = start.plusDays(6);
                    return start.format(DATE_FORMATTER) + " -> " + end.format(DATE_FORMATTER);
                })
                .collect(Collectors.toList());

        choiceSemanas.setItems(FXCollections.observableArrayList(weekStrings));

        choiceSemanas.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                String[] parts = newVal.split(" -> ");
                LocalDate start = LocalDate.parse(parts[0].trim(), DATE_FORMATTER);
                showWeeklyReport(start);
            }
        });

        LocalDate currentWeekStart = LocalDate.now().with(DayOfWeek.MONDAY);
        String currentWeekStr = currentWeekStart.format(DATE_FORMATTER) + " -> " +
                currentWeekStart.plusDays(6).format(DATE_FORMATTER);

        if (weekStrings.contains(currentWeekStr)) {
            choiceSemanas.setValue(currentWeekStr);
        } else {
            choiceSemanas.setValue(weekStrings.get(0));
        }
    }

    private void showWeeklyReport(LocalDate weekStart) {
        try {
            CashboxReportDTO report = reportService.getWeeklyReport(weekStart);

            LocalDate weekEnd = weekStart.plusDays(6);
            lblSemana.setText(weekStart.format(DATE_FORMATTER) + " - " + weekEnd.format(DATE_FORMATTER));

            lblCashInSemanal.setText("+ " + NumberFormatterUtil.format(report.getCashIn()) + " Gs");
            lblCashOutSemanal.setText("- " + NumberFormatterUtil.format(report.getCashOut()) + " Gs");

            lblBankInSemanal.setText("+ " + NumberFormatterUtil.format(report.getBankIn()) + " Gs");
            lblBankOutSemanal.setText("- " + NumberFormatterUtil.format(report.getBankOut()) + " Gs");

            lblTotalInSemanal.setText("+ " + NumberFormatterUtil.format(report.getTotalIn()) + " Gs");
            lblTotalOutSemanal.setText("- " + NumberFormatterUtil.format(report.getTotalOut()) + " Gs");
            lblTotalBalanceSemanal.setText(NumberFormatterUtil.format(report.getTotalBalance()) + " Gs");

            showProduction(boxProduccionSemanal, report);
            buildWeeklyBarChart(weekStart);

        } catch (Exception e) {
            logger.error("{} Error showing weekly report", PREFIX, e);
        }
    }

    private void showNoWeeklyData() {
        lblSemana.setText("No hay registros");
        choiceSemanas.setDisable(true);
    }

    // ============================================================
    // MONTHLY REPORT
    // ============================================================

    private void setupMonthlyReport() {
        List<YearMonth> months = getAvailableMonths();

        if (months.isEmpty()) {
            showNoMonthlyData();
            return;
        }

        List<String> monthStrings = months.stream()
                .map(m -> m.format(MONTH_FORMATTER))
                .collect(Collectors.toList());

        choiceMeses.setItems(FXCollections.observableArrayList(monthStrings));

        choiceMeses.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                YearMonth month = YearMonth.parse(newVal, MONTH_FORMATTER);
                showMonthlyReport(month);
            }
        });

        YearMonth currentMonth = YearMonth.now();
        String currentMonthStr = currentMonth.format(MONTH_FORMATTER);

        if (monthStrings.contains(currentMonthStr)) {
            choiceMeses.setValue(currentMonthStr);
        } else {
            choiceMeses.setValue(monthStrings.get(0));
        }
    }

    private void showMonthlyReport(YearMonth month) {
        try {
            CashboxReportDTO report = reportService.getMonthlyReport(month);

            lblMes.setText(month.format(MONTH_FORMATTER));

            lblCashInMensual.setText("+ " + NumberFormatterUtil.format(report.getCashIn()) + " Gs");
            lblCashOutMensual.setText("- " + NumberFormatterUtil.format(report.getCashOut()) + " Gs");

            lblBankInMensual.setText("+ " + NumberFormatterUtil.format(report.getBankIn()) + " Gs");
            lblBankOutMensual.setText("- " + NumberFormatterUtil.format(report.getBankOut()) + " Gs");

            lblTotalInMensual.setText("+ " + NumberFormatterUtil.format(report.getTotalIn()) + " Gs");
            lblTotalOutMensual.setText("- " + NumberFormatterUtil.format(report.getTotalOut()) + " Gs");
            lblTotalBalanceMensual.setText(NumberFormatterUtil.format(report.getTotalBalance()) + " Gs");

            showProduction(boxProduccionMensual, report);
            buildMonthlyBarChart(month);

        } catch (Exception e) {
            logger.error("{} Error showing monthly report", PREFIX, e);
        }
    }

    private void showNoMonthlyData() {
        lblMes.setText("No hay registros");
        choiceMeses.setDisable(true);
    }

    // ============================================================
    // PRODUCTION
    // ============================================================

    private void showProduction(VBox container, CashboxReportDTO report) {
        container.getChildren().clear();

        if (report.getProductionByUser().isEmpty()) {
            Label lbl = new Label("No hay produccion registrada");
            lbl.getStyleClass().add("caja-production-item");
            container.getChildren().add(lbl);
            return;
        }

        report.getProductionByUser().entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .forEach(entry -> {
                    int userId = entry.getKey();
                    double production = entry.getValue();
                    String userName = report.getUserNames().getOrDefault(userId, "Usuario " + userId);

                    Label lbl = new Label(userName + ": " + NumberFormatterUtil.format(production) + " Gs");
                    lbl.getStyleClass().add("caja-production-item");
                    container.getChildren().add(lbl);
                });
    }

    // ============================================================
    // CHARTS
    // ============================================================

    private void buildDailyPieChart(CashboxReportDTO report) {
        chartContainerDiario.getChildren().clear();

        double cashIn = report.getCashIn();
        double bankIn = report.getBankIn();

        if (cashIn == 0 && bankIn == 0) {
            Label lbl = new Label("Sin ingresos para graficar");
            lbl.getStyleClass().add("caja-no-chart-data");
            chartContainerDiario.getChildren().add(lbl);
            return;
        }

        PieChart chart = new PieChart();
        if (cashIn > 0) {
            chart.getData().add(new PieChart.Data(
                    "Efectivo (" + NumberFormatterUtil.format(cashIn) + ")", cashIn));
        }
        if (bankIn > 0) {
            chart.getData().add(new PieChart.Data(
                    "Banco (" + NumberFormatterUtil.format(bankIn) + ")", bankIn));
        }

        chart.setTitle("Distribucion de Ingresos");
        chart.setLabelsVisible(false);
        chart.setLegendSide(Side.BOTTOM);
        chart.setPrefHeight(280);
        chart.setMaxHeight(280);
        chart.setAnimated(false);
        chart.getStyleClass().add("caja-chart");

        chartContainerDiario.getChildren().add(chart);
    }

    @SuppressWarnings("unchecked")
    private void buildWeeklyBarChart(LocalDate weekStart) {
        chartContainerSemanal.getChildren().clear();

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Gs");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Ingresos vs Egresos por Dia");
        chart.setPrefHeight(300);
        chart.setMaxHeight(300);
        chart.setAnimated(false);
        chart.setCategoryGap(12);
        chart.setBarGap(2);
        chart.setLegendSide(Side.BOTTOM);
        chart.getStyleClass().add("caja-chart");

        XYChart.Series<String, Number> seriesIn = new XYChart.Series<>();
        seriesIn.setName("Ingresos");
        XYChart.Series<String, Number> seriesOut = new XYChart.Series<>();
        seriesOut.setName("Egresos");

        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            String dayLabel = day.getDayOfWeek()
                    .getDisplayName(TextStyle.SHORT, new Locale("es"))
                    + " " + day.getDayOfMonth();

            try {
                CashboxReportDTO dayReport = reportService.getDailyReport(day);
                seriesIn.getData().add(new XYChart.Data<>(dayLabel, dayReport.getTotalIn()));
                seriesOut.getData().add(new XYChart.Data<>(dayLabel, dayReport.getTotalOut()));
            } catch (Exception e) {
                seriesIn.getData().add(new XYChart.Data<>(dayLabel, 0));
                seriesOut.getData().add(new XYChart.Data<>(dayLabel, 0));
            }
        }

        chart.getData().addAll(seriesIn, seriesOut);
        chartContainerSemanal.getChildren().add(chart);
    }

    @SuppressWarnings("unchecked")
    private void buildMonthlyBarChart(YearMonth month) {
        chartContainerMensual.getChildren().clear();

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Gs");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Ingresos vs Egresos por Semana");
        chart.setPrefHeight(300);
        chart.setMaxHeight(300);
        chart.setAnimated(false);
        chart.setCategoryGap(20);
        chart.setBarGap(2);
        chart.setLegendSide(Side.BOTTOM);
        chart.getStyleClass().add("caja-chart");

        XYChart.Series<String, Number> seriesIn = new XYChart.Series<>();
        seriesIn.setName("Ingresos");
        XYChart.Series<String, Number> seriesOut = new XYChart.Series<>();
        seriesOut.setName("Egresos");

        LocalDate monthStart = month.atDay(1);
        LocalDate monthEnd = month.atEndOfMonth();

        int weekNum = 1;
        LocalDate chunkStart = monthStart;

        while (!chunkStart.isAfter(monthEnd)) {
            LocalDate chunkEnd = chunkStart.plusDays(6);
            if (chunkEnd.isAfter(monthEnd)) chunkEnd = monthEnd;

            String label = chunkStart.getDayOfMonth() + "-" + chunkEnd.getDayOfMonth() + "/" +
                    chunkEnd.getMonthValue();

            double totalIn = 0, totalOut = 0;
            LocalDate day = chunkStart;
            while (!day.isAfter(chunkEnd)) {
                try {
                    CashboxReportDTO dayReport = reportService.getDailyReport(day);
                    totalIn += dayReport.getTotalIn();
                    totalOut += dayReport.getTotalOut();
                } catch (Exception e) {
                    // skip
                }
                day = day.plusDays(1);
            }

            seriesIn.getData().add(new XYChart.Data<>(label, totalIn));
            seriesOut.getData().add(new XYChart.Data<>(label, totalOut));

            chunkStart = chunkEnd.plusDays(1);
            weekNum++;
        }

        chart.getData().addAll(seriesIn, seriesOut);
        chartContainerMensual.getChildren().add(chart);
    }

    // ============================================================
    // DATA HELPERS
    // ============================================================

    private List<LocalDate> getAvailableDates() {
        var movementRepo = new CashboxMovementRepositoryImpl();
        var movements = movementRepo.findAll();

        return movements.stream()
                .filter(m -> !"OPENING".equals(m.getMovementType()))
                .map(m -> m.getOccurredAt().toLocalDate())
                .distinct()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
    }

    private List<LocalDate> getAvailableWeeks() {
        List<LocalDate> dates = getAvailableDates();

        if (dates.isEmpty()) {
            return List.of();
        }

        LocalDate minDate = dates.stream().min(LocalDate::compareTo).orElse(LocalDate.now());
        LocalDate maxDate = dates.stream().max(LocalDate::compareTo).orElse(LocalDate.now());

        List<LocalDate> weeks = new ArrayList<>();
        LocalDate weekStart = minDate.with(DayOfWeek.MONDAY);

        while (!weekStart.isAfter(maxDate)) {
            weeks.add(weekStart);
            weekStart = weekStart.plusWeeks(1);
        }

        Collections.reverse(weeks);
        return weeks;
    }

    private List<YearMonth> getAvailableMonths() {
        List<LocalDate> dates = getAvailableDates();

        if (dates.isEmpty()) {
            return List.of();
        }

        return dates.stream()
                .map(YearMonth::from)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
    }

    // ============================================================
    // ACTIONS
    // ============================================================

    @FXML
    private void onCloseCashbox() {
        logger.info("{} Opening cashbox closure modal", PREFIX);

        try {
            CashboxClosureController controller = (CashboxClosureController) WindowManager.openModal(
                    WindowRequest.builder()
                            .fxml("/app/barbman/core/view/cashbox-closure-view.fxml")
                            .title("Cerrar Caja")
                            .css("/app/barbman/core/style/cashbox-closure.css")
                            .owner((Stage) dateFechas.getScene().getWindow())
                            .modal(true)
                            .resizable(false)
                            .returnController(true)
                            .build()
            );

            if (controller != null) {
                controller.setOnClosureSuccess(() -> {
                    refreshLiveBalance();
                    refreshAllReports();
                });
            }

        } catch (Exception e) {
            logger.error("{} Error opening closure modal", PREFIX, e);
        }
    }

    private void refreshAllReports() {
        logger.info("{} Refreshing all reports after closure", PREFIX);

        if (dateFechas.getValue() != null) {
            showDailyReport(dateFechas.getValue());
        }

        if (choiceSemanas.getValue() != null) {
            String[] parts = choiceSemanas.getValue().split(" -> ");
            LocalDate start = LocalDate.parse(parts[0].trim(), DATE_FORMATTER);
            showWeeklyReport(start);
        }

        if (choiceMeses.getValue() != null) {
            YearMonth month = YearMonth.parse(choiceMeses.getValue(), MONTH_FORMATTER);
            showMonthlyReport(month);
        }
    }
}
