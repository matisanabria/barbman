package app.barbman.onbarber.repositories;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Clase encargada de inicializar la base de datos SQLite y crear las tablas si no existen.
 */
public class DbBootstrap {
    private static final Logger logger = LogManager.getLogger(DbBootstrap.class);

    public static final String DB_FOLDER = "data";  // Carpeta donde se guarda la base de datos
    public static final String DB_NAME = "barbman.db";  // Nombre del archivo de la base de datos

    /**
     * Inicializa la base de datos:
     * - Verifica si la carpeta y el archivo de la base de datos existen.
     * - Si no existen, los crea junto con las tablas necesarias.
     */
    public static void init() {
        logger.info("Chequeando database...");

        // Verifica si la carpeta "data" existe, si no, la crea
        File folder = new File(DB_FOLDER);
        if (!folder.exists()) {
            logger.warn("Carpeta de base de datos no encontrada.");
            if (folder.mkdirs()) {
                logger.info("Carpeta creada exitosamente: " + DB_FOLDER);
            } else {
                logger.error("No se pudo crear la carpeta de base de datos.");
            }
        }

        // Verifica si el archivo de la base de datos existe; si no, lo crea junto con las tablas
        File dbFile = new File(DB_FOLDER + "/" + DB_NAME);
        if (dbFile.exists()) logger.info("Base de datos encontrada en: " + dbFile.getPath());
        else {
            logger.warn("Base de datos no encontrada. Creando base de datos...");
            createTables();  // Crea las tablas de la base de datos
        }
    }

    /**
     * Conecta a la base de datos SQLite.
     * @return Conexión a la base de datos
     * @throws SQLException si hay error al conectar
     */
    public static Connection connect() throws SQLException {
        String url = "jdbc:sqlite:" + DB_FOLDER + "/" + DB_NAME;
        return DriverManager.getConnection(url);
    }

    /**
     * Crea todas las tablas necesarias en la base de datos si no existen.
     * Las tablas incluyen: barberos, servicios_definidos, servicios_realizados, egresos, clientes, caja y sueldos.
     *  Campo fecha en las tablas:
     *  - Formato esperado: 'YYYY-MM-DD' (ISO 8601)
     *  - Ejemplo: '2024-06-10'
     *  - Usar java.sql.Date o LocalDate para manipulación
     */
    private static void createTables() {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            logger.info("Creando tablas...");

            // Tabla de barberos
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS barberos (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            nombre TEXT NOT NULL,
                            rol TEXT NOT NULL,
                            pin TEXT NOT NULL UNIQUE CHECK(length(pin) = 4 AND pin GLOB '[0-9][0-9][0-9][0-9]'),
                            tipo_cobro INTEGER NOT NULL DEFAULT 0,
                            param_1 REAL,
                            param_2 REAL
                        );
                    """);

            // Tabla de servicios predefinidos
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS servicios_definidos (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            nombre TEXT NOT NULL UNIQUE,
                            precio_base REAL NOT NULL
                        );
                    """);

            // Tabla de servicios realizados
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS servicios_realizados (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            barbero_id INTEGER NOT NULL,
                            tipo_servicio INTEGER NOT NULL,
                            precio REAL NOT NULL,
                            fecha TEXT NOT NULL CHECK (fecha = date(fecha)),
                            forma_pago TEXT NOT NULL CHECK (forma_pago IN ('efectivo','transferencia','pos')),
                            observaciones TEXT,
                            FOREIGN KEY (barbero_id) REFERENCES barberos(id),
                            FOREIGN KEY (tipo_servicio) REFERENCES servicios_definidos(id)
                        );
                    """);

            // Tabla de egresos
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS egresos (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            descripcion TEXT NOT NULL,
                            monto REAL NOT NULL CHECK (monto > 0),
                            fecha TEXT NOT NULL CHECK (fecha = date(fecha)),
                            tipo TEXT NOT NULL CHECK (
                                    tipo IN (
                                        'insumo',     -- gel, navajas, productos
                                        'servicio',   -- limpieza, alquiler, luz
                                        'compra',     -- mobiliario, herramientas, decoración
                                        'otros',      -- delivery, cosas fuera de lo normal
                                        'sueldo',     -- liquidación de sueldo semanal
                                        'adelanto'    -- plata adelantada antes del cierre semanal
                                    )
                                )
                            );
                   """);

            // Tabla de caja diaria
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS caja_diaria (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            fecha TEXT NOT NULL UNIQUE,
                            ingresos_total REAL NOT NULL,
                            egresos_total REAL NOT NULL,
                            efectivo REAL NOT NULL,
                            transferencia REAL NOT NULL,
                            pos REAL NOT NULL,
                            saldo_final REAL NOT NULL
                        );
                    """);

            // Tabla de sueldos
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS sueldos (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            barbero_id INTEGER NOT NULL,
                            fecha_inicio_semana TEXT NOT NULL,
                            fecha_fin_semana TEXT NOT NULL,
                            produccion_total REAL NOT NULL,
                            monto_liquidado REAL NOT NULL,
                            tipo_cobro_snapshot TEXT NOT NULL,
                            fecha_pago TEXT,
                            FOREIGN KEY (barbero_id) REFERENCES barberos(id)
                        );
                    """);

            logger.info("Tablas creadas correctamente.");
        } catch (SQLException e) {
            logger.error("Error creando las tablas: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
