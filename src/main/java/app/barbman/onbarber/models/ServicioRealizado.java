package app.barbman.onbarber.models;

import java.util.Date;

public class ServicioRealizado {
    int id;
    int barbero_id;
    int tipo_servicio;
    int precio;
    Date fecha;
    String forma_pago;
    String observaciones;

    public ServicioRealizado(){}
    public ServicioRealizado(int id, int barbero_id, int tipo_servicio, int precio,
                             Date fecha, String forma_pago, String observaciones){
        this.id=id;
        this.barbero_id=barbero_id;
        this.tipo_servicio=tipo_servicio;
        this.precio=precio;
        this.fecha=fecha;
        this.forma_pago=forma_pago;
        this.observaciones=observaciones;
    }
    public ServicioRealizado(int barbero_id, int tipo_servicio, int precio,
                             Date fecha, String forma_pago, String observaciones){
        this.barbero_id=barbero_id;
        this.tipo_servicio=tipo_servicio;
        this.precio=precio;
        this.fecha=fecha;
        this.forma_pago=forma_pago;
        this.observaciones=observaciones;
    }


    // Getters
    public int getId(){return id;}
    public int getBarberoId(){return barbero_id;}
    public int getTipoServicio(){return tipo_servicio;}
    public int getPrecio(){return precio;}
    public Date getFecha(){return fecha;
    }

    // Setters
    public void setId(int id) {this.id = id;}
    public void setBarbero_id(int barbero_id) {this.barbero_id = barbero_id;}
    public void setTipo_servicio(int tipo_servicio) {this.tipo_servicio = tipo_servicio;}
    public void setPrecio(int precio) {this.precio = precio;}
    public void setFecha(Date fecha) {this.fecha = fecha;}
    public void setForma_pago(String forma_pago) {this.forma_pago = forma_pago;}
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
}
