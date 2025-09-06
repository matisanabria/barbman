package app.barbman.core.model;

import java.time.LocalDate;
import java.util.Objects;

public class Egreso {
    private int id;
    private String descripcion;
    private double monto;
    private LocalDate fecha;
    private String tipo;
    private String formaPago;  // efectivo, transferencia

    /**
    * Tipos de egreso:
     * "insumo": Compra de insumos o materiales.
     * "servicio": Pago de servicios externos (agua, luz, internet, etc.).
     * "sueldo": Pago de sueldos o salarios.
     * "adelanto": Anticipos o pagos adelantados.
     * "otros": Cualquier otro tipo de egreso no categorizado.
     */

    public Egreso() { }
    public Egreso(int id) {
        this.id = id;
    }
    public Egreso(String descripcion, double monto, LocalDate fecha, String tipo, String formaPago) {
        this.descripcion = descripcion;
        this.monto = monto;
        this.fecha = fecha;
        this.tipo = tipo;
        this.formaPago = formaPago;
    }
    public Egreso(int id, String descripcion, double monto, LocalDate fecha, String tipo, String formaPago) {
        this(descripcion, monto, fecha, tipo, formaPago);
        this.id = id;
    }

    // Getters
    public int getId() { return id; }
    public String getDescripcion() { return descripcion; }
    public double getMonto() { return monto; }
    public LocalDate getFecha() { return fecha; }
    public String getTipo() { return tipo; }
    public String getFormaPago() { return formaPago;}

    // Setters
    public void setId(int id) { this.id = id; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setMonto(double monto) { this.monto = monto; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setFormaPago(String formaPago) { this.formaPago = formaPago; }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Egreso egreso = (Egreso) o;
        return id == egreso.id && Double.compare(monto, egreso.monto) == 0 && Objects.equals(descripcion, egreso.descripcion) && Objects.equals(fecha, egreso.fecha) && Objects.equals(tipo, egreso.tipo) && Objects.equals(formaPago, egreso.formaPago);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, descripcion, monto, fecha, tipo, formaPago);
    }

    @Override
    public String toString() {
        return "Egreso{" +
                "id=" + id +
                ", descripcion='" + descripcion + '\'' +
                ", monto=" + monto +
                ", fecha=" + fecha +
                ", tipo='" + tipo + '\'' +
                ", formaPago='" + formaPago + '\'' +
                '}';
    }
}

