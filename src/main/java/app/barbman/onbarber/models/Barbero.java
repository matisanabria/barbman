package app.barbman.onbarber.models;

public class Barbero {
    int id;
    int tipoCobro;
    String nombre;
    String rol;
    String pin;
    float param1;
    float param2;

    public Barbero() {}

    // Constructor full
    public Barbero(int id, int tipoCobro, String nombre, String rol, String pin, float param1, float param2){
        this.id = id;
        this.tipoCobro = tipoCobro;
        this.nombre = nombre;
        this.rol = rol;
        this.pin = pin;
        this.param1 = param1;
        this.param2 = param2;
    }

    // Constructor corto
    public Barbero(String nombre, String rol, String pin) {
        this.nombre = nombre;
        this.rol = rol;
        this.pin = pin;
        this.tipoCobro = 0;
        this.param1 = 0f;
        this.param2 = 0f;
    }

    // Getters
    public int getId() {return id;}
    public int getTipoCobro() {return tipoCobro;}
    public String getNombre() {return nombre;}
    public String getRol() {return rol;}
    public String getPin() {return pin;}
    public float getParam1() {return param1;}
    public float getParam2() {return param2;}

    // Setters
    public void setId(int id) {this.id = id;} // Para cargar desde DB
    public void setTipoCobro(int tipoCobro) {
        if (tipoCobro < 0 || tipoCobro > 4) {
            throw new IllegalArgumentException("Tipo de cobro inválido.");
        }
        this.tipoCobro = tipoCobro;
    }
    public void setNombre(String nombre) {this.nombre = nombre;}
    public void setRol(String rol) {this.rol = rol;}
    public void setPin(String pin) {
        if (!pin.matches("\\d{4}")) {
            throw new IllegalArgumentException("El PIN debe tener exactamente 4 dígitos numéricos.");
        }
        this.pin = pin;
    }
    public void setParam1(float param1) {this.param1 = param1;}
    public void setParam2(float param2) {this.param2 = param2;}

    // Configuración de tipo de cobro
    /**
     * Tipo de cobro del barbero:
     * 0 = No definido (por defecto)
     * 1 = Por producción (param1 = porcentaje de cobro, ej: 0.5 = 50%)
     * 2 = Sueldo base + porcentaje (param1 = sueldo base, param2 = porcentaje)
     * 3 = Sueldo fijo semanal (param1 = monto fijo, param2 = no usado)
     * 4 = Caso especial (parámetros interpretados según lógica personalizada)
     */

    public void configurarProduccion(float porcentaje) {
        this.tipoCobro = 1;
        this.param1 = porcentaje;
        this.param2 = 0f;
    }

    public void configurarSueldoBase(float sueldoBase, float porcentajeExtra) {
        this.tipoCobro = 2;
        this.param1 = sueldoBase;
        this.param2 = porcentajeExtra;
    }
}
