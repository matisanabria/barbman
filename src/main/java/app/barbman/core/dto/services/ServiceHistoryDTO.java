package app.barbman.core.dto.services;

import java.time.LocalDate;

public class ServiceHistoryDTO {
    private final int id;
    private final String userName;
    private final String serviceNames;
    private final String paymentMethod;
    private final String totalFormatted;
    private final LocalDate date;
    private final String notes;

    public ServiceHistoryDTO(int id, String userName, String serviceNames, String paymentMethod,
                             String totalFormatted, LocalDate date, String notes) {
        this.id = id;
        this.userName = userName;
        this.serviceNames = serviceNames;
        this.paymentMethod = paymentMethod;
        this.totalFormatted = totalFormatted;
        this.date = date;
        this.notes = notes != null && !notes.isBlank() ? notes : "-";
    }

    public int getId() { return id; }
    public String getUserName() { return userName; }
    public String getServiceNames() { return serviceNames; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getTotalFormatted() { return totalFormatted; }
    public LocalDate getDate() { return date; }
    public String getNotes() { return notes; }
}
