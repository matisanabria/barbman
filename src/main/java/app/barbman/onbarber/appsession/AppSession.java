package app.barbman.onbarber.appsession;

import app.barbman.onbarber.model.Barbero;

public class AppSession {
    private static Barbero barberoActivo;

    public static void iniciarSesion(Barbero barbero) {
        AppSession.barberoActivo = barbero;
    }

    public static Barbero getBarberoActivo() {
        return barberoActivo;
    }

    public static void cerrarSesion() {
        AppSession.barberoActivo = null;
    }

    public static boolean sesionActiva() {
        return barberoActivo != null;
    }
}
