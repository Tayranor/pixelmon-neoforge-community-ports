package de.waterdu.atlantis;

import de.waterdu.atlantis.util.java.ExceptionUtils;
import org.apache.logging.log4j.LogBuilder;
import org.apache.logging.log4j.LogManager;

public class AtlantisLogger {

    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger("Atlantis");

    private AtlantisLogger() {
        ExceptionUtils.throwCannotInstantiateClassException();
    }

    public static void log(Severity severity, String message, Object... args) {
        switch (severity) {
            case NONE:
                return;
            case ERROR:
                AtlantisLogger.error(message, args);
                return;
            case WARN:
                AtlantisLogger.warn(message, args);
                return;
            case INFO:
                AtlantisLogger.info(message, args);
                return;
            case DEBUG:
                AtlantisLogger.debug(message, args);
        }
    }

    public static void info(String message, Object... args) {
        AtlantisLogger.LOGGER.info(message, args);
    }

    public static void warn(String message, Object... args) {
        AtlantisLogger.LOGGER.warn(message, args);
    }

    public static void error(String message, Object... args) {
        AtlantisLogger.LOGGER.error(message, args);
    }

    public static void debug(String message, Object... args) {
        AtlantisLogger.LOGGER.debug(message, args);
    }

    public static LogBuilder debugBuilder() {
        return LOGGER.atDebug();
    }

    public enum Severity {

        ERROR,
        WARN,
        INFO,
        DEBUG,
        NONE,
    }
}
