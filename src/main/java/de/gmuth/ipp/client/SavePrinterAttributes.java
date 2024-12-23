package de.gmuth.ipp.client;

/**
 * Copyright (c) 2024 Gerhard Muth
 */

import de.gmuth.ipp.core.IppAttributesGroup;
import de.gmuth.ipp.core.IppRequest;
import de.gmuth.ipp.core.IppResponse;
import de.gmuth.log.Logging;
import de.gmuth.md.GenerateReadme;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.gmuth.ipp.core.IppOperation.GetPrinterAttributes;

public class SavePrinterAttributes {

    static Logger logger = Logging.getLogger(SavePrinterAttributes.class);
    static private IppMessageRepository ippMessageRepository;

    public static void main(String[] args) {
        Logging.configure(Level.INFO);
        ippMessageRepository = IppMessageRepository.getInstance();
        try {
            boolean generateReadme = false;
            if (args.length > 0) {
                List<String> argList = Arrays.asList(args);
                logger.info("> Process printerUri arguments: " + String.join(", ", argList));
                getAndSavePrinterAttributes(argList);
            } else {
                long seconds = 30;
                logger.info("> Looking for mdns services of type _ipp._tcp.local. (" + seconds + " seconds)");
                Logging.flush();
                JmDNS jmDns = JmDNS.create();
                for (ServiceInfo serviceInfo : jmDns.list("_ipp._tcp.local.", seconds * 1000)) {
                    getAndSavePrinterAttributes(toUri(serviceInfo));
                }
                jmDns.close();
            }
            if (generateReadme) new GenerateReadme().generateReadmeFile();
        } catch (Throwable throwable) {
            logger.log(Level.SEVERE, "main failed", throwable);
        }
        Logging.flush();
    }

    private static String toUri(ServiceInfo serviceInfo) {
        return String.format("ipp://%s:%d/%s",
                serviceInfo.getServer(),
                serviceInfo.getPort(),
                serviceInfo.getPropertyString("rp")
        );
    }

    static void getAndSavePrinterAttributes(List<String> printerUris) {
        printerUris.forEach(SavePrinterAttributes::getAndSavePrinterAttributes);
    }

    static void getAndSavePrinterAttributes(String printerUri) {
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
                boolean modified = new AttributesNormalizer(ippResponse).normalize();
                if (modified) ippMessageRepository.saveIppResponse(ippResponse, true);
            }
        } catch (Exception exception) {
            logger.warning("Failed to get attributes from " + printerUri);
            logger.warning(exception.toString());
        }
    }

    static class IppClient extends de.gmuth.ipp.client.IppClient {
        IppResponse exchangeWrapped(IppRequest ippRequest) {
            return super.exchange(ippRequest);
        }
    }

}