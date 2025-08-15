package app.barbman.onbarber.repository;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


public class DbBootstrap {
    private static final Logger logger = LogManager.getLogger(DbBootstrap.class);

    public static final String DB_FOLDER = "data";
    public static final String DB_NAME = "barbman.db";

    // Checks if database exists. Create database if don't exists.
    public static void init() {
        logger.info("Chequeando database...");

        // Checks if "/data" folder exists
        File folder = new File(DB_FOLDER);
        if (!folder.exists()) {
            logger.warn("Carpeta de base de datos no encontrada.");
            if (folder.mkdirs()) {
                logger.info("Carpeta creada exitosamente: " + DB_FOLDER);
            } else {
                logger.error("No se pudo crear la carpeta de base de datos.");
            }
        }

        // Checks if database exists. If not, creates one
        File dbFile = new File(DB_FOLDER + "/" + DB_NAME);
        if (dbFile.exists()) logger.info("Base de datos encontrada en: " + dbFile.getPath());
        else {
            logger.warn("Base de datos no encontrada. Creando base de datos...");
            createTables();  // <- Creates database tables
        }
    }

    // Connects to database
    public static Connection connect() throws SQLException {
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
                pin TEXT NOT NULL UNIQUE CHECK(length(pin) = 4 AND pin GLOB '[0-9][0-9][0-9][0-9]'),
                tipo_cobro INTEGER NOT NULL DEFAULT 0,
                param_1 REAL,
                param_2 REAL
            );
        """);

            // Tabla servicios predefinidos
            stmt.execute("""
            CREATE TABLE IF NOT EXISTS servicios_definidos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL UNIQUE,
                precio_base REAL NOT NULL
            );
        """);

            // Tabla servicios realizados
            // FIXME: Pasar fecha a tipo DATES
            stmt.execute("""
            CREATE TABLE IF NOT EXISTS servicios_realizados (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                barbero_id INTEGER NOT NULL,
                tipo_servicio INTEGER NOT NULL,
                precio REAL NOT NULL,
                fecha TEXT NOT NULL,
                forma_pago TEXT NOT NULL,
                observaciones TEXT,
                FOREIGN KEY (barbero_id) REFERENCES barberos(id),
                FOREIGN KEY (tipo_servicio) REFERENCES servicios_definidos(id)
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

            // Tabla caja
            stmt.execute("""
            CREATE TABLE IF NOT EXISTS caja (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                fecha TEXT NOT NULL UNIQUE,
                ingresos_total REAL NOT NULL,
                egresos_total REAL NOT NULL,
                efectivo REAL NOT NULL,
                transferencia REAL NOT NULL,
                pos REAL NOT NULL,
                saldo REAL NOT NULL,
                observaciones TEXT,
                registrado_por INTEGER NOT NULL,
                FOREIGN KEY (registrado_por) REFERENCES barberos(id)
            );
        """);

            // Tabla sueldos
            stmt.execute("""
            CREATE TABLE IF NOT EXISTS sueldos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                barbero_id INTEGER NOT NULL,
                fecha_inicio_semana TEXT NOT NULL,
                fecha_fin_semana TEXT NOT NULL,
                produccion_total REAL NOT NULL,
                monto_pagado REAL NOT NULL,
                tipo_cobro_snapshot TEXT NOT NULL,
                fecha_pago TEXT,
                FOREIGN KEY (barbero_id) REFERENCES barberos(id)
            );
        """);

            logger.info("Tablas creadas correctamente.");
        } catch (SQLException e) {
            logger.error("Error creando las tablas: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
