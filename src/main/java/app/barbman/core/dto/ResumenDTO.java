package app.barbman.core.dto;

import java.time.LocalDate;
import java.util.Objects;

public class ResumenDTO {
    private LocalDate desde;
    private LocalDate hasta;
    private double ingresosTotal;
    private double egresosTotal;
    private double saldoFinal;
    private double efectivo;
    private double transferencia;
    private double pos;

    public ResumenDTO(LocalDate desde, LocalDate hasta,
                      double ingresosTotal, double egresosTotal, double saldoFinal,
                      double efectivo, double transferencia, double pos) {
        this.desde = desde;
        this.hasta = hasta;
        this.ingresosTotal = ingresosTotal;
        this.egresosTotal = egresosTotal;
        this.saldoFinal = saldoFinal;
        this.efectivo = efectivo;
        this.transferencia = transferencia;
        this.pos = pos;
    }

    // getters

    public LocalDate getDesde() {
        return desde;
    }

    public LocalDate getHasta() {
        return hasta;
    }

    public double getIngresosTotal() {
        return ingresosTotal;
    }

    public double getEgresosTotal() {
        return egresosTotal;
    }

    public double getSaldoFinal() {
        return saldoFinal;
    }

    public double getEfectivo() {
        return efectivo;
    }

    public double getTransferencia() {
        return transferencia;
    }

    public double getPos() {
        return pos;
    }

    // setters

    public void setDesde(LocalDate desde) {
        this.desde = desde;
    }

    public void setHasta(LocalDate hasta) {
        this.hasta = hasta;
    }

    public void setIngresosTotal(double ingresosTotal) {
        this.ingresosTotal = ingresosTotal;
    }

    public void setEgresosTotal(double egresosTotal) {
        this.egresosTotal = egresosTotal;
    }

    public void setSaldoFinal(double saldoFinal) {
        this.saldoFinal = saldoFinal;
    }

    public void setEfectivo(double efectivo) {
        this.efectivo = efectivo;
    }

    public void setTransferencia(double transferencia) {
        this.transferencia = transferencia;
    }

    public void setPos(double pos) {
        this.pos = pos;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ResumenDTO that = (ResumenDTO) o;
        return Double.compare(ingresosTotal, that.ingresosTotal) == 0 && Double.compare(egresosTotal, that.egresosTotal) == 0 && Double.compare(saldoFinal, that.saldoFinal) == 0 && Double.compare(efectivo, that.efectivo) == 0 && Double.compare(transferencia, that.transferencia) == 0 && Double.compare(pos, that.pos) == 0 && Objects.equals(desde, that.desde) && Objects.equals(hasta, that.hasta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(desde, hasta, ingresosTotal, egresosTotal, saldoFinal, efectivo, transferencia, pos);
    }

    @Override
    public String toString() {
        return "ResumenDTO{" +
                "desde=" + desde +
                ", hasta=" + hasta +
                ", ingresosTotal=" + ingresosTotal +
                ", egresosTotal=" + egresosTotal +
                ", saldoFinal=" + saldoFinal +
                ", efectivo=" + efectivo +
                ", transferencia=" + transferencia +
                ", pos=" + pos +
                '}';
    }
}

