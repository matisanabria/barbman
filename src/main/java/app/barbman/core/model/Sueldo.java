package app.barbman.core.model;

import java.time.LocalDate;
import java.util.Objects;

public class Sueldo {
    private int id;
    private int barberoId;
    private double produccionTotal;
    private double montoLiquidado;
    private String tipoCobroSnapshot;
    private LocalDate fechaPago;
    private String formaPago;

    public Sueldo() { }
    public Sueldo(int id) {
        this.id = id;
    }
    public Sueldo(int barberoId, double produccionTotal, double montoLiquidado, String tipoCobroSnapshot, LocalDate fechaPago, String formaPago) {
        this.barberoId = barberoId;
        this.produccionTotal = produccionTotal;
        this.montoLiquidado = montoLiquidado;
        this.tipoCobroSnapshot = tipoCobroSnapshot;
        this.fechaPago = fechaPago;
        this.formaPago = formaPago;
    }

    // Getters
    public int getId() { return id; }
    public int getBarberoId() { return barberoId; }
    public double getProduccionTotal() { return produccionTotal; }
    public double getMontoLiquidado() { return montoLiquidado; }
    public String getTipoCobroSnapshot() { return tipoCobroSnapshot; }
    public LocalDate getFechaPago() { return fechaPago; }
    public String getFormaPago() { return formaPago;}

    // Setters
    public void setId(int id) { this.id = id; }
    public void setBarberoId(int barberoId) { this.barberoId = barberoId; }
    public void setProduccionTotal(double produccionTotal) { this.produccionTotal = produccionTotal; }
    public void setMontoLiquidado(double montoLiquidado) { this.montoLiquidado = montoLiquidado; }
    public void setTipoCobroSnapshot(String tipoCobroSnapshot) { this.tipoCobroSnapshot = tipoCobroSnapshot; }
    public void setFechaPago(LocalDate fechaPago) { this.fechaPago = fechaPago; }
    public void setFormaPago(String formaPago) { this.formaPago = formaPago;}

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Sueldo sueldo = (Sueldo) o;
        return id == sueldo.id && barberoId == sueldo.barberoId && Double.compare(produccionTotal, sueldo.produccionTotal) == 0 && Double.compare(montoLiquidado, sueldo.montoLiquidado) == 0 && Objects.equals(tipoCobroSnapshot, sueldo.tipoCobroSnapshot) && Objects.equals(fechaPago, sueldo.fechaPago) && Objects.equals(formaPago, sueldo.formaPago);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, barberoId, produccionTotal, montoLiquidado, tipoCobroSnapshot, fechaPago, formaPago);
    }

    @Override
    public String toString() {
        return "Sueldo{" +
                "id=" + id +
                ", barberoId=" + barberoId +
                ", produccionTotal=" + produccionTotal +
                ", montoLiquidado=" + montoLiquidado +
                ", tipoCobroSnapshot='" + tipoCobroSnapshot + '\'' +
                ", fechaPago=" + fechaPago +
                ", formaPago='" + formaPago + '\'' +
                '}';
    }
}
