package de.gmuth.ipp.client;

/**
 * Copyright (c) 2024-2025 Gerhard Muth
 */

import de.gmuth.ipp.core.IppAttributesGroup;
import de.gmuth.ipp.core.IppRequest;
import de.gmuth.ipp.core.IppResponse;
import de.gmuth.log.Logging;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.gmuth.ipp.client.IppMessageRepository.getPrinterMakeAndModel;
import static de.gmuth.ipp.core.IppOperation.GetPrinterAttributes;

public class SavePrinterAttributes {

    static Logger logger = Logging.getLogger(SavePrinterAttributes.class);
    static protected IppMessageRepository ippMessageRepository;
    public boolean forceSaving = false;

    public static void main(String[] args) {
        Logging.configure(Level.INFO);
        ippMessageRepository = IppMessageRepository.getInstance();
        try {
            new SavePrinterAttributes().getPrinterUrisAndSaveAttributes(args);
            //new GenerateReadme().generateReadmeFile();
        } catch (Throwable throwable) {
            logger.log(Level.SEVERE, "main failed", throwable);
        }
        Logging.flush();
    }

    protected void getPrinterUrisAndSaveAttributes(String[] args) throws Exception {
        if (args.length > 0) {
            List<String> argList = Arrays.asList(args);
            logger.info("> printerUris from args: " + String.join(", ", argList));
            argList.forEach(this::getAndSavePrinterAttributes);
        } else {
            logger.info("> Looking for mdns services of type _ipp._tcp.local.");
            Logging.flush();
            PrinterDiscovery.discoverPrinters(this::getAndSavePrinterAttributes);
        }
    }

    protected void getAndSavePrinterAttributes(String printerUri) {
        try {
            IppClient ippClient = new IppClient();
            IppRequest ippRequest = ippClient.ippRequest(GetPrinterAttributes, URI.create(printerUri), null, null);
            IppResponse ippResponse = ippClient.exchangeWrapped(ippRequest);
            logger.info("Get printer attributes from " + printerUri + " -> " + ippResponse);
            IppAttributesGroup attributes = ippResponse.getPrinterGroup();
            if (attributes.containsKey("cups-version")) {
                logger.info(attributes.get("device-uri").toString());
                logger.info("CUPS printer queues are out of scope. Use the uri of the physical printer instead.");
            } else {
                logger.fine("Normalize response: " + ippResponse);
                String makeAndModel = getPrinterMakeAndModel(ippResponse);
                if (!forceSaving && ippMessageRepository.makeAndModelExists(makeAndModel)) {
                    logger.info(makeAndModel + " already exists.");
                } else {
                    boolean modified = new AttributesNormalizer(ippResponse).normalize();
                    ippMessageRepository.saveIppResponse(ippResponse, true);
                }
            }
        } catch (Exception exception) {
            logger.warning("Failed to get attributes from " + printerUri);
            logger.warning(exception.toString());
            logger.log(Level.FINE, "", exception);
        }
    }

    static class IppClient extends de.gmuth.ipp.client.IppClient {
        IppResponse exchangeWrapped(IppRequest ippRequest) {
            return super.exchange(ippRequest);
        }
    }
}