package app.barbman.core.model;

import java.time.LocalDate;
import java.util.Objects;

public class CajaDiaria {
    private int id;
    private LocalDate fecha;
    private double ingresosTotal;
    private double egresosTotal;
    private double saldoFinal;
    private double efectivo;
    private double transferencia;
    private double pos;

    // --- Constructores ---
    public CajaDiaria() {}

    public CajaDiaria(int id, LocalDate fecha, double ingresosTotal, double egresosTotal,
                      double saldoFinal, double efectivo, double transferencia, double pos) {
        this.id = id;
        this.fecha = fecha;
        this.ingresosTotal = ingresosTotal;
        this.egresosTotal = egresosTotal;
        this.saldoFinal = saldoFinal;
        this.efectivo = efectivo;
        this.transferencia = transferencia;
        this.pos = pos;
    }

    public CajaDiaria(LocalDate fecha, double ingresosTotal, double egresosTotal,
                      double saldoFinal, double efectivo, double transferencia, double pos) {
        this(0, fecha, ingresosTotal, egresosTotal, saldoFinal, efectivo, transferencia, pos);
    }

    // Getters y Setters
    public int getId() {return id;}
    public LocalDate getFecha() {return fecha;}
    public double getIngresosTotal() {return ingresosTotal;}
    public double getEgresosTotal() {return egresosTotal;}
    public double getSaldoFinal() {return saldoFinal;}
    public double getEfectivo() {return efectivo;}
    public double getTransferencia() {return transferencia;}
    public double getPos() {return pos;}

    public void setId(int id) {this.id = id;}
    public void setFecha(LocalDate fecha) {this.fecha = fecha;}
    public void setIngresosTotal(double ingresosTotal) {this.ingresosTotal = ingresosTotal;}
    public void setEgresosTotal(double egresosTotal) {this.egresosTotal = egresosTotal;}
    public void setSaldoFinal(double saldoFinal) {this.saldoFinal = saldoFinal;}
    public void setEfectivo(double efectivo) {this.efectivo = efectivo;}
    public void setTransferencia(double transferencia) {this.transferencia = transferencia;}
    public void setPos(double pos) {this.pos = pos;}

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CajaDiaria that = (CajaDiaria) o;
        return id == that.id && Double.compare(ingresosTotal, that.ingresosTotal) == 0 && Double.compare(egresosTotal, that.egresosTotal) == 0 && Double.compare(saldoFinal, that.saldoFinal) == 0 && Double.compare(efectivo, that.efectivo) == 0 && Double.compare(transferencia, that.transferencia) == 0 && Double.compare(pos, that.pos) == 0 && Objects.equals(fecha, that.fecha);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fecha, ingresosTotal, egresosTotal, saldoFinal, efectivo, transferencia, pos);
    }

    @Override
    public String toString() {
        return "CajaDiaria{" +
                "id=" + id +
                ", fecha=" + fecha +
                ", ingresosTotal=" + ingresosTotal +
                ", egresosTotal=" + egresosTotal +
                ", saldoFinal=" + saldoFinal +
                ", efectivo=" + efectivo +
                ", transferencia=" + transferencia +
                ", pos=" + pos +
                '}';
    }
}
