package app.barbman.core.service.sales;

import app.barbman.core.dto.SaleItemSummaryDTO;
import app.barbman.core.dto.SaleSummaryDTO;
import app.barbman.core.model.PaymentMethod;
import app.barbman.core.model.human.Client;
import app.barbman.core.model.sales.Sale;
import app.barbman.core.model.sales.products.Product;
import app.barbman.core.model.sales.products.ProductHeader;
import app.barbman.core.model.sales.products.ProductSaleItem;
import app.barbman.core.model.sales.services.ServiceDefinition;
import app.barbman.core.model.sales.services.ServiceHeader;
import app.barbman.core.model.sales.services.ServiceItem;

import app.barbman.core.repositories.sales.*;
import app.barbman.core.repositories.sales.services.serviceheader.*;
import app.barbman.core.repositories.sales.services.serviceitems.*;
import app.barbman.core.repositories.sales.services.servicedefinition.*;
import app.barbman.core.repositories.sales.products.productheader.*;
import app.barbman.core.repositories.sales.products.productsaleitem.*;
import app.barbman.core.repositories.sales.products.product.*;

import app.barbman.core.repositories.client.*;
import app.barbman.core.repositories.paymentmethod.*;

import app.barbman.core.service.clients.ClientService;
import app.barbman.core.service.paymentmethods.PaymentMethodsService;

import java.util.ArrayList;
import java.util.List;

public class SaleQueryService {

    // =====================
    // HARDCODED DEPENDENCIES
    // =====================
    private final SaleRepository saleRepo = new SaleRepositoryImpl();

    private final ServiceHeaderRepository serviceHeaderRepo =
            new ServiceHeaderRepositoryImpl();
    private final ServiceItemRepository serviceItemRepo =
            new ServiceItemRepositoryImpl();
    private final ServiceDefinitionRepository serviceDefRepo =
            new ServiceDefinitionRepositoryImpl();

    private final ProductHeaderRepository productHeaderRepo =
            new ProductHeaderRepositoryImpl();
    private final ProductSaleItemRepository productItemRepo =
            new ProductSaleItemRepositoryImpl();
    private final ProductRepository productRepo =
            new ProductRepositoryImpl();

    private final ClientService clientService =
            new ClientService(new ClientRepositoryImpl());

    private final PaymentMethodsService paymentService =
            new PaymentMethodsService(new PaymentMethodRepositoryImpl());

    // =====================
    // MAIN API
    // =====================
    public SaleSummaryDTO getSaleSummary(int saleId) {

        Sale sale = saleRepo.findById(saleId);
        if (sale == null) {
            throw new IllegalStateException("Sale not found: " + saleId);
        }

        SaleSummaryDTO dto = new SaleSummaryDTO();
        dto.setSaleId(sale.getId());
        dto.setTotal(sale.getTotal());

        // =====================
        // CLIENT
        // =====================
        if (sale.getClientId() != null) {
            Client c = clientService.findById(sale.getClientId());
            if (c != null) {
                dto.setClientName(c.getName());
                dto.setClientDocument(c.getDocument());
            } else {
                dto.setClientName("Cliente eliminado");
                dto.setClientDocument("-");
            }
        } else {
            dto.setClientName("Cliente casual");
            dto.setClientDocument("-");
        }

        // =====================
        // PAYMENT METHOD
        // =====================
        PaymentMethod pm =
                paymentService.getPaymentMethodById(
                        sale.getPaymentMethodId()
                );

        dto.setPaymentMethod(
                pm != null ? pm.getName() : "Desconocido"
        );

        // =====================
        // ITEMS
        // =====================
        List<SaleItemSummaryDTO> items = new ArrayList<>();

        // -------- SERVICES --------
        for (ServiceHeader sh : serviceHeaderRepo.findAll()) {

            if (sh.getSaleId() != saleId) continue;

            for (ServiceItem si : serviceItemRepo.findByServiceId(sh.getId())) {

                ServiceDefinition def =
                        serviceDefRepo.findById(
                                si.getServiceDefinitionId()
                        );

                SaleItemSummaryDTO item = new SaleItemSummaryDTO();
                item.setName(def != null ? def.getName() : "Servicio eliminado");
                item.setQuantity(si.getQuantity());
                item.setUnitPrice(si.getUnitPrice());
                item.setSubtotal(si.getItemTotal());

                items.add(item);
            }
        }

        // -------- PRODUCTS --------
        for (ProductHeader ph : productHeaderRepo.findAll()) {

            if (ph.getSaleId() != saleId) continue;

            for (ProductSaleItem pi :
                    productItemRepo.findBySaleId(ph.getId())) {

                Product p =
                        productRepo.findById(pi.getProductId());

                SaleItemSummaryDTO item = new SaleItemSummaryDTO();
                item.setName(p != null ? p.getName() : "Producto eliminado");
                item.setQuantity(pi.getQuantity());
                item.setUnitPrice(pi.getUnitPrice());
                item.setSubtotal(pi.getItemTotal());

                items.add(item);
            }
        }

        dto.setItems(items);
        return dto;
    }
}
