package de.gmuth.ipp.client;

/**
 * Copyright (c) 2025 Gerhard Muth
 */

import de.gmuth.log.Logging;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InspectPrinters {

    static Logger logger = Logging.getLogger(InspectPrinters.class);

    public static void main(String[] args) {
        Logging.configure(Level.INFO);
        try {
            new InspectPrinters().inspectPrinters(args);
        } catch (Throwable throwable) {
            logger.log(Level.SEVERE, "main failed", throwable);
        }
        Logging.flush();
    }

    protected void inspectPrinters(String[] args) throws Exception {
        if (args.length > 0) {
            List<String> argList = Arrays.asList(args);
            logger.info("> printerUris from args: " + String.join(", ", argList));
            argList.forEach(this::inspectPrinter);
        } else {
            logger.info("> Looking for mdns services of type _ipp._tcp.local.");
            Logging.flush();
            PrinterDiscovery.discoverPrinters(this::inspectPrinter);
        }
    }

    protected void inspectPrinter(String printerUri) {
        try {
            IppInspector ippInspector = new IppInspector();
            ippInspector.inspect(URI.create(printerUri), true, true);
        } catch (Exception exception) {
            logger.warning("Failed to inspect printer " + printerUri);
            logger.warning(exception.toString());
            logger.log(Level.FINE, "", exception);
        }
    }
}