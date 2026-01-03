package app.barbman.core.dto;

import java.util.List;

public class SaleSummaryDTO {
    private int saleId;

    private String clientName;
    private String clientDocument;

    private String paymentMethod;

    private double total;

    private List<SaleItemSummaryDTO> items;

    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientDocument() {
        return clientDocument;
    }

    public void setClientDocument(String clientDocument) {
        this.clientDocument = clientDocument;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public List<SaleItemSummaryDTO> getItems() {
        return items;
    }

    public void setItems(List<SaleItemSummaryDTO> items) {
        this.items = items;
    }
}