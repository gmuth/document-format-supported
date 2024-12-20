package de.gmuth.ipp;

/**
 * Copyright (c) 2024 Gerhard Muth
 */

import de.gmuth.ipp.attributes.PrinterState;
import de.gmuth.ipp.core.*;
import de.gmuth.log.Logging;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class AttributesNormalizer {

    static Logger logger = Logging.getLogger(AttributesNormalizer.class);

    protected IppResponse response;
    protected IppAttributesGroup attributes;

    public AttributesNormalizer(IppResponse ippResponse) {
        this.response = ippResponse;
        this.attributes = ippResponse.getPrinterGroup();
    }

    boolean normalize() {
        logger.fine("Normalize response: " + response);
        byte[] oldBytes = response.getRawBytes();
        hardcodePrivateValues();
        hardcodeVolatileValues();
        response.encode(false); // keep raw bytes in sync
        byte[] newBytes = response.getRawBytes();
        boolean isModified = !Arrays.equals(oldBytes, newBytes);
        if (isModified) logger.info("> Normalized response: " + response);
        return isModified;
    }

    void hardcodePrivateValues() {
        ifAttributesContainKeySetValue("printer-location", "Greenwich");
        ifAttributesContainKeySetValue("printer-geo-location", URI.create("geo:51.477,0"));
        ifAttributesContainKeySetValues("printer-organization", "International Meridian Conference");
        ifAttributesContainKeySetValues("printer-organizational-unit", "1884");

        ifAttributesContainKeySetValue("printer-uuid", URI.create("urn:uuid:01234567-89ab-cdef-0123-456789abcdef"));

        IppString makeAndModel = attributes.getValue("printer-make-and-model");
        ifAttributesContainKeySetValue("printer-dns-sd-name", makeAndModel);
        ifAttributesContainKeySetValue("printer-name", makeAndModel);
        ifAttributesContainKeySetValue("printer-info", makeAndModel);
    }

    void hardcodeVolatileValues() {
        ifAttributesContainKeySetValue("printer-state", PrinterState.Idle.getCode());
        ifAttributesContainKeySetValues("printer-state-reasons", "none");
        ifAttributesContainKeySetValue("printer-state-message", "Raising ball to top of the mast.");
        ifAttributesContainKeySetValue("printer-is-accepting-jobs", true);
        ifAttributesContainKeySetValue("queued-job-count", 0);
        ifAttributesContainKeySetValue("printer-message-from-operator", "Abandoning solar time.");
        for (String name : new ArrayList<>(attributes.keySet())) {
            IppAttribute<?> attribute = attributes.get(name);
            hardcodeTimeValues(attribute);
            hardCodeUriValues(attribute);
        }
    }

    private void hardcodeTimeValues(IppAttribute<?> attribute) {
        if (attribute.getName().endsWith("-time") && attribute.getTag() == IppTag.Integer) {
            setValue(attribute.getName(), 0); // epoch 1.1.1970
        } else if (attribute.getTag() == IppTag.DateTime) {
            setValue(attribute.getName(), new IppDateTime(
                    ZonedDateTime.of(1884, 10, 13, 12, 0, 0, 0, ZoneId.of("UTC"))
            ));
        }
    }

    private void hardCodeUriValues(IppAttribute<?> attribute) {
        if (attribute.getTag() == IppTag.Uri) {
            List<URI> newUris = new ArrayList<>();
            for (URI uri : (List<URI>) attribute.getValues()) {
                boolean doNormalize = uri.getScheme().startsWith("ipp") || uri.getScheme().startsWith("http");
                if (uri.getScheme().startsWith("http") && uri.getHost().endsWith(".com")) {
                    doNormalize = false;
                    logger.info("Keeping unmodified: " + attribute);
                }
                if (doNormalize) newUris.add(newUriWithHost(uri, "123.45.67.89"));
                else newUris.add(uri);
            }
            setValues(attribute.getName(), newUris);
        }
    }

    private static URI newUriWithHost(URI uri, String host) {
        try {
            return new URI(
                    uri.getScheme(), uri.getUserInfo(), host, uri.getPort(),
                    uri.getRawPath(), uri.getRawQuery(), uri.getRawFragment()
            );
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    void ifAttributesContainKeySetValue(String name, Object value) {
        if (attributes.containsKey(name)) setValue(name, value);
    }

    void ifAttributesContainKeySetValues(String name, List<Object> values) {
        if (attributes.containsKey(name)) setValues(name, values);
    }

    void ifAttributesContainKeySetValues(String name, Object... values) {
        if (attributes.containsKey(name)) setValues(name, Arrays.asList(values));
    }

    void setValue(String name, Object value) {
        IppAttribute<?> oldAttribute = attributes.get(name);
        IppAttribute<?> newAttribute = new IppAttribute<>(name, oldAttribute.getTag(), value);
        if (oldAttribute.getTag().isOutOfBandTag()) {
            logger.info("Keeping unmodified: " + oldAttribute);
        } else if (!newAttribute.toString().equals(oldAttribute.toString())) {
            logger.info(oldAttribute + " --- replaced by ---> " + value);
            attributes.put(newAttribute);
        }
    }

    void setValues(String name, List<?> values) {
        IppAttribute<?> oldAttribute = attributes.get(name);
        IppAttribute<?> newAttribute = new IppAttribute<>(name, oldAttribute.getTag(), values);
        if (!newAttribute.toString().equals(oldAttribute.toString())) {
            String valuesString = values.stream().map(Object::toString).collect(Collectors.joining(", "));
            logger.info(oldAttribute + " --- replaced by ---> " + valuesString);
            attributes.put(newAttribute);
        }
    }

    public static void main(String[] args) {
        Logging.configure(Level.INFO);
        logger.setLevel(Level.FINE);
        try {
            IppMessageRepository ippMessageRepository = IppMessageRepository.getInstance();
            IppResponse ippResponse = ippMessageRepository.getIppResponse("HP LaserJet 100 colorMFP M175nw");
            AttributesNormalizer attributesNormalizer = new AttributesNormalizer(ippResponse);
            boolean modified = attributesNormalizer.normalize();
            //if(modified) ippMessageRepository.saveIppResponse(ippResponse, true);
        } catch (Throwable throwable) {
            logger.log(Level.SEVERE, "main failed", throwable);
        }
        Logging.flush();
    }

}
