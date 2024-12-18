package de.gmuth.ipp;

/**
 * Copyright (c) 2024 Gerhard Muth
 */

import de.gmuth.ipp.client.IppConfig;
import de.gmuth.ipp.client.IppPrinter;
import de.gmuth.log.Logging;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.gmuth.ipp.PrinterAttributesRepository.printersdirectory;

public class SavePrinterAttributes {

    static Logger logger = Logging.getLogger(SavePrinterAttributes.class);

    public static void main(String[] args) {
        Logging.configure(Level.INFO);
        try {
            if (args.length > 0) {
                List<String> argList = Arrays.asList(args);
                logger.info("> Process printerUri arguments: " + String.join(", ", argList));
                savePrinterAttributes(argList);
            } else {
                logger.info("> Looking for mdns services of type _ipp._tcp.local.");
                Logging.flush();
                JmDNS jmDns = JmDNS.create();
                for (ServiceInfo serviceInfo : jmDns.list("_ipp._tcp.local.", 5000)) {
                    savePrinterAttributes(toUri(serviceInfo));
                }
                jmDns.close();
            }
        } catch (Throwable throwable) {
            logger.log(Level.SEVERE, "main failed", throwable);
        }
        Logging.flush();
    }

    static String toUri(ServiceInfo serviceInfo) {
        return String.format("ipp://%s:%d/%s",
                serviceInfo.getServer(),
                serviceInfo.getPort(),
                serviceInfo.getPropertyString("rp")
        );
    }

    static void savePrinterAttributes(List<String> printerUris) {
        printerUris.forEach(uri -> savePrinterAttributes(uri));
    }

    static void savePrinterAttributes(String printerUri) {
        try {
            logger.info("Connecting to " + printerUri + ".");
            IppConfig ippConfig = new IppConfig();
            ippConfig.setTimeout(Duration.ofSeconds(3));
            IppPrinter ippPrinter = new IppPrinter(printerUri, ippConfig);
            if (ippPrinter.isCups()) {
                logger.info("CUPS printer driver is out of scope.");
                logger.info(ippPrinter.getDeviceUri().toString());
                logger.info("Use the printerURI instead.");
            } else {
                ippPrinter.savePrinterAttributes(printersdirectory);
            }
        } catch (Exception exception) {
            logger.warning("Failed to get attributes from " + printerUri);
            logger.warning(exception.toString());
        }
    }

}