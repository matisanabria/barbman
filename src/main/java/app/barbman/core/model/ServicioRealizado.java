package app.barbman.core.model;

import java.time.LocalDate;
import java.util.Objects;

public class ServicioRealizado {
    private int id;
    private int barbero_id;
    private int tipo_servicio;
    private double precio;
    private LocalDate fecha;
    private String forma_pago;
    private String observaciones;

    public ServicioRealizado(){}
    public ServicioRealizado(int id){
        this.id = id;
    }

    public ServicioRealizado(int barbero_id, int tipo_servicio, double precio,
                             LocalDate fecha, String forma_pago, String observaciones){
        this.barbero_id=barbero_id;
        this.tipo_servicio=tipo_servicio;
        this.precio=precio;
        this.fecha=fecha;
        this.forma_pago=forma_pago;
        this.observaciones=observaciones;
    }

    public ServicioRealizado(int id, int barbero_id, int tipo_servicio, double precio,
                             LocalDate fecha, String forma_pago, String observaciones){
        this(barbero_id, tipo_servicio, precio, fecha, forma_pago, observaciones);
        this.id=id;
    }

    // Getters
    public int getId(){return id;}
    public int getBarberoId(){return barbero_id;}
    public int getTipoServicio(){return tipo_servicio;}
    public double getPrecio(){return precio;}
    public LocalDate getFecha(){return fecha;}
    public String getFormaPago(){return forma_pago;}
    public String getObservaciones(){return observaciones;}

    // Setters
    public void setId(int id) {this.id = id;}
    public void setBarberoId(int barbero_id) {this.barbero_id = barbero_id;}
    public void setTipoServicio(int tipo_servicio) {this.tipo_servicio = tipo_servicio;}
    public void setPrecio(double precio) {this.precio = precio;}
    public void setFecha(LocalDate fecha) {this.fecha = fecha;}
    public void setFormaPago(String forma_pago) {this.forma_pago = forma_pago;}
    public void setObservaciones(String observaciones) {this.observaciones = observaciones;}

    @Override
    public String toString() {
        return "ServiciosRealizados{" +
                "id=" + id +
                ", barbero_id=" + barbero_id +
                ", tipo_servicio=" + tipo_servicio +
                ", precio=" + precio +
                ", fecha=" + fecha +
                ", forma_pago='" + forma_pago + '\'' +
                ", observaciones='" + observaciones + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ServicioRealizado that = (ServicioRealizado) o;
        return id == that.id && barbero_id == that.barbero_id && tipo_servicio == that.tipo_servicio && precio == that.precio && Objects.equals(fecha, that.fecha) && Objects.equals(forma_pago, that.forma_pago) && Objects.equals(observaciones, that.observaciones);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, barbero_id, tipo_servicio, precio, fecha, forma_pago, observaciones);
    }
}
