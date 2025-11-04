package app.barbman.core.dto.services;

public class ServiceHistoryDTO {
    private final String userName;
    private final String serviceNames;
    private final String paymentMethod;
    private final String totalFormatted;
    private final String date;
    private final String notes;

    public ServiceHistoryDTO(String userName, String serviceNames, String paymentMethod,
                             String totalFormatted, String date, String notes) {
        this.userName = userName;
        this.serviceNames = serviceNames;
        this.paymentMethod = paymentMethod;
        this.totalFormatted = totalFormatted;
        this.date = date;
        this.notes = notes != null && !notes.isBlank() ? notes : "-";
    }

    public String getUserName() { return userName; }
    public String getServiceNames() { return serviceNames; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getTotalFormatted() { return totalFormatted; }
    public String getDate() { return date; }
    public String getNotes() { return notes; }
}
