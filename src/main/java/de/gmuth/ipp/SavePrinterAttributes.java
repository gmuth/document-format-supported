package de.gmuth.ipp;

/**
 * Copyright (c) 2024 Gerhard Muth
 */

import de.gmuth.ipp.client.IppConfig;
import de.gmuth.ipp.client.IppPrinter;
import de.gmuth.log.Logging;

import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.gmuth.ipp.PrinterAttributesRepository.printersdirectory;

public class SavePrinterAttributes {

    public static void main(String[] args) {
        Logging.configure(Level.INFO);
        Logger logger = Logging.getLogger(SavePrinterAttributes.class);
        try {
            if (args.length == 0) throw new RuntimeException("argument printerUri is missing");
            String printerUri = args[0];
            logger.info("Connecting to " + printerUri + ".");

            IppConfig ippConfig = new IppConfig();
            ippConfig.setTimeout(Duration.ofSeconds(3));
            IppPrinter ippPrinter = new IppPrinter(printerUri, ippConfig);

            if(ippPrinter.isCups()) {
                logger.info("CUPS printer driver is out of scope.");
                logger.info(ippPrinter.getDeviceUri().toString());
                logger.info("Use the printerURI instead.");
            } else {
                ippPrinter.savePrinterAttributes(printersdirectory);
            }
        } catch (Throwable throwable) {
            logger.log(Level.SEVERE, "main failed", throwable);
        }
    }
}