package app.barbman.core.dto;

/**
 * DTO para guardar información que se muestra en la vista de sueldos
 */
public class SueldoDTO {
    private int barberoId;
    private String nombreBarbero;
    private double produccionTotal;
    private double montoLiquidado;
    private boolean pagado; // true si ya está en la tabla sueldos
    private int sueldoId;   // si ya existe un sueldo, se usa este id, si no = -1

    public SueldoDTO() {
    }

    public SueldoDTO(int barberoId, String nombreBarbero, double produccionSemanal,
                     double montoLiquidado, boolean pagado, int sueldoId) {
        this.barberoId = barberoId;
        this.nombreBarbero = nombreBarbero;
        this.produccionTotal = produccionSemanal;
        this.montoLiquidado = montoLiquidado;
        this.pagado = pagado;
        this.sueldoId = sueldoId;
    }

    public int getBarberoId() {
        return barberoId;
    }

    public void setBarberoId(int barberoId) {
        this.barberoId = barberoId;
    }

    public String getNombreBarbero() {
        return nombreBarbero;
    }

    public void setNombreBarbero(String nombreBarbero) {
        this.nombreBarbero = nombreBarbero;
    }

    public double getProduccionTotal() {
        return produccionTotal;
    }

    public void setProduccionTotal(double produccionTotal) {
        this.produccionTotal = produccionTotal;
    }

    public double getMontoLiquidado() {
        return montoLiquidado;
    }

    public void setMontoLiquidado(double montoLiquidado) {
        this.montoLiquidado = montoLiquidado;
    }

    public boolean isPagado() {
        return pagado;
    }

    public void setPagado(boolean pagado) {
        this.pagado = pagado;
    }

    public int getSueldoId() {
        return sueldoId;
    }

    public void setSueldoId(int sueldoId) {
        this.sueldoId = sueldoId;
    }
}
