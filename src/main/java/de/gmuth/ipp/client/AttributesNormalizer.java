package de.gmuth.ipp.client;

/**
 * Copyright (c) 2024-2025 Gerhard Muth
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
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.Collections.nCopies;

public class AttributesNormalizer {

    static Logger logger = Logging.getLogger(AttributesNormalizer.class);

    protected IppResponse response;
    protected IppAttributesGroup attributes;

    public AttributesNormalizer(IppResponse ippResponse) {
        this.response = ippResponse;
        this.attributes = ippResponse.getPrinterGroup();
    }

    public boolean normalize() {
        logger.fine("Normalize response: " + response);
        byte[] oldBytes = response.getRawBytes();
        response.setRawBytes(null); // invalidate original bytes
        hardcodePrivateValues();
        hardcodePrivateUriValues();
        hardcodeVolatileValues();
        hardcodeVolatileTimeValues();
        hardcodeVolatileMarkerValues();
        byte[] newBytes = response.encode(false);
        boolean isModified = !Arrays.equals(oldBytes, newBytes);
        if (isModified) logger.info("> Normalized response: " + response);
        return isModified;
    }

    private void hardcodePrivateValues() {
        normalizeAttributeValue("printer-location", "Greenwich");
        normalizeAttributeValue("printer-wifi-ssid", "Observatory");
        normalizeAttributeValue("printer-geo-location", URI.create("geo:51.477,0"));
        normalizeAttributeValues("printer-organization", "International Meridian Conference");
        normalizeAttributeValues("printer-organizational-unit", "1884");

        IppString makeAndModel = attributes.getValue("printer-make-and-model");
        normalizeAttributeValue("printer-dns-sd-name", makeAndModel);
        normalizeAttributeValue("printer-name", makeAndModel);
        normalizeAttributeValue("printer-info", makeAndModel);

        normalizeAttributeValue("printer-uuid", URI.create("urn:uuid:01234567-89ab-cdef-0123-456789abcdef"));
    }

    private void hardcodeVolatileValues() {
        normalizeAttributeValue("printer-state", PrinterState.Idle.getCode());
        normalizeAttributeValues("printer-state-reasons", "none");
        normalizeAttributeValue("printer-state-message", "Raising ball to top of the mast.");
        normalizeAttributeValue("printer-is-accepting-jobs", true);
        normalizeAttributeValue("queued-job-count", 0);
        normalizeAttributeValue("printer-message-from-operator", "Abandoning solar time.");
        normalizeAttributeValues("printer-alert", "none");
        normalizeAttributeValues("printer-alert-description", "none");
    }

    private void hardcodeVolatileTimeValues() {
        for (String name : new ArrayList<>(attributes.keySet())) {
            IppAttribute<?> attribute = attributes.get(name);
            if (name.endsWith("-time") && attribute.getTag() == IppTag.Integer) {
                setAttributeValue(name, 0); // epoch 1.1.1970
            } else if (attribute.getTag() == IppTag.DateTime) {
                setAttributeValue(name, new IppDateTime(
                        ZonedDateTime.of(1884, 10, 13, 12, 0, 0, 0, ZoneId.of("UTC"))
                ));
            }
        }
    }

    private void hardcodeVolatileMarkerValues() {
        for (String name : new ArrayList<>(attributes.keySet())) {
            IppAttribute<?> attribute = attributes.get(name);
            if (attribute.getTag() == IppTag.Integer) {
                if (name.equals("marker-levels")) setAttributeValues(name, nCopies(attribute.getValues().size(), 50));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void hardcodePrivateUriValues() {
        for (String name : new ArrayList<>(attributes.keySet())) {
            IppAttribute<?> attribute = attributes.get(name);
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
                setAttributeValues(name, newUris);
            }
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

    boolean attributeValuesShouldBeNormalized(String name) {
        if (!attributes.containsKey(name)) return false;
        IppAttribute<?> attribute = attributes.get(name);
        if (attribute.getTag().isOutOfBandTag()) return false;
        if (attributeValueIsEmpty(attribute)) return false;
        return true;
    }

    boolean attributeValueIsEmpty(IppAttribute<?> attribute) {
        if (attribute.is1setOf()) {
            Collection<?> values = attribute.getValues();
            return values.isEmpty() || values.size() == 1 && valueIsEmpty(values.iterator().next());
        } else {
            return valueIsEmpty(attribute.getValue());
        }
    }

    boolean valueIsEmpty(Object value) {
        if (value instanceof String) {
            return ((String) value).isEmpty();
        } else if (value instanceof IppString) {
            return ((IppString) value).getText().isEmpty();
        }
        return false;
    }

    void normalizeAttributeValue(String name, Object value) {
        if (attributeValuesShouldBeNormalized(name)) setAttributeValue(name, value);
    }

    void normalizeAttributeValues(String name, Object... values) {
        if (attributeValuesShouldBeNormalized(name)) setAttributeValues(name, Arrays.asList(values));
    }

    void setAttributeValue(String name, Object value) {
        IppAttribute<?> oldAttribute = attributes.get(name);
        IppAttribute<?> newAttribute = new IppAttribute<>(name, oldAttribute.getTag(), value);
        if (oldAttribute.getTag().isOutOfBandTag()) {
            logger.warning("Keeping unmodified: " + oldAttribute);
        } else if (!newAttribute.toString().equals(oldAttribute.toString())) {
            logger.info(oldAttribute + " --- replaced by ---> " + value);
            attributes.put(newAttribute);
        }
    }

    void setAttributeValues(String name, List<?> values) {
        IppAttribute<?> oldAttribute = attributes.get(name);
        IppAttribute<?> newAttribute = new IppAttribute<>(name, oldAttribute.getTag(), values);
        if (!newAttribute.toString().equals(oldAttribute.toString())) {
            String valuesString = values.stream().map(Object::toString).collect(Collectors.joining(","));
            logger.info(oldAttribute + " --- replaced by ---> " + valuesString);
            attributes.put(newAttribute);
        }
    }

    public static void main(String[] args) {
        Logging.configure(Level.INFO, true);
        logger.setLevel(Level.FINE);
        try {
            IppMessageRepository ippMessageRepository = IppMessageRepository.getInstance();
            for (IppResponse ippResponse : ippMessageRepository.allIppResponses) {
                AttributesNormalizer attributesNormalizer = new AttributesNormalizer(ippResponse);
                boolean modified = attributesNormalizer.normalize();
                if (modified || true) ippMessageRepository.saveIppResponse(ippResponse, true);
            }
        } catch (Throwable throwable) {
            logger.log(Level.SEVERE, "main failed", throwable);
        }
        Logging.flush();
    }

}
