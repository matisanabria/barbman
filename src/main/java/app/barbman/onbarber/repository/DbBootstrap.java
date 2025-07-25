package app.barbman.onbarber.repository;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;


public class DbBootstrap {
    private static final Logger logger = Logger.getLogger(DbBootstrap.class.getName());

    public static final String DB_FOLDER = "data";
    public static final String DB_NAME = "barbman.db";

    // Checks if database exists. Create database if don't exists.
    public static void init() {
        logger.info("Chequeando database...");

        // Checks if "/data" folder exists
        File folder = new File(DB_FOLDER);
        if (!folder.exists()) {
            logger.warning("Carpeta de base de datos no encontrada.");
            if (folder.mkdirs()) {
                logger.info("Carpeta creada exitosamente: " + DB_FOLDER);
            } else {
                logger.severe("No se pudo crear la carpeta de base de datos.");
            }
        }

        // Checks if database exists. If not, creates one
        File dbFile = new File(DB_FOLDER + "/" + DB_NAME);
        if (dbFile.exists()) logger.info("Base de datos encontrada en: " + dbFile.getPath());
        else {
            logger.warning("Base de datos no encontrada. Creando base de datos...");
            createTables();  // <- Creates database tables
        }
    }

    // Connects to database
    private static Connection connect() throws SQLException {
        String url = "jdbc:sqlite:" + DB_FOLDER + "/" + DB_NAME;
        return DriverManager.getConnection(url);
    }

    // Creates database tables if don't exists
    private static void createTables() {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            logger.info("Creando tablas...");

            // Tabla barberos
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS barberos (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre TEXT NOT NULL,
                    rol TEXT NOT NULL,
                    pin TEXT NOT NULL UNIQUE,
                    tipo_cobro INTEGER NOT NULL DEFAULT 0,
                    param_1 REAL,
                    param_2 REAL
                );
            """);
            // Tabla servicios pre-definidos
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS servicios_definidos (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre TEXT NOT NULL UNIQUE,
                    precio_base REAL NOT NULL
                );
            """);
            // Tabla servicios realizados
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS servicios_realizados (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    barbero_id INTEGER NOT NULL,
                    tipo_servicio TEXT NOT NULL,
                    precio REAL NOT NULL,
                    fecha TEXT NOT NULL,
                    forma_pago TEXT NOT NULL,
                    FOREIGN KEY (barbero_id) REFERENCES barberos(id)
                );
            """);
            // Tabla egresos
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS egresos (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    barbero_id INTEGER NOT NULL,
                    descripcion TEXT NOT NULL,
                    monto REAL NOT NULL,
                    fecha TEXT NOT NULL,
                    tipo_egreso TEXT NOT NULL,
                    FOREIGN KEY (barbero_id) REFERENCES barberos(id)
                );
                """);
            // Tabla clientes
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS clientes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nombre TEXT NOT NULL,
                        telefono TEXT,
                        observaciones TEXT
                    );
                    """);


            logger.info("Tablas creadas correctamente.");
        } catch (SQLException e) {
            logger.severe("Error creando las tablas: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
