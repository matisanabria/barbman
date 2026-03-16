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
import javafx.scene.control.ChoiceBox;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for the cashbox dashboard.
 * Displays live balances for current period + daily, weekly, and monthly reports.
 */
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

    // ============================================================
    // FXML - DIARIO
    // ============================================================

    @FXML private ChoiceBox<String> choiceFechas;
    @FXML private Label lblFechaDiaria;
    @FXML private Label lblCashInDiaria;
    @FXML private Label lblCashOutDiaria;
    @FXML private Label lblCashBalanceDiaria;
    @FXML private Label lblBankInDiaria;
    @FXML private Label lblBankOutDiaria;
    @FXML private Label lblBankBalanceDiaria;
    @FXML private Label lblTotalBalanceDiaria;
    @FXML private VBox boxProduccionDiaria;

    // ============================================================
    // FXML - SEMANAL
    // ============================================================

    @FXML private ChoiceBox<String> choiceSemanas;
    @FXML private Label lblSemana;
    @FXML private Label lblCashInSemanal;
    @FXML private Label lblCashOutSemanal;
    @FXML private Label lblCashBalanceSemanal;
    @FXML private Label lblBankInSemanal;
    @FXML private Label lblBankOutSemanal;
    @FXML private Label lblBankBalanceSemanal;
    @FXML private Label lblTotalBalanceSemanal;
    @FXML private VBox boxProduccionSemanal;

    // ============================================================
    // FXML - MENSUAL
    // ============================================================

    @FXML private ChoiceBox<String> choiceMeses;
    @FXML private Label lblMes;
    @FXML private Label lblCashInMensual;
    @FXML private Label lblCashOutMensual;
    @FXML private Label lblCashBalanceMensual;
    @FXML private Label lblBankInMensual;
    @FXML private Label lblBankOutMensual;
    @FXML private Label lblBankBalanceMensual;
    @FXML private Label lblTotalBalanceMensual;
    @FXML private VBox boxProduccionMensual;

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

        List<String> dateStrings = dates.stream()
                .map(d -> d.format(DATE_FORMATTER))
                .collect(Collectors.toList());

        choiceFechas.setItems(FXCollections.observableArrayList(dateStrings));

        choiceFechas.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                LocalDate date = LocalDate.parse(newVal, DATE_FORMATTER);
                showDailyReport(date);
            }
        });

        LocalDate today = LocalDate.now();
        String todayStr = today.format(DATE_FORMATTER);

        if (dateStrings.contains(todayStr)) {
            choiceFechas.setValue(todayStr);
        } else {
            choiceFechas.setValue(dateStrings.get(0));
        }
    }

    private void showDailyReport(LocalDate date) {
        try {
            CashboxReportDTO report = reportService.getDailyReport(date);

            lblFechaDiaria.setText(date.format(DATE_FORMATTER));

            lblCashInDiaria.setText("+ " + NumberFormatterUtil.format(report.getCashIn()) + " Gs");
            lblCashOutDiaria.setText("- " + NumberFormatterUtil.format(report.getCashOut()) + " Gs");
            lblCashBalanceDiaria.setText("= " + NumberFormatterUtil.format(report.getCashBalance()) + " Gs");

            lblBankInDiaria.setText("+ " + NumberFormatterUtil.format(report.getBankIn()) + " Gs");
            lblBankOutDiaria.setText("- " + NumberFormatterUtil.format(report.getBankOut()) + " Gs");
            lblBankBalanceDiaria.setText("= " + NumberFormatterUtil.format(report.getBankBalance()) + " Gs");

            lblTotalBalanceDiaria.setText(NumberFormatterUtil.format(report.getTotalBalance()) + " Gs");

            showProduction(boxProduccionDiaria, report);

        } catch (Exception e) {
            logger.error("{} Error showing daily report", PREFIX, e);
        }
    }

    private void showNoDailyData() {
        lblFechaDiaria.setText("No hay registros");
        choiceFechas.setDisable(true);
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
            lblSemana.setText(weekStart.format(DATE_FORMATTER) + " -> " + weekEnd.format(DATE_FORMATTER));

            lblCashInSemanal.setText("+ " + NumberFormatterUtil.format(report.getCashIn()) + " Gs");
            lblCashOutSemanal.setText("- " + NumberFormatterUtil.format(report.getCashOut()) + " Gs");
            lblCashBalanceSemanal.setText("= " + NumberFormatterUtil.format(report.getCashBalance()) + " Gs");

            lblBankInSemanal.setText("+ " + NumberFormatterUtil.format(report.getBankIn()) + " Gs");
            lblBankOutSemanal.setText("- " + NumberFormatterUtil.format(report.getBankOut()) + " Gs");
            lblBankBalanceSemanal.setText("= " + NumberFormatterUtil.format(report.getBankBalance()) + " Gs");

            lblTotalBalanceSemanal.setText(NumberFormatterUtil.format(report.getTotalBalance()) + " Gs");

            showProduction(boxProduccionSemanal, report);

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
            lblCashBalanceMensual.setText("= " + NumberFormatterUtil.format(report.getCashBalance()) + " Gs");

            lblBankInMensual.setText("+ " + NumberFormatterUtil.format(report.getBankIn()) + " Gs");
            lblBankOutMensual.setText("- " + NumberFormatterUtil.format(report.getBankOut()) + " Gs");
            lblBankBalanceMensual.setText("= " + NumberFormatterUtil.format(report.getBankBalance()) + " Gs");

            lblTotalBalanceMensual.setText(NumberFormatterUtil.format(report.getTotalBalance()) + " Gs");

            showProduction(boxProduccionMensual, report);

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
    // DATA HELPERS
    // ============================================================

    private List<LocalDate> getAvailableDates() {
        var movementRepo = new CashboxMovementRepositoryImpl();
        var movements = movementRepo.findAll();

        return movements.stream()
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
                            .owner((Stage) choiceFechas.getScene().getWindow())
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

        if (choiceFechas.getValue() != null) {
            LocalDate date = LocalDate.parse(choiceFechas.getValue(), DATE_FORMATTER);
            showDailyReport(date);
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
