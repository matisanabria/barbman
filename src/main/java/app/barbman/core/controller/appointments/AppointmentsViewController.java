package app.barbman.core.controller.appointments;

import app.barbman.core.infrastructure.EnvConfig;
import app.barbman.core.service.OnBarberApiClient;
import app.barbman.core.service.OnBarberApiClient.AppointmentDTO;
import app.barbman.core.service.OnBarberApiClient.BarberDTO;
import app.barbman.core.util.window.WindowManager;
import app.barbman.core.util.window.WindowRequest;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class AppointmentsViewController {

    private static final Logger logger = LogManager.getLogger(AppointmentsViewController.class);
    private static final String PREFIX = "[APPOINTMENTS]";

    private final OnBarberApiClient apiClient = new OnBarberApiClient();
    private LocalDate weekStart;

    // Cached data
    private List<BarberDTO> barbers = new ArrayList<>();
    private List<AppointmentDTO> allAppointments = new ArrayList<>();

    // Map: "yyyy-MM-dd" -> list of available time strings ("09:00", "10:00", ...)
    private final Map<String, List<String>> availableSlotsCache = new HashMap<>();

    @FXML private Label weekLabel;
    @FXML private Label statTotal;
    @FXML private Label statPending;
    @FXML private Label statConfirmed;
    @FXML private Label statusLabel;
    @FXML private ComboBox<BarberDTO> barberCombo;
    @FXML private GridPane scheduleGrid;
    @FXML private ScrollPane gridScroll;
    @FXML private VBox loadingPane;
    @FXML private ProgressIndicator loadingSpinner;
    @FXML private Label loadingLabel;

    @FXML
    private void initialize() {
        logger.info("{} Initializing", PREFIX);

        weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        if (!EnvConfig.isConfigured()) {
            showStatus("API no configurada. Configurar el archivo .env con ONBARBER_API_URL y ONBARBER_API_TOKEN.");
            return;
        }

        setupBarberCombo();
        updateWeekLabel();
        loadBarbers();
    }

    // ============================================================
    // BARBER SELECTOR
    // ============================================================

    private void setupBarberCombo() {
        barberCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(BarberDTO b) { return b != null ? b.getName() : ""; }
            @Override
            public BarberDTO fromString(String s) { return null; }
        });
        barberCombo.valueProperty().addListener((obs, old, val) -> {
            if (val != null) {
                loadWeekData();
            }
        });
    }

    private void loadBarbers() {
        showLoading("Cargando barberos...");

        Thread thread = new Thread(() -> {
            try {
                List<BarberDTO> result = apiClient.getBarbers();
                List<AppointmentDTO> appointments = apiClient.getAppointments();
                Platform.runLater(() -> {
                    barbers = result;
                    allAppointments = appointments;
                    barberCombo.setItems(FXCollections.observableArrayList(barbers));
                    if (!barbers.isEmpty()) {
                        barberCombo.setValue(barbers.get(0));
                    }
                    hideLoading();
                });
            } catch (Exception e) {
                logger.error("{} Failed to load barbers: {}", PREFIX, e.getMessage());
                Platform.runLater(() -> {
                    hideLoading();
                    showStatus("Error al conectar con la API: " + e.getMessage());
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // ============================================================
    // WEEK NAVIGATION
    // ============================================================

    @FXML
    private void handlePrevWeek() {
        weekStart = weekStart.minusWeeks(1);
        updateWeekLabel();
        loadWeekData();
    }

    @FXML
    private void handleNextWeek() {
        weekStart = weekStart.plusWeeks(1);
        updateWeekLabel();
        loadWeekData();
    }

    @FXML
    private void handleToday() {
        weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        updateWeekLabel();
        loadWeekData();
    }

    private void updateWeekLabel() {
        LocalDate weekEnd = weekStart.plusDays(6);
        Locale es = new Locale("es");

        String startDay = String.valueOf(weekStart.getDayOfMonth());
        String endDay = String.valueOf(weekEnd.getDayOfMonth());
        String startMonth = capitalize(weekStart.getMonth().getDisplayName(TextStyle.FULL, es));
        String endMonth = capitalize(weekEnd.getMonth().getDisplayName(TextStyle.FULL, es));

        if (weekStart.getMonth() == weekEnd.getMonth()) {
            weekLabel.setText("Semana del " + startDay + " al " + endDay + " de " + endMonth + " " + weekEnd.getYear());
        } else {
            weekLabel.setText(startDay + " " + startMonth + " — " + endDay + " " + endMonth + " " + weekEnd.getYear());
        }
    }

    // ============================================================
    // DATA LOADING
    // ============================================================

    private void loadWeekData() {
        BarberDTO barber = barberCombo.getValue();
        if (barber == null) return;

        showLoading("Cargando horarios...");
        availableSlotsCache.clear();

        Thread thread = new Thread(() -> {
            try {
                ExecutorService pool = Executors.newFixedThreadPool(8);

                // All 8 requests in parallel: 1 appointments + 7 slots
                Future<List<AppointmentDTO>> appointmentsFuture =
                        pool.submit(() -> apiClient.getAppointments());

                Map<String, Future<List<String>>> slotFutures = new LinkedHashMap<>();
                for (int i = 0; i < 7; i++) {
                    LocalDate day = weekStart.plusDays(i);
                    String dateStr = day.toString();
                    int barberId = barber.getId();
                    slotFutures.put(dateStr, pool.submit(() -> apiClient.getAvailableSlots(barberId, day)));
                }

                // Collect results
                List<AppointmentDTO> appointments = appointmentsFuture.get(15, TimeUnit.SECONDS);

                Map<String, List<String>> slotsMap = new LinkedHashMap<>();
                for (var entry : slotFutures.entrySet()) {
                    try {
                        slotsMap.put(entry.getKey(), entry.getValue().get(15, TimeUnit.SECONDS));
                    } catch (Exception e) {
                        slotsMap.put(entry.getKey(), Collections.emptyList());
                        logger.debug("{} No slots for {}: {}", PREFIX, entry.getKey(), e.getMessage());
                    }
                }

                pool.shutdown();

                Platform.runLater(() -> {
                    allAppointments = appointments;
                    availableSlotsCache.putAll(slotsMap);
                    buildGrid();
                    hideLoading();
                });
            } catch (Exception e) {
                logger.error("{} Failed to load week data: {}", PREFIX, e.getMessage());
                Platform.runLater(() -> {
                    hideLoading();
                    showStatus("Error al cargar datos: " + e.getMessage());
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // ============================================================
    // GRID BUILDING
    // ============================================================

    private void buildGrid() {
        scheduleGrid.getChildren().clear();
        scheduleGrid.getColumnConstraints().clear();
        scheduleGrid.getRowConstraints().clear();

        BarberDTO barber = barberCombo.getValue();
        if (barber == null) return;

        Locale es = new Locale("es");

        // Filter appointments for selected barber & week
        List<AppointmentDTO> weekAppointments = allAppointments.stream()
                .filter(a -> a.getBarberId() == barber.getId())
                .filter(a -> {
                    LocalDate d = LocalDate.parse(a.getDateOnly());
                    return !d.isBefore(weekStart) && !d.isAfter(weekStart.plusDays(6));
                })
                .collect(Collectors.toList());

        // Collect all time slots (available + booked) to determine rows
        Set<String> allTimes = new TreeSet<>();
        for (int i = 0; i < 7; i++) {
            String dateStr = weekStart.plusDays(i).toString();
            List<String> available = availableSlotsCache.getOrDefault(dateStr, Collections.emptyList());
            allTimes.addAll(available);
        }
        for (AppointmentDTO a : weekAppointments) {
            allTimes.add(a.getFormattedTime());
        }

        if (allTimes.isEmpty()) {
            showStatus("Sin horarios disponibles para esta semana.");
            updateStats(weekAppointments);
            return;
        }

        List<String> timeSlots = new ArrayList<>(allTimes);

        // Column constraints: time label + 7 days
        ColumnConstraints timeCol = new ColumnConstraints();
        timeCol.setMinWidth(60);
        timeCol.setPrefWidth(65);
        timeCol.setMaxWidth(70);
        scheduleGrid.getColumnConstraints().add(timeCol);

        for (int i = 0; i < 7; i++) {
            ColumnConstraints dayCol = new ColumnConstraints();
            dayCol.setHgrow(Priority.ALWAYS);
            dayCol.setFillWidth(true);
            scheduleGrid.getColumnConstraints().add(dayCol);
        }

        // Header row
        RowConstraints headerRow = new RowConstraints();
        headerRow.setMinHeight(48);
        headerRow.setPrefHeight(48);
        scheduleGrid.getRowConstraints().add(headerRow);

        // Empty top-left corner
        Label corner = new Label("Hora");
        corner.getStyleClass().add("schedule-header-time");
        corner.setMaxWidth(Double.MAX_VALUE);
        corner.setMaxHeight(Double.MAX_VALUE);
        corner.setAlignment(Pos.CENTER);
        scheduleGrid.add(corner, 0, 0);

        // Day headers
        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            String dayName = capitalize(day.getDayOfWeek().getDisplayName(TextStyle.SHORT, es));
            String dayNum = String.valueOf(day.getDayOfMonth());

            VBox header = new VBox(1);
            header.setAlignment(Pos.CENTER);
            header.getStyleClass().add("schedule-day-header");
            header.setMaxWidth(Double.MAX_VALUE);
            header.setMaxHeight(Double.MAX_VALUE);

            if (day.equals(LocalDate.now())) {
                header.getStyleClass().add("schedule-day-today");
            }

            Label nameLabel = new Label(dayName);
            nameLabel.getStyleClass().add("schedule-day-name");
            Label numLabel = new Label(dayNum);
            numLabel.getStyleClass().add("schedule-day-num");

            header.getChildren().addAll(nameLabel, numLabel);
            scheduleGrid.add(header, i + 1, 0);
        }

        // Time rows
        for (int row = 0; row < timeSlots.size(); row++) {
            RowConstraints rc = new RowConstraints();
            rc.setMinHeight(62);
            rc.setPrefHeight(68);
            scheduleGrid.getRowConstraints().add(rc);

            String time = timeSlots.get(row);
            int gridRow = row + 1;

            // Time label
            Label timeLabel = new Label(time);
            timeLabel.getStyleClass().add("schedule-time-label");
            timeLabel.setMaxWidth(Double.MAX_VALUE);
            timeLabel.setMaxHeight(Double.MAX_VALUE);
            timeLabel.setAlignment(Pos.CENTER);
            scheduleGrid.add(timeLabel, 0, gridRow);

            // Day cells
            for (int dayIdx = 0; dayIdx < 7; dayIdx++) {
                LocalDate day = weekStart.plusDays(dayIdx);
                String dateStr = day.toString();

                List<String> available = availableSlotsCache.getOrDefault(dateStr, Collections.emptyList());

                // Find appointment at this time
                AppointmentDTO appointment = findAppointment(weekAppointments, dateStr, time);

                VBox cell = createCell(day, time, available.contains(time), appointment);
                cell.setMaxWidth(Double.MAX_VALUE);
                cell.setMaxHeight(Double.MAX_VALUE);
                GridPane.setFillWidth(cell, true);
                GridPane.setFillHeight(cell, true);
                scheduleGrid.add(cell, dayIdx + 1, gridRow);
            }
        }

        updateStats(weekAppointments);
    }

    private VBox createCell(LocalDate date, String time, boolean isAvailable, AppointmentDTO appointment) {
        VBox cell = new VBox(2);
        cell.setAlignment(Pos.CENTER);
        cell.getStyleClass().add("schedule-cell");

        if (appointment != null) {
            String status = appointment.getStatus();
            cell.getStyleClass().add("schedule-cell-" + status);

            Label clientLabel = new Label(appointment.getClientName());
            clientLabel.getStyleClass().add("schedule-cell-client");
            clientLabel.setMaxWidth(Double.MAX_VALUE);
            clientLabel.setAlignment(Pos.CENTER);

            Label statusLabel = new Label(appointment.getStatusDisplay());
            statusLabel.getStyleClass().add("schedule-cell-status");
            statusLabel.setMaxWidth(Double.MAX_VALUE);
            statusLabel.setAlignment(Pos.CENTER);

            cell.getChildren().addAll(clientLabel, statusLabel);

            cell.setOnMouseClicked(e -> openDetailModal(appointment));
            cell.setCursor(javafx.scene.Cursor.HAND);

        } else if (isAvailable) {
            cell.getStyleClass().add("schedule-cell-available");

            Label label = new Label("Disponible");
            label.getStyleClass().add("schedule-cell-available-text");
            label.setMaxWidth(Double.MAX_VALUE);
            label.setAlignment(Pos.CENTER);
            cell.getChildren().add(label);

            cell.setOnMouseClicked(e -> openCreateModal(date, time));
            cell.setCursor(javafx.scene.Cursor.HAND);

        } else {
            // Closed / not available
            cell.getStyleClass().add("schedule-cell-closed");
        }

        return cell;
    }

    private AppointmentDTO findAppointment(List<AppointmentDTO> appointments, String dateStr, String time) {
        return appointments.stream()
                .filter(a -> a.getDateOnly().equals(dateStr) && a.getFormattedTime().equals(time))
                .filter(a -> !"cancelled".equals(a.getStatus()))
                .findFirst()
                .orElse(null);
    }

    // ============================================================
    // STATS
    // ============================================================

    private void updateStats(List<AppointmentDTO> weekAppointments) {
        // Exclude cancelled from total count for meaningful stats
        long active = weekAppointments.stream()
                .filter(a -> !"cancelled".equals(a.getStatus()))
                .count();
        long pending = weekAppointments.stream().filter(a -> "pending".equals(a.getStatus())).count();
        long confirmed = weekAppointments.stream().filter(a -> "confirmed".equals(a.getStatus())).count();

        statTotal.setText(String.valueOf(active));
        statPending.setText(String.valueOf(pending));
        statConfirmed.setText(String.valueOf(confirmed));
    }

    // ============================================================
    // MODALS
    // ============================================================

    private void openCreateModal(LocalDate date, String time) {
        BarberDTO barber = barberCombo.getValue();
        if (barber == null) return;

        try {
            Stage owner = (Stage) scheduleGrid.getScene().getWindow();
            Object controller = WindowManager.openModal(
                    WindowRequest.builder()
                            .fxml("/app/barbman/core/view/appointment-modal.fxml")
                            .title("Nueva Reserva")
                            .owner(owner)
                            .modal(true)
                            .css("/app/barbman/core/style/appointment-modal.css")
                            .returnController(true)
                            .build()
            );

            if (controller instanceof AppointmentModalController modalCtrl) {
                modalCtrl.setCreateModePrefilled(apiClient, barber, date, time);
                modalCtrl.setOnSaved(this::loadWeekData);
            }
        } catch (Exception e) {
            logger.error("{} Failed to open create modal: {}", PREFIX, e.getMessage());
        }
    }

    private void openDetailModal(AppointmentDTO appointment) {
        try {
            Stage owner = (Stage) scheduleGrid.getScene().getWindow();
            Object controller = WindowManager.openModal(
                    WindowRequest.builder()
                            .fxml("/app/barbman/core/view/appointment-modal.fxml")
                            .title("Detalle de Reserva")
                            .owner(owner)
                            .modal(true)
                            .css("/app/barbman/core/style/appointment-modal.css")
                            .returnController(true)
                            .build()
            );

            if (controller instanceof AppointmentModalController modalCtrl) {
                modalCtrl.setViewMode(appointment, apiClient);
                modalCtrl.setOnSaved(this::loadWeekData);
            }
        } catch (Exception e) {
            logger.error("{} Failed to open detail modal: {}", PREFIX, e.getMessage());
        }
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private void showLoading(String message) {
        loadingLabel.setText(message);
        loadingPane.setVisible(true);
        loadingPane.setManaged(true);
        gridScroll.setVisible(false);
        gridScroll.setManaged(false);
        hideStatus();
    }

    private void hideLoading() {
        loadingPane.setVisible(false);
        loadingPane.setManaged(false);
        gridScroll.setVisible(true);
        gridScroll.setManaged(true);
    }

    private void showStatus(String message) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
    }

    private void hideStatus() {
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
