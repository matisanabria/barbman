package app.barbman.core.service.servicios;

import app.barbman.core.model.PerformedService;
import app.barbman.core.repositories.performedservice.PerformedServiceRepository;

import java.time.LocalDate;
import java.util.List;

public class ServicioRealizadoService {
    private final PerformedServiceRepository performedServiceRepository;
    private static final List<String> FORMAS_VALIDAS = List.of("efectivo", "transferencia", "pos");


    public ServicioRealizadoService(PerformedServiceRepository repo) {
        this.performedServiceRepository = repo;
    }

    public PerformedService addServicioRealizado(int barberoId, int tipoServicio, double precio, int formaPago, String observaciones) {
        if (barberoId <= 0) throw new IllegalArgumentException("Debe seleccionar un barbero.");
        if (tipoServicio <= 0) throw new IllegalArgumentException("Debe seleccionar un tipo de servicio.");
        if (precio <= 0) throw new IllegalArgumentException("El precio debe ser mayor a cero.");
        if (formaPago == null || formaPago.isBlank()) throw new IllegalArgumentException("Debe seleccionar una forma de pago.");

        if (!FORMAS_VALIDAS.contains(formaPago.toLowerCase())) {
            throw new IllegalArgumentException("Forma de pago inválida.");
        }

        if (observaciones != null && observaciones.length() > 500) {
            throw new IllegalArgumentException("Las observaciones no deben superar los 500 caracteres.");
        }


        LocalDate hoy = LocalDate.now();
        PerformedService sr = new PerformedService(barberoId, tipoServicio, precio, hoy, formaPago, observaciones);
        performedServiceRepository.save(sr);
        return sr;
    }
    public void eliminarServicio(int servicioId) {
        if (servicioId <= 0) {
            throw new IllegalArgumentException("ID de servicio inválido.");
        }
        performedServiceRepository.delete(servicioId);
    }

    
}
