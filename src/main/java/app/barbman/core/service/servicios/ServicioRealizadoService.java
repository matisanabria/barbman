package app.barbman.core.service.servicios;

import app.barbman.core.model.ServicioRealizado;
import app.barbman.core.repositories.serviciorealizado.ServicioRealizadoRepository;

import java.time.LocalDate;

public class ServicioRealizadoService {
    private final ServicioRealizadoRepository servicioRealizadoRepository;

    public ServicioRealizadoService(ServicioRealizadoRepository repo) {
        this.servicioRealizadoRepository = repo;
    }

    public ServicioRealizado addServicioRealizado(int barberoId, int tipoServicio, double precio, String formaPago, String observaciones) {
        if (precio <= 0) throw new IllegalArgumentException("Precio debe ser > 0");
        if (formaPago == null || formaPago.isBlank()) throw new IllegalArgumentException("Forma de pago requerida");
        if (observaciones != null && observaciones.length() > 500) {
            throw new IllegalArgumentException("Observaciones demasiado largas");
        }
        LocalDate hoy = LocalDate.now();
        ServicioRealizado sr = new ServicioRealizado(barberoId, tipoServicio, precio, hoy, formaPago, observaciones);

        servicioRealizadoRepository.save(sr);
        return sr;
    }
    public void eliminarServicio(int servicioId) {
        // Aquí se implementaría la lógica para eliminar un servicio
        // Por ejemplo, buscar el servicio por ID y eliminarlo de la base de datos
        // ServicioRealizado servicio = db.findById(servicioId);
        // db.delete(servicio);
    }

    
}
