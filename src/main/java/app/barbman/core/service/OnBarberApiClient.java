package app.barbman.core.service;

import app.barbman.core.infrastructure.EnvConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OnBarberApiClient {

    private static final Logger logger = LogManager.getLogger(OnBarberApiClient.class);
    private static final String PREFIX = "[ONBARBER-API]";

    private final HttpClient httpClient;
    private final ObjectMapper mapper;
    private final String baseUrl;
    private final String token;

    public OnBarberApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.mapper = new ObjectMapper();
        String raw = EnvConfig.get("ONBARBER_API_URL", "http://localhost:8000/api");
        // Normalize: remove trailing slash
        this.baseUrl = raw.endsWith("/") ? raw.substring(0, raw.length() - 1) : raw;
        this.token = EnvConfig.get("ONBARBER_API_TOKEN", "");
    }

    // ============================================================
    // PUBLIC API
    // ============================================================

    /**
     * GET /barbers — list active barbers (public endpoint)
     */
    public List<BarberDTO> getBarbers() throws Exception {
        JsonNode root = get("/barbers");
        List<BarberDTO> barbers = new ArrayList<>();
        for (JsonNode node : root) {
            barbers.add(parseBarber(node));
        }
        return barbers;
    }

    /**
     * GET /barbers/{id}/slots?date=YYYY-MM-DD — available 1h slots (public endpoint)
     */
    public List<String> getAvailableSlots(int barberId, LocalDate date) throws Exception {
        JsonNode root = get("/barbers/" + barberId + "/slots?date=" + date);
        List<String> slots = new ArrayList<>();
        JsonNode slotsNode = root.get("slots");
        if (slotsNode != null && slotsNode.isArray()) {
            for (JsonNode s : slotsNode) {
                if (s.isObject()) {
                    boolean available = s.has("available") && s.get("available").asBoolean(false);
                    if (available && s.has("time")) {
                        slots.add(s.get("time").asText());
                    }
                } else {
                    // Fallback: plain string format
                    slots.add(s.asText());
                }
            }
        }
        return slots;
    }

    /**
     * GET /appointments — list all appointments (protected)
     */
    public List<AppointmentDTO> getAppointments() throws Exception {
        JsonNode root = getProtected("/appointments");
        List<AppointmentDTO> appointments = new ArrayList<>();
        for (JsonNode node : root) {
            appointments.add(parseAppointment(node));
        }
        return appointments;
    }

    /**
     * POST /appointments — create appointment (public endpoint)
     */
    public AppointmentDTO createAppointment(int barberId, String clientName, String clientPhone,
                                             LocalDate date, String time) throws Exception {
        ObjectNode body = mapper.createObjectNode();
        body.put("barber_id", barberId);
        body.put("client_name", clientName);
        body.put("client_phone", clientPhone);
        body.put("appointment_date", date.toString());
        body.put("appointment_time", time);

        JsonNode result = post("/appointments", body);
        return parseAppointment(result);
    }

    /**
     * PATCH /appointments/{id} — update status (protected)
     */
    public AppointmentDTO updateAppointmentStatus(int appointmentId, String status) throws Exception {
        ObjectNode body = mapper.createObjectNode();
        body.put("status", status);

        JsonNode result = patch("/appointments/" + appointmentId, body);
        return parseAppointment(result);
    }

    /**
     * POST /schedule-overrides — close (or modify) a specific day for a barber (protected)
     */
    public void closeDayForBarber(int barberId, LocalDate date) throws Exception {
        ObjectNode body = mapper.createObjectNode();
        body.put("barber_id", barberId);
        body.put("date", date.toString());
        body.put("is_open", false);

        postProtected("/schedule-overrides", body);
    }

    /**
     * POST /schedule-overrides — reopen a previously closed day (protected)
     */
    public void reopenDayForBarber(int barberId, LocalDate date) throws Exception {
        // Fetch existing overrides to find the one for this date
        JsonNode overrides = getProtected("/barbers/" + barberId + "/overrides");
        for (JsonNode o : overrides) {
            String overrideDate = o.get("date").asText();
            // API returns "YYYY-MM-DDTHH:MM:SS.000000Z" or "YYYY-MM-DD"
            if (overrideDate.startsWith(date.toString())) {
                deleteProtected("/schedule-overrides/" + o.get("id").asInt());
                return;
            }
        }
    }

    /**
     * GET /barbers/{id}/overrides — list schedule overrides for a barber (protected)
     */
    public List<ClosedDayDTO> getClosedDays(int barberId) throws Exception {
        JsonNode root = getProtected("/barbers/" + barberId + "/overrides");
        List<ClosedDayDTO> days = new ArrayList<>();
        for (JsonNode node : root) {
            if (!node.get("is_open").asBoolean(true)) {
                String dateStr = node.get("date").asText();
                // Normalize: "2026-03-20T00:00:00.000000Z" → "2026-03-20"
                if (dateStr.length() > 10) dateStr = dateStr.substring(0, 10);
                days.add(new ClosedDayDTO(node.get("id").asInt(), barberId, dateStr));
            }
        }
        return days;
    }

    // ============================================================
    // HTTP METHODS
    // ============================================================

    private JsonNode get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .GET()
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(15))
                .build();

        return execute(request);
    }

    private JsonNode getProtected(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .GET()
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .timeout(Duration.ofSeconds(15))
                .build();

        return execute(request);
    }

    private JsonNode post(String path, ObjectNode body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(15))
                .build();

        return execute(request);
    }

    private JsonNode postProtected(String path, ObjectNode body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .timeout(Duration.ofSeconds(15))
                .build();

        return execute(request);
    }

    private JsonNode deleteProtected(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .DELETE()
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .timeout(Duration.ofSeconds(15))
                .build();

        return execute(request);
    }

    private JsonNode patch(String path, ObjectNode body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .method("PATCH", HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .timeout(Duration.ofSeconds(15))
                .build();

        return execute(request);
    }

    private JsonNode execute(HttpRequest request) throws Exception {
        logger.debug("{} {} {}", PREFIX, request.method(), request.uri());

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        logger.debug("{} Response: {} ({})", PREFIX, response.statusCode(), response.body().length());

        if (response.statusCode() >= 400) {
            String errorMsg = response.body();
            try {
                JsonNode errorJson = mapper.readTree(errorMsg);
                if (errorJson.has("message")) {
                    errorMsg = errorJson.get("message").asText();
                }
            } catch (Exception ignored) {}

            throw new ApiException(response.statusCode(), errorMsg);
        }

        return mapper.readTree(response.body());
    }

    // ============================================================
    // PARSING
    // ============================================================

    private BarberDTO parseBarber(JsonNode node) {
        return new BarberDTO(
                node.get("id").asInt(),
                node.get("name").asText(),
                node.has("phone") && !node.get("phone").isNull() ? node.get("phone").asText() : null,
                node.has("photo_url") && !node.get("photo_url").isNull() ? node.get("photo_url").asText() : null
        );
    }

    private AppointmentDTO parseAppointment(JsonNode node) {
        BarberDTO barber = null;
        if (node.has("barber") && !node.get("barber").isNull()) {
            barber = parseBarber(node.get("barber"));
        }

        return new AppointmentDTO(
                node.get("id").asInt(),
                node.get("barber_id").asInt(),
                node.get("client_name").asText(),
                node.get("client_phone").asText(),
                node.get("appointment_date").asText(),
                node.get("appointment_time").asText(),
                node.get("status").asText(),
                barber
        );
    }

    // ============================================================
    // INNER CLASSES
    // ============================================================

    public static class BarberDTO {
        private final int id;
        private final String name;
        private final String phone;
        private final String photoUrl;

        public BarberDTO(int id, String name, String phone, String photoUrl) {
            this.id = id;
            this.name = name;
            this.phone = phone;
            this.photoUrl = photoUrl;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getPhone() { return phone; }
        public String getPhotoUrl() { return photoUrl; }

        @Override
        public String toString() { return name; }
    }

    public static class AppointmentDTO {
        private final int id;
        private final int barberId;
        private final String clientName;
        private final String clientPhone;
        private final String appointmentDate;
        private final String appointmentTime;
        private final String status;
        private final BarberDTO barber;

        public AppointmentDTO(int id, int barberId, String clientName, String clientPhone,
                              String appointmentDate, String appointmentTime, String status,
                              BarberDTO barber) {
            this.id = id;
            this.barberId = barberId;
            this.clientName = clientName;
            this.clientPhone = clientPhone;
            this.appointmentDate = appointmentDate;
            this.appointmentTime = appointmentTime;
            this.status = status;
            this.barber = barber;
        }

        public int getId() { return id; }
        public int getBarberId() { return barberId; }
        public String getClientName() { return clientName; }
        public String getClientPhone() { return clientPhone; }
        public String getAppointmentDate() { return appointmentDate; }
        public String getAppointmentTime() { return appointmentTime; }
        public String getStatus() { return status; }
        public BarberDTO getBarber() { return barber; }

        public String getBarberName() {
            return barber != null ? barber.getName() : "—";
        }

        public String getFormattedTime() {
            // "HH:MM:SS" → "HH:MM"
            if (appointmentTime != null && appointmentTime.length() >= 5) {
                return appointmentTime.substring(0, 5);
            }
            return appointmentTime;
        }

        public String getDateOnly() {
            // "2026-03-14T00:00:00.000000Z" or "2026-03-14" → "2026-03-14"
            if (appointmentDate != null && appointmentDate.length() >= 10) {
                return appointmentDate.substring(0, 10);
            }
            return appointmentDate;
        }

        public String getFormattedDate() {
            String d = getDateOnly();
            if (d != null && d.length() == 10) {
                return d.substring(8) + "/" + d.substring(5, 7) + "/" + d.substring(0, 4);
            }
            return appointmentDate;
        }

        public String getStatusDisplay() {
            return switch (status) {
                case "pending" -> "Pendiente";
                case "confirmed" -> "Confirmada";
                case "cancelled" -> "Cancelada";
                case "completed" -> "Completada";
                default -> status;
            };
        }
    }

    public static class ClosedDayDTO {
        private final int id;
        private final int barberId;
        private final String date;

        public ClosedDayDTO(int id, int barberId, String date) {
            this.id = id;
            this.barberId = barberId;
            this.date = date;
        }

        public int getId() { return id; }
        public int getBarberId() { return barberId; }
        public String getDate() { return date; }

        public String getFormattedDate() {
            if (date != null && date.length() == 10) {
                return date.substring(8) + "/" + date.substring(5, 7) + "/" + date.substring(0, 4);
            }
            return date;
        }
    }

    public static class ApiException extends Exception {
        private final int statusCode;

        public ApiException(int statusCode, String message) {
            super(message);
            this.statusCode = statusCode;
        }

        public int getStatusCode() { return statusCode; }
    }
}
