package app.barbman.onbarber.service.servicios;

import app.barbman.onbarber.model.ServicioRealizado;

import java.util.Date;

public class ServiciosManager {
    public void realizarServicio(int barberoId, int tipoServicio, int precio, String formaPago, String observaciones) {
        // Aquí se implementaría la lógica para realizar un servicio
        // Por ejemplo, crear una instancia de ServicioRealizado y guardarla en la base de datos
        ServicioRealizado servicio = new ServicioRealizado(barberoId, tipoServicio, precio, new Date(), formaPago, observaciones);
        
        // Lógica para guardar el servicio en la base de datos
        // db.save(servicio);
    }
    public void eliminarServicio(int servicioId) {
        // Aquí se implementaría la lógica para eliminar un servicio
        // Por ejemplo, buscar el servicio por ID y eliminarlo de la base de datos
        // ServicioRealizado servicio = db.findById(servicioId);
        // db.delete(servicio);
    }

    
}
