package app.barbman.core.util.legacy;

import app.barbman.core.repositories.DbBootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class LegacyDatabase {
    private static final Logger logger = LogManager.getLogger(LegacyDatabase.class);

    public static Connection getConnection() throws SQLException {
        File appFolder = DbBootstrap.getAppFolder();
        // Verificamos que appFolder no sea null
        if (appFolder == null) {
            logger.error("[LEGACY-DB] El appFolder es NULL. DbBootstrap no se inició correctamente.");
            return null;
        }

        File legacyFile = new File(appFolder, "data/legacy.db");
        logger.info("[LEGACY-DB] Buscando archivo en: {}", legacyFile.getAbsolutePath());

        if (!legacyFile.exists()) {
            logger.warn("[LEGACY-DB] Archivo legacy.db NO ENCONTRADO en la ruta especificada.");
            return null;
        }

        logger.info("[LEGACY-DB] Archivo encontrado. Intentando conectar...");
        String url = "jdbc:sqlite:" + legacyFile.getAbsolutePath();
        return DriverManager.getConnection(url);
    }
}