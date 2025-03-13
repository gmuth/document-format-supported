package de.gmuth.log;

/**
 * Copyright (c) 2024-2025 Gerhard Muth
 */

import de.gmuth.ipp.client.IppClient;

import java.util.Locale;
import java.util.logging.*;

public class Logging {

    // https://docs.oracle.com/javase/7/docs/api/java/util/logging/SimpleFormatter.html
    public static String format = "%1$tT.%1$tL %3$-30s%4$-9s%5$s%6$s%n";
    public static String shortFormat = "%5$s%6$s%n";
    protected static StreamHandler stdoutHandler;

    public static void configure(Level rootLevel, boolean useShortFormat) {
        Locale.setDefault(Locale.ENGLISH); // for level-name-localized: -Duser.language=en
        System.setProperty("java.util.logging.SimpleFormatter.format", useShortFormat ? shortFormat : format);
        Logger rootLogger = LogManager.getLogManager().getLogger("");
        for (Handler handler : rootLogger.getHandlers()) {
            if (handler instanceof ConsoleHandler) rootLogger.removeHandler(handler);
        }
        rootLogger.setLevel(rootLevel);
        rootLogger.addHandler(new StreamHandler(System.out, new SimpleFormatter() {
            @Override // use simple class name
            public String format(final LogRecord logRecord) {
                String loggerName = logRecord.getLoggerName();
                int lastDot = loggerName.lastIndexOf('.');
                if (lastDot > 0) logRecord.setLoggerName(loggerName.substring(lastDot + 1));
                return super.format(logRecord);
            }
        }) {{
            setLevel(Level.ALL);
            Logging.stdoutHandler = this;
        }});
        configureLogLevels();
    }

    public static void configureLogLevels() {
        setLogLevel(IppClient.class, Level.INFO); // ?? SavePrinterAttributes$IppClient
    }

    public static void flush() {
        stdoutHandler.flush();
    }

    public static Logger getLogger(Class<?> clazz) {
        return Logger.getLogger(clazz.getCanonicalName());
    }

    public static void setLogLevel(Class<?> clazz, Level level) {
        getLogger(clazz).setLevel(level);
    }

    public static void main(String[] args) {
        Logging.configure(Level.ALL, false);
        Logger logger = Logging.getLogger(Logging.class);
        setLogLevel(Logging.class, Level.FINEST);
        try {
            logger.severe(() -> "severe");
            logger.warning(() -> "warning");
            logger.info(() -> "info");
            logger.config(() -> "config");
            logger.fine(() -> "fine");
            logger.finer(() -> "finer");
            logger.finest(() -> "finest");
            throw new Exception("nothing", new RuntimeException("really nothing"));
        } catch (Throwable throwable) {
            logger.log(Level.SEVERE, "What went wrong?", throwable);
        }
        Logging.flush();
    }
}