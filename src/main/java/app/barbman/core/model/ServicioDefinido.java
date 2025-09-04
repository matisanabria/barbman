package app.barbman.core.model;

import java.util.Objects;

public class ServicioDefinido {
    int id;
    String nombre;
    double precioBase;

    public ServicioDefinido(){}
    public ServicioDefinido(String nombre){
        // Lo podemos usar para el autocompletado
        // a la hora de cargar servicios realizados
        this.nombre = nombre;
    }
    public ServicioDefinido(int id, String nombre){
        // Para guardar el servicio en database supongo.
        this(nombre);
        this.id = id;
    }
    public ServicioDefinido(int id, String nombre, double precioBase){
        // Lo podemos usar para la opcion
        // de agregar nuevos servicios a la lista,
        this(id, nombre);
        this.precioBase=precioBase;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPrecioBase() {
        return precioBase;
    }

    public void setPrecioBase(double precioBase) {
        this.precioBase = precioBase;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ServicioDefinido that = (ServicioDefinido) o;
        return id == that.id && Double.compare(precioBase, that.precioBase) == 0 && Objects.equals(nombre, that.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombre, precioBase);
    }
}
