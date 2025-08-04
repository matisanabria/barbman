package app.barbman.onbarber.model;

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
}
