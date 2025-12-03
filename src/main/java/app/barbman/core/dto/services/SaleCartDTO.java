package app.barbman.core.dto.services;

import app.barbman.core.model.services.ServiceItem;
import app.barbman.core.model.services.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SaleCartDTO {

    private static final Logger logger = LogManager.getLogger(SaleCartDTO.class);
    private static final String PREFIX = "[CART-DTO]";

    private final Service service;            // contiene user_id, date, payment_method_id, total, notes
    private final List<CartItem> cartItems;   // items con quantity y subtotales

    public SaleCartDTO(int userId) {
        this.service = new Service(
                userId,
                LocalDate.now(), // date
                0,               // payment_method (se asigna en pantalla de pago)
                0,               // total
                null             // notes
        );
        this.cartItems = new ArrayList<>();
    }

    // -----------------------
    // AGREGAR ÍTEM
    // -----------------------
    public void addItem(int serviceTypeId, String serviceName, double price) {
        CartItem existing = cartItems.stream()
                .filter(i -> i.getTypeId() == serviceTypeId && i.getPrice() == price)
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + 1);
            logger.info("{} Quantity +1 for {}", PREFIX, serviceName);
        } else {
            cartItems.add(new CartItem(serviceTypeId, serviceName, price, 1));
            logger.info("{} Item added -> {}", PREFIX, serviceName);
        }

        recalculateTotal();
    }

    // -----------------------
    // REMOVER 1 UNIDAD
    // -----------------------
    public void removeSingleUnit(CartItem item) {
        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
        } else {
            cartItems.remove(item);
        }
        recalculateTotal();
    }

    // -----------------------
    // ELIMINAR ÍTEM COMPLETO
    // -----------------------
    public void removeItem(CartItem item) {
        cartItems.remove(item);
        recalculateTotal();
    }

    // -----------------------
    // TOTAL
    // -----------------------
    public void recalculateTotal() {
        double total = cartItems.stream()
                .mapToDouble(CartItem::getSubtotal)
                .sum();
        service.setTotal(total);

        logger.debug("{} Total recalculated -> {} Gs", PREFIX, total);
    }

    // -----------------------
    // ASIGNAR MÉTODO DE PAGO
    // -----------------------
    public void setPaymentMethod(int paymentMethodId) {
        service.setPaymentMethodId(paymentMethodId);
    }

    // -----------------------
    // ASIGNAR NOTAS
    // -----------------------
    public void setNotes(String notes) {
        service.setNotes(notes);
    }

    // -----------------------
    // OBTENER MODELO PARA GUARDAR
    // -----------------------
    public Service buildServiceModel() {
        return service;
    }

    public List<ServiceItem> buildItemModels() {
        List<ServiceItem> list = new ArrayList<>();
        for (CartItem ci : cartItems) {
            for (int i = 0; i < ci.getQuantity(); i++) {
                list.add(new ServiceItem(0, ci.getTypeId(), ci.getPrice()));
            }
        }
        return list;
    }

    // -----------------------
    // GETTERS
    // -----------------------
    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public double getTotal() {
        return service.getTotal();
    }

    public Service getService() {
        return service;
    }
}
