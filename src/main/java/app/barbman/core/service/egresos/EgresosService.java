package app.barbman.core.service.egresos;

import app.barbman.core.model.Egreso;
import app.barbman.core.repositories.egresos.EgresosRepository;

import java.time.LocalDate;

public class EgresosService {
    public static EgresosRepository egresosRepository;
    public EgresosService(EgresosRepository repo) {
        egresosRepository = repo;
    }

    public Egreso addEgreso(String tipo, double monto, String descripcion, String formaPago) {
        if (tipo == null || tipo.isBlank()) throw new IllegalArgumentException("Debe seleccionar un tipo de egreso.");
        if (formaPago == null || formaPago.isBlank()) throw new IllegalArgumentException("Debe seleccionar el metodo de pago.");
        if (monto <= 0) throw new IllegalArgumentException("El monto debe ser mayor a cero.");
        if (descripcion != null && descripcion.length() > 500) {
            throw new IllegalArgumentException("La descripción no debe superar los 500 caracteres.");
        }
        LocalDate hoy = LocalDate.now();

        Egreso egreso = new Egreso(descripcion, monto, hoy, tipo,formaPago);
        egresosRepository.save(egreso);
        return egreso;
    }
}
