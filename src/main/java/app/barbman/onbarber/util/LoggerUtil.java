package app.barbman.onbarber.util;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;

public class LoggerUtil {

    public static Logger getLogger(Class<?> clazz) {
        Logger logger = Logger.getLogger(clazz.getName());

        if (logger.getHandlers().length == 0) {
            try {
                File logDir = new File("data");
                if (!logDir.exists()) {
                    logDir.mkdirs();
                }

                // LOG A CONSOLA
                ConsoleHandler consoleHandler = new ConsoleHandler();
                consoleHandler.setLevel(Level.ALL);
                consoleHandler.setFormatter(new SimpleFormatter());

                // LOG A ARCHIVO
                FileHandler fileHandler = new FileHandler("data/barbman.log", true);
                // true = no sobreescribe
                fileHandler.setLevel(Level.ALL);
                fileHandler.setFormatter(new SimpleFormatter());

                logger.addHandler(consoleHandler);
                logger.addHandler(fileHandler);

                logger.setUseParentHandlers(false); // evita duplicación en consola

                logger.setLevel(Level.ALL);

            } catch (IOException e) {
                System.err.println("No se pudo crear el archivo de log: " + e.getMessage());
            }
        }

        return logger;
    }
}
