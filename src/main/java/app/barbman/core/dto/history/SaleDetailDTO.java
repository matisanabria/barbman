package app.barbman.core.dto.history;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * DTO that represents the detail of a sale, including header and line items.
 */

public class SaleDetailDTO {
    private int saleId;

    // Header
    private LocalDate date;
    private String userName;
    private String clientName;
    private String paymentMethod;
    private double total;

    // Detail
    private List<ServiceDetailDTO> services;
    private List<ProductDetailDTO> products;

    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
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

    public List<ServiceDetailDTO> getServices() {
        return services;
    }

    public void setServices(List<ServiceDetailDTO> services) {
        this.services = services;
    }

    public List<ProductDetailDTO> getProducts() {
        return products;
    }

    public void setProducts(List<ProductDetailDTO> products) {
        this.products = products;
    }

    private List<SaleItemDTO> serviceItems = new ArrayList<>();
    private List<SaleItemDTO> productItems = new ArrayList<>();

    public List<SaleItemDTO> getServiceItems() {
        return serviceItems;
    }

    public void setServiceItems(List<SaleItemDTO> serviceItems) {
        this.serviceItems = serviceItems;
    }

    public List<SaleItemDTO> getProductItems() {
        return productItems;
    }

    public void setProductItems(List<SaleItemDTO> productItems) {
        this.productItems = productItems;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SaleDetailDTO that = (SaleDetailDTO) o;
        return saleId == that.saleId && Double.compare(total, that.total) == 0 && Objects.equals(date, that.date) && Objects.equals(userName, that.userName) && Objects.equals(clientName, that.clientName) && Objects.equals(paymentMethod, that.paymentMethod) && Objects.equals(services, that.services) && Objects.equals(products, that.products);
    }

    @Override
    public int hashCode() {
        return Objects.hash(saleId, date, userName, clientName, paymentMethod, total, services, products);
    }
}
